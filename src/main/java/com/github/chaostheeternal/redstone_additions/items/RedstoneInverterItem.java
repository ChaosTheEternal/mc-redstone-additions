package com.github.chaostheeternal.redstone_additions.items;

import com.github.chaostheeternal.redstone_additions.RedstoneAdditionsMod;
import com.github.chaostheeternal.redstone_additions.blocks.RedstoneInverterBlock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

public class RedstoneInverterItem extends BlockItem {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String REGISTRY_NAME = "redstone_inverter_item";
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(RedstoneAdditionsMod.MOD_ID, REGISTRY_NAME);
    public static final RegistryObject<RedstoneInverterItem> REGISTRY_OBJECT = RegistryObject.of(RESOURCE_LOCATION, ForgeRegistries.ITEMS);
    public static final RedstoneInverterItem ITEM = new RedstoneInverterItem();

    private RedstoneInverterItem() {
        super(RedstoneInverterBlock.BLOCK, new Properties().maxStackSize(64).group(ItemGroup.REDSTONE));
        this.setRegistryName(RESOURCE_LOCATION);
    }

	@Mod.EventBusSubscriber(modid = RedstoneAdditionsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class Registration {
		@SubscribeEvent
		public static void registerItem(final RegistryEvent.Register<Item> e) {
			LOGGER.debug("{}::registerItem", Registration.class.getName());
			e.getRegistry().register(ITEM);
		}
	}
    
}