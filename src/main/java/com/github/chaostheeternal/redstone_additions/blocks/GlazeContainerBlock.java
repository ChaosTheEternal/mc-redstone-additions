package com.github.chaostheeternal.redstone_additions.blocks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.chaostheeternal.redstone_additions.RedstoneAdditionsMod;
import com.github.chaostheeternal.redstone_additions.tileEntities.GlazeContainerTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
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
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    //TODO: Any property so it can present the block ID of what it is imitating?
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(RedstoneAdditionsMod.MOD_ID, REGISTRY_NAME);
    public static final RegistryObject<GlazeContainerBlock> REGISTRY_OBJECT = RegistryObject.of(RESOURCE_LOCATION, ForgeRegistries.BLOCKS);
    public static final GlazeContainerBlock BLOCK = new GlazeContainerBlock(Block.Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0F).sound(SoundType.SLIME));

    public GlazeContainerBlock(Properties properties) {
        super(properties);
        this.setRegistryName(RESOURCE_LOCATION);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH).with(FILLED, false));
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(FACING, FILLED);
    }

    // TODO: Figure this out from error's stuff later, I at least want to get the empty model and "chest" piece working first
    // // TODO: How would I emulate an empty one, then?
    // public Block getEmulatedBlock(IBlockReader world, BlockPos pos) {
    //     if (world instanceof IWorld) {
	// 		net.minecraft.world.chunk.AbstractChunkProvider provider = ((IWorld)world).getChunkProvider();
	// 		if( !provider.isChunkLoaded( new ChunkPos( pos ) ) ) {
	// 			LOGGER.error( "{}::getEmulatedBlock chunk not loaded: {}", getClass().getName(), pos );
	// 			return Blocks.AIR;
	// 		}
    //     }
    //     TileEntity te = world.getTileEntity(pos);
    //     if (te == null || !(te instanceof GlazeContainerTileEntity)) return Blocks.AIR;
    //     Block b = te.getEmulatedBlock();
    //     if (b == this) return Blocks.AIR;
    //     return b;
    // }

    // public BlockState getEmulatedBlockState(IBlockReader world, BlockPos pos) {
    //     if (world instanceof IWorld) {
    //         net.minecraft.world.chunk.AbstractChunkProvider provider = ((IWorld)world).getChunkProvider();
    //         if (!provider.canTick(pos)) {
    //             LOGGER.error("{}::getEmulatedBlockState chunk not loaded: {}", getClass().getName(), pos);
    //             return Blocks.AIR.getDefaultState();
    //         }
    //     }
    //     TileEntity te = world.getTileEntity(pos);
    //     if (te == null || !(te instanceof GlazeContainerTileEntity)) return Blocks.AIR.getDefaultState();
    //     BlockState emulatedBlockState = te.getEmulatedBlockState();
    //     if (emulatedBlockState == null || emulatedBlockState.getBlock() == this) return Blocks.AIR.getDefaultState();
    //     return emulatedBlockState;
    // }

    // @Override
    // public TileEntity createTileEntity(BlockState state, IBlockReader worldIn) {
    //     return createNewTileEntity(worldIn);
    // }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return null; //new GlazeContainerTileEntity();
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return false;
    }
    @Override
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState stateIn, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote || stateIn.get(FILLED)) return ActionResultType.SUCCESS; //ignore right-clicks if this is already filled
        INamedContainerProvider incp = this.getContainer(stateIn, worldIn, pos);
        if (incp != null) player.openContainer(incp);
        return ActionResultType.SUCCESS;
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