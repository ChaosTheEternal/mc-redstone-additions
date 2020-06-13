package com.github.chaostheeternal.redstone_additions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class RedstoneAdditionsModChannel {
	private static final Logger LOGGER = LogManager.getLogger();
	private RedstoneAdditionsModChannel() {}
	public static final RedstoneAdditionsModChannel INSTANCE = new RedstoneAdditionsModChannel();
	private static final String CHANNEL_NAME = "main";
	private static final String PROTOCOL_VERSION = "1";

	private SimpleChannel _channel;

	public void initialize() {
		LOGGER.debug( "{}::initialize", getClass().getName() );
		assert( _channel == null);
		_channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(RedstoneAdditionsMod.MOD_ID, CHANNEL_NAME), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	}
}
