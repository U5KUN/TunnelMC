package me.THEREALWWEFAN231.tunnelmc.connection.bedrock.network.translators;

import com.nukkitx.protocol.bedrock.data.GameRuleData;
import com.nukkitx.protocol.bedrock.packet.ChunkRadiusUpdatedPacket;
import com.nukkitx.protocol.bedrock.packet.PlayStatusPacket;
import com.nukkitx.protocol.bedrock.packet.RequestChunkRadiusPacket;
import com.nukkitx.protocol.bedrock.packet.StartGamePacket;
import me.THEREALWWEFAN231.tunnelmc.TunnelMC;
import me.THEREALWWEFAN231.tunnelmc.connection.bedrock.BedrockConnection;
import me.THEREALWWEFAN231.tunnelmc.connection.java.FakeJavaConnection;
import me.THEREALWWEFAN231.tunnelmc.events.PlayerInitializedEvent;
import me.THEREALWWEFAN231.tunnelmc.translator.dimension.Dimension;
import me.THEREALWWEFAN231.tunnelmc.translator.gamemode.GameModeTranslator;
import me.THEREALWWEFAN231.tunnelmc.translator.packet.PacketIdentifier;
import me.THEREALWWEFAN231.tunnelmc.translator.packet.PacketTranslator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.ChunkRenderDistanceCenterS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@PacketIdentifier(StartGamePacket.class)
public class StartGameTranslator extends PacketTranslator<StartGamePacket> {

	@Override
	public void translate(StartGamePacket packet, BedrockConnection bedrockConnection, FakeJavaConnection javaConnection) {
		bedrockConnection.runtimeId = packet.getRuntimeEntityId();
		bedrockConnection.uniqueId = packet.getUniqueEntityId();

		GameMode gameMode = GameModeTranslator.bedrockToJava(packet.getPlayerGameType());

		Dimension dimension = Dimension.getDimensionFromId(packet.getDimensionId()).orElse(Dimension.OVERWORLD);
		RegistryKey<DimensionType> dimensionRegistryKey = dimension.getDimensionRegistryKey();
		RegistryKey<World> worldRegistryKey = dimension.getWorldRegistryKey();

		long seed = packet.getSeed();
		int maxPlayers = 999;
		int chunkLoadDistance = 3;
		boolean showDeathScreen = true;

		for (GameRuleData<?> gameRule : packet.getGamerules()) {
			if (gameRule.getName().equals("doimmediaterespawn")) {
				showDeathScreen = !((Boolean) gameRule.getValue());
				break;
			}
		}

		Set<RegistryKey<World>> dimensionIds = Stream.of(World.OVERWORLD, World.NETHER, World.END).collect(Collectors.toSet());

		GameJoinS2CPacket gameJoinS2CPacket = new GameJoinS2CPacket((int) packet.getRuntimeEntityId(), false, gameMode, gameMode, dimensionIds, DynamicRegistryManager.BUILTIN.get(), dimensionRegistryKey, worldRegistryKey, seed, maxPlayers, chunkLoadDistance, chunkLoadDistance, false, showDeathScreen, false, false, Optional.empty());
		javaConnection.processJavaPacket(gameJoinS2CPacket);

		TunnelMC.getInstance().getEventManager().fire(new PlayerInitializedEvent());
		bedrockConnection.movementMode = packet.getPlayerMovementSettings().getMovementMode();

		// TODO: Send a complete SynchronizeTagsS2CPacket so that water can work.

		MinecraftClient.getInstance().execute(() -> GameRulesChangedTranslator.onGameRulesChanged(packet.getGamerules()));

		int chunkX = MathHelper.floor(packet.getPlayerPosition().getX()) >> 4;
		int chunkZ = MathHelper.floor(packet.getPlayerPosition().getZ()) >> 4;

		ChunkRenderDistanceCenterS2CPacket chunkRenderDistanceCenterS2CPacket = new ChunkRenderDistanceCenterS2CPacket(chunkX, chunkZ);
		javaConnection.processJavaPacket(chunkRenderDistanceCenterS2CPacket);

		RequestChunkRadiusPacket requestChunkRadiusPacket = new RequestChunkRadiusPacket();
		requestChunkRadiusPacket.setRadius(TunnelMC.mc.options.getViewDistance().getValue());
		bedrockConnection.sendPacketImmediately(requestChunkRadiusPacket);
		bedrockConnection.expect(ChunkRadiusUpdatedPacket.class, PlayStatusPacket.class);
	}
}
