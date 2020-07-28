package com.github.chaostheeternal.redstone_additions.inventory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;

public class GlazeContainerContainer extends Container {
	private static final Logger LOGGER = LogManager.getLogger();
	public static int SLOTS = 1;
	private final IInventory _inventory;

    public static GlazeContainerContainer create(int windowId, PlayerInventory playerInventory) {
        return new GlazeContainerContainer(windowId, playerInventory, new Inventory(SLOTS));
    }

    public GlazeContainerContainer(int windowId, PlayerInventory playerInventory, IInventory inventory) {
        super(GlazeContainerContainerType.CONTAINER_TYPE, windowId);
		LOGGER.debug("{}::ctor", getClass().getName());
		_inventory = inventory;
		_inventory.openInventory(playerInventory.player);
		addSlot(new GlazeContainerSlot(inventory, 0, 12 + 4 * 18, 8 + 2 * 18));
		final int XSIZE = 184;
		final int YSIZE = 184;
		int leftCol = (XSIZE - 162) / 2 + 1;
		for(int playerInvRow = 0; playerInvRow < 3; ++playerInvRow) {
			for(int playerInvCol = 0; playerInvCol < 9; ++playerInvCol) {
				addSlot(new Slot(playerInventory, playerInvCol + playerInvRow * 9 + 9, leftCol + playerInvCol * 18, YSIZE - (4 - playerInvRow) * 18 - 10));
			}
		}
		for(int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
			addSlot(new Slot(playerInventory, hotbarSlot, leftCol + hotbarSlot * 18, YSIZE - 24));
		}
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return false; //TODO: Get the actual check
    }
    
    //TODO: Finish implementation
}