package me.THEREALWWEFAN231.tunnelmc.translator.container.screenhandler.translators;

import me.THEREALWWEFAN231.tunnelmc.connection.bedrock.BedrockConnectionAccessor;
import me.THEREALWWEFAN231.tunnelmc.connection.bedrock.network.caches.container.BedrockContainers;
import me.THEREALWWEFAN231.tunnelmc.translator.container.screenhandler.ScreenHandlerTranslator;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;

public class GenericContainerScreenHandlerTranslator extends ScreenHandlerTranslator<GenericContainerScreenHandler> {

	@Override
	public Integer getBedrockContainerId(GenericContainerScreenHandler javaContainer, int javaSlotId) {
		int slotsInContainer = javaContainer.getRows() * 9;

		if (javaSlotId < slotsInContainer) {
			return BedrockConnectionAccessor.getCurrentConnection().getWrappedContainers().getCurrentlyOpenContainerId();
		}

		return BedrockContainers.PLAYER_INVENTORY_COTNAINER_ID;
	}

	@Override
	public int getBedrockSlotId(GenericContainerScreenHandler javaContainer, int javaSlotId) {
		int slotsInContainer = javaContainer.getRows() * 9;
		if (javaSlotId < slotsInContainer) {//the ids are the same in java and bedrock for chest containers
			return javaSlotId;
		}

		javaSlotId -= slotsInContainer;

		if (javaSlotId > 26) {//hotbar
			return javaSlotId - 27;
		}

		return javaSlotId + 9;
	}

	@Override
	public Class<? extends ScreenHandler> getScreenHandlerClass() {
		return GenericContainerScreenHandler.class;
	}

}
