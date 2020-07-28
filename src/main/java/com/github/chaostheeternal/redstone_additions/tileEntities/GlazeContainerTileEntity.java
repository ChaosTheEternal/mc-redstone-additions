package com.github.chaostheeternal.redstone_additions.tileEntities;

import com.github.chaostheeternal.redstone_additions.RedstoneAdditionsMod;
import com.github.chaostheeternal.redstone_additions.blocks.GlazeContainerBlock;
import com.github.chaostheeternal.redstone_additions.inventory.GlazeContainerContainer;
import com.github.chaostheeternal.redstone_additions.items.GlazeContainerItem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//NOTE: Do I need to use LockableTileEntity?  Or should I try to do this "from scratch" with TileEntity and implement INamedContainerProvider?
public class GlazeContainerTileEntity extends LockableTileEntity {
	private static final Logger LOGGER = LogManager.getLogger();
    public static final String REGISTRY_NAME = "glaze_container_entity_type";
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(RedstoneAdditionsMod.MOD_ID, REGISTRY_NAME);
    public static final int CONTAINER_SIZE = 1;
    public static final String CONTAINER_TAG = "emulated_block";

    public static TileEntityType<GlazeContainerTileEntity> TILE_ENTITY = TileEntityType.Builder.create(GlazeContainerTileEntity::new, GlazeContainerBlock.BLOCK).build(null);
    static {
        TILE_ENTITY.setRegistryName(RESOURCE_LOCATION);
    }

	private boolean _updatingEmulatedBlock = false;
    private Block _emulatedBlock = Blocks.AIR;
	private BlockState _emulatedBlockState = Blocks.AIR.getDefaultState();
	private NonNullList<ItemStack> _contents;

    public GlazeContainerTileEntity() {
        super(TILE_ENTITY);
    }

	public boolean canPlayerAccessInventory(PlayerEntity player) {
		return world.getTileEntity(pos) == this && !(player.getDistanceSq((double)pos.getX() + .5D, (double)pos.getY() + .5D, (double)pos.getZ() + .5D) > 64.D);
    }    
    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return canPlayerAccessInventory(player);
    }
    @Override
    protected Container createMenu(int windowId, PlayerInventory playerInventory) {
		return new GlazeContainerContainer(windowId, playerInventory, this);
    }    

	@Override
	public int getSizeInventory() {
		return CONTAINER_SIZE;
	}

	@Override
	public boolean isEmpty() {
		for(ItemStack itemStack : _contents) {
			if(!itemStack.isEmpty()) return false;
		}
		return true;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}


    

	public void refreshEmulatedBlock() {
		// LOGGER.debug("{}::refreshEmulatedBlock", getClass().getName());
		ItemStack itemStack = _contents.get(0);
		Block block = Blocks.AIR;
		if(!itemStack.isEmpty()) block = Block.getBlockFromItem(itemStack.getItem());
		setEmulatedBlock(block);
	}

	public void setEmulatedBlock(Block block) {
		_updatingEmulatedBlock = true;
		assert(world != null);
		assert(block != null);
		// LOGGER.debug("{}::setEmulatedBlock {}", getClass().getName(), block.getRegistryName());
		if(!isLoaded()) {
			LOGGER.error("{}::setEmulatedBlock chunk not loaded", getClass().getName());
			_updatingEmulatedBlock = false;
			return;
		}
		_emulatedBlock = block;
		_emulatedBlockState = null;
		markDirty();
		if(world.isRemote()) {
			_updatingEmulatedBlock = false;
			return;
		}
		BlockState oldState = getBlockState();
		BlockState newState = oldState.with(GlazeContainerBlock.FILLED, !(block instanceof AirBlock));
		world.markBlockRangeForRenderUpdate(pos, oldState, newState);
		world.setBlockState(pos, newState, BlockFlags.DEFAULT_AND_RERENDER | BlockFlags.IS_MOVING);
		_updatingEmulatedBlock = false;
	}

	public Block getEmulatedBlock() {
		return _emulatedBlock;
	}

	private boolean isLoaded() {
		if(world == null) return false;
		if(world.restoringBlockSnapshots) return false;
		if(!world.getChunkProvider().isChunkLoaded(new ChunkPos(pos))) return false;
		return true;
    }

	@SuppressWarnings("unchecked")
	public BlockState getEmulatedBlockState() {
		if(_emulatedBlockState == null && _emulatedBlock != null) {
			BlockState blockState = getBlockState();
			if(blockState != null) {
				BlockState emulatedBlockState = _emulatedBlock.getDefaultState();
				String facingName = GlazeContainerBlock.FACING.getName();
				Direction facingValue = blockState.get(GlazeContainerBlock.FACING);
				for(IProperty<?> property : emulatedBlockState.getProperties()) {
					String propertyName = property.getName();
					// LOGGER.debug("{}::getEmulatedBlockState {}", getClass().getName(), property);
					if(facingName.equals(propertyName)) {
						if(property.getAllowedValues().contains(facingValue)) {
							emulatedBlockState = emulatedBlockState.with((IProperty<Direction>)property, facingValue);
						}
					}
				}
				_emulatedBlockState = emulatedBlockState;
			}
		}
		return _emulatedBlockState;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
        assert(index == 0);
        return stack.isEmpty() || (stack.getItem() instanceof BlockItem && !(stack.getItem() instanceof GlazeContainerItem));
	}

    @Override
    public ItemStack getStackInSlot(int index) {
        return _contents.get(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        // TODO Auto-generated method stub

    }

    @Override
    public void clear() {
        // TODO Auto-generated method stub

    }

    @Override
    protected ITextComponent getDefaultName() {
        // TODO Auto-generated method stub
        return null;
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
