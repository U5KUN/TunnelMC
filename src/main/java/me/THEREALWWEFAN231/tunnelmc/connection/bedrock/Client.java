package me.THEREALWWEFAN231.tunnelmc.connection.bedrock;

import com.nukkitx.network.util.DisconnectReason;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPacket;
import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.BedrockSession;
import com.nukkitx.protocol.bedrock.data.AuthoritativeMovementMode;
import com.nukkitx.protocol.bedrock.data.GameType;
import com.nukkitx.protocol.bedrock.packet.LoginPacket;
import com.nukkitx.protocol.bedrock.v545.Bedrock_v545;
import io.netty.util.AsciiString;
import lombok.extern.log4j.Log4j2;
import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.auth.ClientData;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.ClientBatchHandler;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.BlockEntityDataCache;
import me.THEREALWWEFAN231.tunnelmc.bedrockconnection.caches.container.BedrockContainers;
import me.THEREALWWEFAN231.tunnelmc.connection.bedrock.auth.OfflineModeLoginChainSupplier;
import me.THEREALWWEFAN231.tunnelmc.connection.bedrock.auth.OnlineModeLoginChainSupplier;
import me.THEREALWWEFAN231.tunnelmc.connection.bedrock.auth.data.AuthData;
import me.THEREALWWEFAN231.tunnelmc.connection.bedrock.auth.data.ChainData;
import me.THEREALWWEFAN231.tunnelmc.gui.BedrockConnectingScreen;
import me.THEREALWWEFAN231.tunnelmc.javaconnection.FakeJavaConnection;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicBoolean;

@Log4j2
public class Client {
	public static final BedrockPacketCodec CODEC = Bedrock_v545.V545_CODEC;

	public static Client instance = new Client(null); // TODO: make a client for every connection

	private String ip;
	private int port;

	public ChainData chainData;
	public AuthData authData;
	public BedrockClient bedrockClient;
	public BedrockConnectingScreen connectScreen;
	public FakeJavaConnection javaConnection;
	
	public BedrockContainers containers;
	public BlockEntityDataCache blockEntityDataCache;

	public int entityRuntimeId;
	public byte openContainerId = 0;
	private int revision = 0;
	public AuthoritativeMovementMode movementMode = AuthoritativeMovementMode.CLIENT;
	public GameType defaultGameMode;
	public AtomicBoolean startedSprinting = new AtomicBoolean();
	public AtomicBoolean startedSneaking = new AtomicBoolean();
	public AtomicBoolean stoppedSprinting = new AtomicBoolean();
	public AtomicBoolean stoppedSneaking = new AtomicBoolean();

	Client(InetSocketAddress bindAddress) {

	}

	public void initialize(String ip, int port, boolean onlineMode) {
		this.ip = ip;
		this.port = port;

		LoginChainSupplier supplier;
		if (onlineMode) {
			supplier = new OnlineModeLoginChainSupplier(System.out);
		} else {
			supplier = new OfflineModeLoginChainSupplier(TunnelMC.mc.getSession().getUsername());
		}
		this.chainData = supplier.get().join(); // TODO: make async
		this.authData = this.chainData.decodeAuthData();

		InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", getRandomPort());
		this.bedrockClient = new BedrockClient(bindAddress);
		this.bedrockClient.bind().join();
		this.connectScreen = new BedrockConnectingScreen(MinecraftClient.getInstance().currentScreen, MinecraftClient.getInstance(), this.bedrockClient);
		TunnelMC.mc.setScreen(this.connectScreen);

		InetSocketAddress addressToConnect = new InetSocketAddress(ip, port);
		this.bedrockClient.connect(addressToConnect).whenComplete((session, throwable) -> {
			if (throwable != null) {
				MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().disconnect(
						new DisconnectedScreen(
								new TitleScreen(false),
								Text.of("TunnelMC"),
								Text.of(throwable.getMessage())
						)
				));
				return;
			}

			this.connectScreen.setStatus(Text.of("Logging in..."));
			Client.this.onSessionInitialized(session);
		});

	}

	private int getRandomPort() {
		try (DatagramSocket datagramSocket = new DatagramSocket(0)) {
			return datagramSocket.getLocalPort();
		} catch(SocketException e) {
			throw new RuntimeException("Could not open socket to find next free port", e);
		}
	}

	public void onSessionInitialized(BedrockSession bedrockSession) {
		bedrockSession.setPacketCodec(CODEC);
		bedrockSession.addDisconnectHandler(reason -> MinecraftClient.getInstance().execute(() -> {
			// We disconnected ourselves.
			if (reason == DisconnectReason.DISCONNECTED) {
				return;
			}

			MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().disconnect(
					new DisconnectedScreen(
							new TitleScreen(false),
							Text.of("TunnelMC"),
							Text.of("You were disconnected from the target server because: " + reason.toString())
					)
			));
		}));

		bedrockSession.setBatchHandler(new ClientBatchHandler());
		bedrockSession.setLogging(false);

		try {
			LoginPacket loginPacket = new LoginPacket();

			String clientData = ClientData.getClientData(this.ip + ":" + this.port);
			if (clientData == null) {
				MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().disconnect(
						new DisconnectedScreen(
								new TitleScreen(false),
								Text.of("TunnelMC"),
								Text.of("There was an error when generating client data. Please try again later.")
						)
				));
				return;
			}

			loginPacket.setProtocolVersion(bedrockSession.getPacketCodec().getProtocolVersion());
			loginPacket.setChainData(new AsciiString(chainData.rawData().getBytes()));
			loginPacket.setSkinData(new AsciiString(clientData));
			this.sendPacketImmediately(loginPacket);

			this.connectScreen.setStatus(Text.of("Loading resources..."));

			this.javaConnection = new FakeJavaConnection();
		} catch (Exception e) {
			MinecraftClient.getInstance().execute(() -> MinecraftClient.getInstance().disconnect(
					new DisconnectedScreen(
							new TitleScreen(false),
							Text.of("TunnelMC"),
							Text.of(e.getMessage())
					)
			));
		}
	}

	public void onPlayerInitialized() {
		this.containers = new BedrockContainers();
		this.blockEntityDataCache = new BlockEntityDataCache();
	}

	public boolean isConnectionOpen() {
		return this.bedrockClient != null && this.bedrockClient.getRakNet() != null && this.bedrockClient.getRakNet().isRunning();
	}

	public void sendPacketImmediately(BedrockPacket packet) {
		BedrockSession session = this.bedrockClient.getSession();

		if (session != null) {
			session.sendPacketImmediately(packet);
			if (session.isLogging()) {
				log.info("Outbound {}: {}", session.getAddress().toString(), packet.getClass().getCanonicalName());
			}
		}
	}

	public void sendPacket(BedrockPacket packet) {
		BedrockSession session = this.bedrockClient.getSession();

		if (session != null) {
			session.sendPacket(packet);
			if (session.isLogging()) {
				log.info("Outbound {}: {}", session.getAddress().toString(), packet.getClass().getCanonicalName());
			}
		}
	}

	public int nextRevision() {
		this.revision = this.revision + 1 & Short.MAX_VALUE;
		return this.revision;
	}
}