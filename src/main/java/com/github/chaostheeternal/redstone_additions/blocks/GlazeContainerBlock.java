package com.github.chaostheeternal.redstone_additions.blocks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.chaostheeternal.redstone_additions.RedstoneAdditionsMod;
import com.github.chaostheeternal.redstone_additions.items.GlazeContainerItem;
import com.github.chaostheeternal.redstone_additions.tileEntities.GlazeContainerTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

public class GlazeContainerBlock extends ContainerBlock {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String REGISTRY_NAME = "glaze_container_block";
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(RedstoneAdditionsMod.MOD_ID, REGISTRY_NAME);
    public static final RegistryObject<GlazeContainerBlock> REGISTRY_OBJECT = RegistryObject.of(RESOURCE_LOCATION, ForgeRegistries.BLOCKS);
    public static final Material GLAZE = (new Material.Builder(MaterialColor.GRAY)).build();
    public static final GlazeContainerBlock BLOCK = new GlazeContainerBlock(Block.Properties.create(GLAZE).hardnessAndResistance(0.0F).sound(SoundType.SLIME));

    //TODO: Consider reworking the "render" piece based off of TheGreyGhost's MBE04 example
    //  Problem with that is, that examples renders a model based off of what is adjacent, whereas mine renders based on the block stored
    //TODO: Need the "transparent" logic so it doesn't hide edges of blocks it's attached to
    //  I thought the RenderType.getTranslucent() would've handled that
    //TODO: Can I change the tool requirements based on the block it's emulating?
    //TODO: Need to handle facing on placement and/or when the block it will contain is placed (if possible and it would be preferred)

    public GlazeContainerBlock(Properties properties) {
        super(properties);
        this.setRegistryName(RESOURCE_LOCATION);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH).with(FILLED, false));
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(FACING, FILLED);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        if (state.get(FILLED)) {
            return BlockRenderType.INVISIBLE; //leave how it appears to the render method
        } else {
            return BlockRenderType.MODEL; //Not filled, use the blockstate model stuff
        }        
    }

    public Block getEmulatedBlock(IBlockReader world, BlockPos pos) {
        if (world instanceof IWorld) {
			net.minecraft.world.chunk.AbstractChunkProvider provider = ((IWorld)world).getChunkProvider();
			if (!provider.isChunkLoaded(new ChunkPos(pos))) {
				LOGGER.error("{}::getEmulatedBlock chunk not loaded: {}", getClass().getName(), pos);
				return Blocks.AIR;
			}
        }
        TileEntity te = world.getTileEntity(pos);
        if (te == null || !(te instanceof GlazeContainerTileEntity)) return Blocks.AIR;
        Block b = ((GlazeContainerTileEntity)te).getEmulatedBlock();
        if (b == this) return Blocks.AIR;
        return b;
    }

    public BlockState getEmulatedBlockState(IBlockReader world, BlockPos pos) {
        if (world instanceof IWorld) {
            net.minecraft.world.chunk.AbstractChunkProvider provider = ((IWorld)world).getChunkProvider();
            if (!provider.canTick(pos)) {
                LOGGER.error("{}::getEmulatedBlockState chunk not loaded: {}", getClass().getName(), pos);
                return Blocks.AIR.getDefaultState();
            }
        }
        TileEntity te = world.getTileEntity(pos);
        if (te == null || !(te instanceof GlazeContainerTileEntity)) return Blocks.AIR.getDefaultState();
        BlockState emulatedBlockState = ((GlazeContainerTileEntity)te).getEmulatedBlockState();
        if (emulatedBlockState == null || emulatedBlockState.getBlock() == this) return Blocks.AIR.getDefaultState();
        return emulatedBlockState;
    }

    @Override
    public float getBlockHardness(BlockState state, IBlockReader worldIn, BlockPos pos) {
        if (state.get(FILLED)) {
            return getEmulatedBlockState(worldIn, pos).getBlockHardness(worldIn, pos);
        } else {
            return 0;
        }
    }

    @Override
    public float getExplosionResistance(BlockState state, IWorldReader world, BlockPos pos, Entity exploder, Explosion explosion) {
        if (state.get(FILLED)) {
            return getEmulatedBlockState(world, pos).getExplosionResistance(world, pos, exploder, explosion);
        } else {
            return 0;
        }
    }
    // PROBLEM: Can't change HarvestTool based on the emulated block since I don't get world/pos info
    // Will I have to change it so if you "break" this block it places the contained block in its place?
    // In that case, it seems like I'd need to do it both ways, placing this "replaces" an existing block, and that might not be feasible
    // Is there any way for me to have all those methods use the emulated block?  We don't store that at this level (and not sure if I can)
    //  and I can't get the tileEntity without world and pos, which not all methods take... like getHarvestTool

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader worldIn) {
        return createNewTileEntity(worldIn);
    }
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return new GlazeContainerTileEntity();
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState stateIn, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) return ActionResultType.SUCCESS;
        if (stateIn.get(FILLED)) return ActionResultType.PASS;
        INamedContainerProvider incp = this.getContainer(stateIn, worldIn, pos);
        if (incp != null && incp instanceof GlazeContainerTileEntity) { 
            GlazeContainerTileEntity te = (GlazeContainerTileEntity)incp;
            // I get the feeling I should have extra checks in this section to prevent NREs
            ItemStack stack = player.getHeldItem(handIn);
            Block block = Block.getBlockFromItem(stack.getItem());
            if (te.canPlayerAccessInventory(player) && !stack.isEmpty() && stack.getItem() instanceof BlockItem && !(block instanceof ContainerBlock) && block.getDefaultState().isSolid()) {
                te.addBlockToContainer(stack, block);
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof GlazeContainerTileEntity) ((GlazeContainerTileEntity)te).dropAllContents(worldIn, pos);
        }
    }

    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.PUSH_ONLY;
    }    
    
    @Mod.EventBusSubscriber(modid = RedstoneAdditionsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerBlock(final RegistryEvent.Register<Block> e) {
            LOGGER.debug("{}::registerBlock", Registration.class.getName());
            e.getRegistry().register(BLOCK);
        }
    }
}