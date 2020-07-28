package com.github.chaostheeternal.redstone_additions.inventory;

import java.util.function.Predicate;

import com.github.chaostheeternal.redstone_additions.items.GlazeContainerItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.items.ItemStackHandler;

public class GlazeContainerInventory implements IInventory {
    private final ItemStackHandler contents;
    private Predicate<PlayerEntity> canPlayerAccessInventoryLambda = x-> true;
    private Notify markDirtyNotificationLambda = ()->{};
    
    @FunctionalInterface
    public interface Notify {
        void invoke();
    }

    public static GlazeContainerInventory createForTileEntity(Predicate<PlayerEntity> canPlayerAccessInventoryLambda, Notify markDirtyNotificationLambda) {
        return new GlazeContainerInventory(canPlayerAccessInventoryLambda, markDirtyNotificationLambda);
    }
    public static GlazeContainerInventory createForClientSideContainer() {
        return new GlazeContainerInventory();
    }
    private GlazeContainerInventory() {
        this.contents = new ItemStackHandler(1);
    }
    private GlazeContainerInventory(Predicate<PlayerEntity> canPlayerAccessInventoryLambda, Notify markDirtyNotificationLambda) {
        this.contents = new ItemStackHandler(1);
        this.canPlayerAccessInventoryLambda = canPlayerAccessInventoryLambda;
        this.markDirtyNotificationLambda = markDirtyNotificationLambda;
    }
    public void setCanPlayerAccessInventoryLambda(Predicate<PlayerEntity> canPlayerAccessInventoryLambda) {
        this.canPlayerAccessInventoryLambda = canPlayerAccessInventoryLambda;
    }
    public void setMarkDirtyNotificationLambda(Notify markDirtyNotificationLambda) {
        this.markDirtyNotificationLambda = markDirtyNotificationLambda;
    }

    public CompoundNBT serializeNBT() {
        return contents.serializeNBT();
    }
    public void deserializeNBT(CompoundNBT nbt) {
        contents.deserializeNBT(nbt);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof BlockItem && !(stack.getItem() instanceof GlazeContainerItem) && contents.isItemValid(slot, stack);
    }

    @Override
    public void clear() {
        contents.setStackInSlot(0, ItemStack.EMPTY);
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        return contents.extractItem(slot, count, false);
    }

    @Override
    public int getSizeInventory() {
        return contents.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return contents.getStackInSlot(slot);
    }

    @Override
    public boolean isEmpty() {
        return contents.getStackInSlot(0).isEmpty(); //This only ever has one slot
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return canPlayerAccessInventoryLambda.test(player);
    }

    @Override
    public void markDirty() {
        markDirtyNotificationLambda.invoke();
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        return contents.extractItem(slot, 1, false); //The one slot can hold only a single item
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (stack.getCount() == 1) {
            contents.setStackInSlot(slot, stack);
        } else {
            ItemStack copy = stack.copy();
            copy.setCount(1);
            contents.setStackInSlot(slot, copy);
        }
        stack.shrink(1);
    }
}