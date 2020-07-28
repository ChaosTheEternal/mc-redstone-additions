package com.github.chaostheeternal.redstone_additions.inventory;

import com.github.chaostheeternal.redstone_additions.RedstoneAdditionsMod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class GlazeContainerContainerType extends ContainerType<GlazeContainerContainer> {
    private static Logger LOGGER = LogManager.getLogger();
	public static final String REGISTRY_NAME = "glaze_container_container_type";
	public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(RedstoneAdditionsMod.MOD_ID, REGISTRY_NAME);
	public static final GlazeContainerContainerType CONTAINER_TYPE = new GlazeContainerContainerType();

    public GlazeContainerContainerType() {
        super(GlazeContainerContainer::create);
		setRegistryName(RESOURCE_LOCATION);
    }

	@Mod.EventBusSubscriber(modid = RedstoneAdditionsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class Registration {
		@SubscribeEvent
		public static void registerContainerType( final RegistryEvent.Register<ContainerType<?>> e ) {
			LOGGER.debug( "{}::registerContainerType", Registration.class.getName() );
			e.getRegistry().register( CONTAINER_TYPE );
		}
	}
}