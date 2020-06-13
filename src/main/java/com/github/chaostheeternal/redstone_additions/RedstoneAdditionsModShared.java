package com.github.chaostheeternal.redstone_additions;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RedstoneAdditionsModShared {
	private static final Logger LOGGER = LogManager.getLogger();

	public RedstoneAdditionsModShared() {
		LOGGER.debug( "{}::ctor", getClass().getName() );
	}

	public void run() {
		LOGGER.debug( "{}::run", getClass().getName() );
	}

	@Mod.EventBusSubscriber(modid = RedstoneAdditionsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class Registration {
		@SubscribeEvent
		public static void registerCommonSetup(FMLCommonSetupEvent e) {
			LOGGER.debug( "{}::registerCommonSetup", Registration.class.getName() );
			RedstoneAdditionsModChannel.INSTANCE.initialize();
		}
	}
}
