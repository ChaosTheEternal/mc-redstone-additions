package com.github.chaostheeternal.redstone_additions;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;

import java.util.function.Supplier;

import com.github.chaostheeternal.redstone_additions.blocks.InstantOffRepeaterBlock;
import com.github.chaostheeternal.redstone_additions.blocks.RedstoneInverterBlock;

@OnlyIn(Dist.CLIENT)
public class RedstoneAdditionsModClient {
	private static final Logger LOGGER = LogManager.getLogger();

	private Minecraft _minecraft;

	public RedstoneAdditionsModClient(Supplier<Minecraft> minecraftSupplier) {
		LOGGER.debug( "{}::ctor", getClass().getName() );
		assert(FMLEnvironment.dist == Dist.CLIENT);
		_minecraft = minecraftSupplier.get();
		assert( _minecraft != null);
	}

	public void run() {
		LOGGER.debug( "{}::run", getClass().getName() );
	}

	@Mod.EventBusSubscriber(modid = RedstoneAdditionsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = { Dist.CLIENT })
	@OnlyIn(Dist.CLIENT)
	public static class Registration {
		@SubscribeEvent
		public static void registerClientSetup(FMLClientSetupEvent e) {
			LOGGER.debug( "{}::registerClientSetup", Registration.class.getName() );
		}
		
		@SubscribeEvent
		public static void onClientSetupEvent(FMLClientSetupEvent event) {
			RenderTypeLookup.setRenderLayer(RedstoneInverterBlock.BLOCK, RenderType.getCutout());
			RenderTypeLookup.setRenderLayer(InstantOffRepeaterBlock.BLOCK, RenderType.getCutout());
			Minecraft.getInstance().getBlockColors().register((state, world, pos, tint) -> { return InstantOffRepeaterBlock.lineColorMultiplier(state.get(InstantOffRepeaterBlock.POWERED)); }, InstantOffRepeaterBlock.BLOCK);
		}
	}
}
