package com.github.chaostheeternal.redstone_additions.tileEntities;

import javax.annotation.Nullable;

import com.github.chaostheeternal.redstone_additions.RedstoneAdditionsMod;
import com.github.chaostheeternal.redstone_additions.blocks.GlazeContainerBlock;
import com.github.chaostheeternal.redstone_additions.inventory.GlazeContainerInventory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class GlazeContainerTileEntity extends TileEntity implements INamedContainerProvider {
	private static final Logger LOGGER = LogManager.getLogger();
    public static final String REGISTRY_NAME = "glaze_container_entity_type";
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(RedstoneAdditionsMod.MOD_ID, REGISTRY_NAME);
    public static final int CONTAINER_SIZE = 1;
    public static final String CONTAINER_TAG = "emulated_block";

    public static TileEntityType<GlazeContainerTileEntity> TILE_ENTITY = TileEntityType.Builder.create(GlazeContainerTileEntity::new, GlazeContainerBlock.BLOCK).build(null);
    static {
        TILE_ENTITY.setRegistryName(RESOURCE_LOCATION);
    }

	private final GlazeContainerInventory emulatedBlock;
	
    public GlazeContainerTileEntity() {
		super(TILE_ENTITY);
		emulatedBlock = GlazeContainerInventory.createForTileEntity(this::canPlayerAccessInventory, this::markDirty);
    }

	@Override
	public Container createMenu(int arg0, PlayerInventory arg1, PlayerEntity arg2) {
		return null; //Menu won't show, so this isn't important
	}

	@Override
	public ITextComponent getDisplayName() {
		return null; //Menu won't show, so this isn't important
	}

	public boolean canPlayerAccessInventory(PlayerEntity player) {
		return world.getTileEntity(pos) == this && !(player.getDistanceSq((double)pos.getX() + .5D, (double)pos.getY() + .5D, (double)pos.getZ() + .5D) > 64.D);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		CompoundNBT inventory = emulatedBlock.serializeNBT();
		compound.put(CONTAINER_TAG, inventory);
		return compound;
	}
	@Override
	public void read(CompoundNBT compound) {
		super.read(compound);
		CompoundNBT inventory = compound.getCompound(CONTAINER_TAG);
		emulatedBlock.deserializeNBT(inventory);
		if (emulatedBlock.getSizeInventory() != CONTAINER_SIZE) {
			LOGGER.error("{}::read has wrong size (got {} expected {})", getClass().getName(), emulatedBlock.getSizeInventory(), CONTAINER_SIZE);
			emulatedBlock.clear();
		}
		loadEmulatedBlock();
	}
	@Override
	@Nullable
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT compound = new CompoundNBT();
		write(compound);
		return new SUpdateTileEntityPacket(this.pos, 0, compound);
	}
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		read(pkt.getNbtCompound());
	}
	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT compound = new CompoundNBT();
		write(compound);
		return compound;
	}
	@Override
	public void handleUpdateTag(CompoundNBT tag) {
		this.read(tag);
	}

	public void dropAllContents(World worldIn, BlockPos pos) {
		InventoryHelper.dropInventoryItems(worldIn, pos, emulatedBlock);
	}

	public void addBlockToContainer(ItemStack stack, Block block) {
		emulatedBlock.setInventorySlotContents(0, stack);
		setEmulatedBlock(block);
	}

	private Block _emulatedBlock = Blocks.AIR;
	public void loadEmulatedBlock() {
		ItemStack stack = emulatedBlock.getStackInSlot(0);
		_emulatedBlock = Block.getBlockFromItem(stack.getItem());
	}
	private void setEmulatedBlock(Block block) {
		_emulatedBlock = block;
		BlockState oldState = getBlockState();
		BlockState newState = oldState.with(GlazeContainerBlock.FILLED, true);
		//world.markBlockRangeForRenderUpdate(pos, oldState, newState); //Do I need this?
		world.setBlockState(pos, newState, BlockFlags.DEFAULT); //Why doesn't this tell the client to re-calculate light level?  It works right on quit and load, but not on placement
		// Looking at TheGreyGhost's furnace example, he would change the state inside a method in the TileEntity and calls setBlockState and markDirty right after... and then the block's getLightValue should calculate based on new information
		// I know the getLightLevel correctly updated since it was logging that the getLightLevel calls with Sea Lanterns were returning 15...
		// A part of me thinks it has something to do with the customized `render` method...
		markDirty();
	}	
	public Block getEmulatedBlock() {
		return _emulatedBlock;
	}
	public BlockState getEmulatedBlockState() {
		return _emulatedBlock.getDefaultState();
	}

	@Mod.EventBusSubscriber(modid = RedstoneAdditionsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class Registration {
		@SubscribeEvent
		public static void registerTileEntityType(final RegistryEvent.Register<TileEntityType<?>> e) {
			LOGGER.debug("{}::registerTileEntityType", Registration.class.getName());
			e.getRegistry().register(TILE_ENTITY);
		}
	}
}
