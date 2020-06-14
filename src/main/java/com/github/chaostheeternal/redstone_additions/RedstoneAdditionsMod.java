package com.github.chaostheeternal.redstone_additions;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(RedstoneAdditionsMod.MOD_ID)
public class RedstoneAdditionsMod {
    public static final String MOD_ID = "redstone_additions";
    public static final String VERSION = "0.3";
	private static final Logger LOGGER = LogManager.getLogger();

	public RedstoneAdditionsMod() {
		LOGGER.debug( "{}::ctor", getClass().getName() );
		MinecraftForge.EVENT_BUS.register(this);
	}
}