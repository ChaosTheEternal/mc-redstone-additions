package com.github.chaostheeternal.redstone_additions.blocks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.chaostheeternal.redstone_additions.RedstoneAdditionsMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
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

    // TODO: How would I emulate an empty one, then?
    // public Block getEmulatedBlock(IBlockReader world, BlockPos pos) {
    // if (world instanceof IWorld) {
    // val provider = ((IWorld)world).getChunkSource();
    // if (!provider.isEntityTickingChunk(new ChunkPos(pos))) {
    // LOGGER.error("{}::getEmulatedBlock chunk not loaded: {}",
    // getClass().getName(), pos);
    // return Blocks.AIR;
    // }
    // }
    // val te = getBlockEntity(world, pos);
    // if (te == null) return Blocks.AIR;
    // val b = te.getEmulatedBlock();
    // if (b == this) return Blocks.AIR;
    // return b;
    // }

    // public BlockState getEmulatedBlockState(IBlockReader world, BlockPos pos) {
    // if (world instanceof IWorld) {
    // val provider = ((IWorld)world).getChunkSource();
    // if (!provider.isEntityTickingChunk(new ChunkPos(pos))) {
    // LOGGER.error("{}::getEmulatedBlockState chunk not loaded: {}",
    // getClass().getName(), pos);
    // return Blocks.AIR.defaultBlockState();
    // }
    // }
    // val te = getBlockEntity(world, pos);
    // if (te == null) return Blocks.AIR.defaultBlockState();
    // val emulatedBlockState = te.getEmulatedBlockState();
    // if (emulatedBlockState == null) return Blocks.AIR.defaultBlockState();
    // if (emulatedBlockState.getBlock() == this) return
    // Blocks.AIR.defaultBlockState();
    // return emulatedBlockState;
    // }

    @Override
    public ActionResultType onBlockActivated(BlockState stateIn, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) return ActionResultType.PASS;
        INamedContainerProvider menuProvider = getContainer(stateIn, worldIn, pos);
        if (menuProvider == null) return ActionResultType.PASS;
        player.openContainer(menuProvider);
        return ActionResultType.SUCCESS;
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        // TODO Auto-generated method stub
        return null;
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