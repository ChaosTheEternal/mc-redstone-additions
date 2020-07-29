package com.github.chaostheeternal.redstone_additions.tileEntities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import com.github.chaostheeternal.redstone_additions.RedstoneAdditionsMod;
import com.mojang.blaze3d.matrix.MatrixStack;


@OnlyIn(Dist.CLIENT)
public class GlazeContainerTileEntityRenderer extends TileEntityRenderer<GlazeContainerTileEntity> {
	private static final Logger LOGGER = LogManager.getLogger();

	public GlazeContainerTileEntityRenderer(TileEntityRendererDispatcher tileEntityRendererDispatcher) {
		super(tileEntityRendererDispatcher);
		LOGGER.debug("{}::ctor", getClass().getName());
	}

	@Override
	public void render(GlazeContainerTileEntity tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
		BlockState bs = tileEntity.getEmulatedBlockState();
		if (bs == null || bs.getBlock() instanceof net.minecraft.block.AirBlock) {
            return; //TODO: need to do renderBlock with what I tried as the default "blockstate" stuff, a Honey Block-esque without the inner block and maybe recolored
        } else {
            BlockRendererDispatcher brd = Minecraft.getInstance().getBlockRendererDispatcher();
            brd.renderBlock(bs, matrixStack, buffer, 512, combinedOverlay, EmptyModelData.INSTANCE); //Why is this always super dark?  "combinedLight" is NOT a light level
            // If I force combinedLightIn to higher values, it starts to appear "more correctly" in daylight (but not other light), but still not the right light level, and why am I being passed 0?
            // Too high, and it flips back over to rendering in all black (tried 65025)
        }
	}

	@Mod.EventBusSubscriber(modid = RedstoneAdditionsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = { Dist.CLIENT })
	@OnlyIn(Dist.CLIENT)
	public static class Registration {
		@SubscribeEvent
		@OnlyIn(Dist.CLIENT)
		public static void registerClientSetup(final FMLClientSetupEvent e) {
			LOGGER.debug("{}::registerClientSetup", Registration.class.getName());
			ClientRegistry.bindTileEntityRenderer(GlazeContainerTileEntity.TILE_ENTITY, dispatcher -> new GlazeContainerTileEntityRenderer(dispatcher));
		}
	}
}