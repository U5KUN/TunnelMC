package me.THEREALWWEFAN231.tunnelmc.mixins;

import com.nukkitx.protocol.bedrock.data.SoundEvent;
import com.nukkitx.protocol.bedrock.packet.LevelSoundEventPacket;
import me.THEREALWWEFAN231.tunnelmc.connection.bedrock.BedrockConnectionAccessor;
import me.THEREALWWEFAN231.tunnelmc.translator.blockstate.BlockPaletteTranslator;
import me.THEREALWWEFAN231.tunnelmc.utils.PositionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionHandler {
	@Shadow private BlockPos currentBreakingPos;

	@Shadow @Final private MinecraftClient client;

	@Redirect(method = "updateBlockBreakingProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundManager;play(Lnet/minecraft/client/sound/SoundInstance;)V"))
	public void cancelBlockSound(SoundManager soundManager, SoundInstance sound) {
		if (!BedrockConnectionAccessor.isConnectionOpen()) {
			soundManager.play(sound);
			return;
		}

		LevelSoundEventPacket packet = new LevelSoundEventPacket();
		packet.setSound(SoundEvent.HIT);
		packet.setPosition(PositionUtils.toBedrockVector3f(this.currentBreakingPos));
		packet.setExtraData(BlockPaletteTranslator.BLOCK_STATE_TO_RUNTIME_ID.getInt(this.client.world.getBlockState(this.currentBreakingPos)));
		packet.setIdentifier("");
		BedrockConnectionAccessor.getCurrentConnection().sendPacket(packet);
	}
}
