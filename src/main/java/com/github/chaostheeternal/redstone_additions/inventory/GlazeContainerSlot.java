package com.github.chaostheeternal.redstone_additions.inventory;

import com.github.chaostheeternal.redstone_additions.items.GlazeContainerItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GlazeContainerSlot extends Slot {
	private static final Logger LOGGER = LogManager.getLogger();

	public GlazeContainerSlot(IInventory inventory, int slotIndex, int xPos, int yPos) {
		super(inventory, slotIndex, xPos, yPos);
		LOGGER.debug("{}::ctor", getClass().getName());
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		Item item = stack.getItem();
		return item instanceof BlockItem && !(item instanceof GlazeContainerItem);
	}
}