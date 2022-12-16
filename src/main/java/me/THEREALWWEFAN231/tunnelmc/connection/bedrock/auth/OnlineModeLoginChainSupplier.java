package me.THEREALWWEFAN231.tunnelmc.connection.bedrock.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.THEREALWWEFAN231.tunnelmc.connection.bedrock.LoginChainSupplier;
import me.THEREALWWEFAN231.tunnelmc.connection.bedrock.auth.data.ChainData;
import me.THEREALWWEFAN231.tunnelmc.connection.bedrock.auth.data.XboxToken;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static me.THEREALWWEFAN231.tunnelmc.TunnelMC.JSON_MAPPER;

@Log4j2
@RequiredArgsConstructor
public class OnlineModeLoginChainSupplier extends LoginChainSupplier {
    protected final Consumer<String> infoCallback;
    protected final File rememberAccountFile;

    public OnlineModeLoginChainSupplier() {
        this(s -> {});
    }

    public OnlineModeLoginChainSupplier(File rememberAccountFile) {
        this(s -> {}, rememberAccountFile);
    }

    public OnlineModeLoginChainSupplier(Consumer<String> infoCallback) {
        this(infoCallback, null);
    }

    public CompletableFuture<ChainData> get() {
        return LiveAuthorization.getAccessToken(infoCallback)
                .exceptionally(throwable -> {
                    infoCallback.accept(throwable.getMessage());
                    return null;
                }).whenComplete((token, throwable) -> {
                    if(throwable == null && this.rememberAccountFile != null) {
                        try {
                            JSON_MAPPER.writeValue(this.rememberAccountFile, token);
                        } catch (IOException e) {
                            log.error(e);
                        }
                    }
                }).thenApply(this::getChain);
    }

    protected ChainData getChain(OAuth2AccessToken accessToken) {
        if(accessToken == null) {
            return null;
        }

        KeyPair keyPair = EncryptionUtils.createKeyPair();
        return new ChainData(JSON_MAPPER.createObjectNode().set("chain",
                getSelfSignedChain(getAuthenticatedChain(accessToken, keyPair.getPublic()), keyPair)).toString(), keyPair);
    }

    private String getAuthenticatedChain(OAuth2AccessToken token, PublicKey publicKey) {
        infoCallback.accept("Login Successful! Please Wait...");
        XboxToken xboxToken = XboxAuthorization.getXBLToken(token, "https://multiplayer.minecraft.net/");
        return MinecraftAuthentication.getMinecraftChain(publicKey, xboxToken);
    }

    private ArrayNode getSelfSignedChain(String authenticatedChain, KeyPair keyPair) {
        ArrayNode chainArray;
        String x5uKey;
        try {
            chainArray = (ArrayNode) JSON_MAPPER.readTree(authenticatedChain).get("chain");
            String[] parts = chainArray.get(0).asText().split("\\.");

            JsonNode headerNode = JSON_MAPPER.readTree(Base64.getUrlDecoder().decode(parts[0]));
            x5uKey = headerNode.get("x5u").asText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JsonNode payloadNode = JSON_MAPPER.createObjectNode()
                .put("certificateAuthority", true)
                .put("exp", Instant.now().plus(6, ChronoUnit.HOURS).getEpochSecond())
                .put("identityPublicKey", x5uKey)
                .put("nbf", Instant.now().minus(6, ChronoUnit.HOURS).getEpochSecond());

        String publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        JsonNode headerNode = JSON_MAPPER.createObjectNode()
                .put("alg", "ES384")
                .put("x5u", publicKeyBase64);

        try {
            String header = Base64.getUrlEncoder().withoutPadding().encodeToString(JSON_MAPPER.writeValueAsBytes(headerNode));
            String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(JSON_MAPPER.writeValueAsBytes(payloadNode));
            String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(
                    this.signBytes(keyPair.getPrivate(), (header + "." + payload).getBytes()));

            String jwt = header + "." + payload + "." + signature;
            chainArray.insert(0, jwt);
        } catch (JsonProcessingException | NoSuchAlgorithmException | SignatureException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        return chainArray;
    }
}
