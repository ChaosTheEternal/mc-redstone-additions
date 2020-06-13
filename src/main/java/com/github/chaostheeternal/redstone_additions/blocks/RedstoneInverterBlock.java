package com.github.chaostheeternal.redstone_additions.blocks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.chaostheeternal.redstone_additions.RedstoneAdditionsMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntitySpawnPlacementRegistry.PlacementType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public class RedstoneInverterBlock extends Block {
    private static final Logger LOGGER = LogManager.getLogger();    
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final String REGISTRY_NAME = "redstone_inverter_block";
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(RedstoneAdditionsMod.MOD_ID, REGISTRY_NAME);
    public static final RegistryObject<RedstoneInverterBlock> REGISTRY_OBJECT = RegistryObject.of(RESOURCE_LOCATION, ForgeRegistries.BLOCKS);
    public static final Material MATERIAL = new Material(
        MaterialColor.LIGHT_GRAY, // color
        false,  // isLiquid
        false,  // isSolid
        true,   // doesBlockMovement
        false,  // isOpaque
        true,   // requiresNoTool
        false,  // canBurn
        false,  // isReplacable
        PushReaction.DESTROY // pushReaction
    );
    public static final RedstoneInverterBlock BLOCK = new RedstoneInverterBlock();

	public static final VoxelShape SHAPE = Block.makeCuboidShape(0.D, 0.D, 0.D, 16.D, 2.D, 16.D);
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return true;
    }

	@Override
	public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, PlacementType type, EntityType<?> entityType) {
		return false;
	}

	public static boolean canBePlacedOn(BlockState state, IWorldReader world, BlockPos pos) {
		if(world.isAirBlock(pos)) return false;
		BlockState blockState = world.getBlockState(pos);
		return blockState.isSolid();
	}

	@Override
	public boolean isValidPosition( BlockState state, IWorldReader world, BlockPos pos ) {
		return canBePlacedOn(state, world, pos.offset(Direction.DOWN));
	}
    //TODO: Need to figure out why the copied model doesn't seem to work (can't find "redstone_torch_on" and doesn't have transparency)
    //TODO: Need to change the model to look closer to a minifed torch off a "wall" and redstone out.
    //TODO: Need to change the item icon to look closer to the new model

    private RedstoneInverterBlock() {
        super(
            Properties.create(MATERIAL)
            .notSolid()
            .sound(SoundType.STONE)
        );
        LOGGER.debug("{}::ctor", getClass().getName());
        setRegistryName(RESOURCE_LOCATION);
        setDefaultState(stateContainer.getBaseState().with(FACING, Direction.NORTH).with(POWERED, false));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World worldIn, BlockPos pos, Random rand) {
        if (!state.get(POWERED)) {
            //TODO: Only need to do this once, on the torch, and only when it isn't powered (so the torch is on and the block is outputting power)
            double dx = (double)pos.getX() + 0.5D + ((double)rand.nextFloat() - 0.5D) * 0.2D;
            double dy = (double)((float)pos.getY() + 0.375F); //6 pixels above bottom, which should be mid-point of the torch tip
            double dz = (double)pos.getZ() + 0.5D + ((double)rand.nextFloat() - 0.5D) * 0.2D;

            float f1 = 1.0F;
            float f2 = Math.max(0.0F, 0.2F);
            float f3 = Math.max(0.0F, -0.1F);
            RedstoneParticleData particle = new RedstoneParticleData(f1, f2, f3, 1.0F);
            worldIn.addParticle(particle, dx, dy, dz, 0.0D, 0.0D, 0.0D);
        }
    }
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        Direction inputDir = state.get(FACING);
        //NOTE: This may seem backwards, but we're matching how Redstone Repeaters work.  They "face" opposite the direction you're facing when you place them.
        // So, they "face" the input, so you want to check one block forward from the way they're facing, but the power at the opposite side (so the border between input and this).
        BlockState newState = state.with(POWERED, worldIn.isSidePowered(pos.offset(inputDir), inputDir.getOpposite()));
        if (newState != state) { worldIn.setBlockState(pos, newState, Constants.BlockFlags.DEFAULT_AND_RERENDER); }
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        if (side == null) return false; //This seems to fire if redstone is placed on a neighboring block on a different Y
        Direction myFacing = state.get(FACING);
        return (myFacing == side || myFacing == side.getOpposite()); //Input is the way this block is facing, output is opposite the way this block is facing
    }

    @Override
    public int getWeakPower(BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side) {
        if (state.get(FACING) == side && !state.get(POWERED)) {
            return 15; // Output full power
        } else {
            return 0; // Wrong side or we're getting power, no power output
        }
    }
    @Override
    public int getStrongPower(BlockState state, IBlockReader blockAccess, BlockPos pos, Direction side) {
        if (state.get(FACING) == side && !state.get(POWERED)) {
            return 15; // Output full power
        } else {
            return 0; // Wrong side or we're getting power, no power output
        }
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side) {
        return false;
    }
    //TODO: Figure out why this gets power "stuck" on the output despite the input changing

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation direction) {
        return state.with(FACING, direction.rotate(state.get(FACING)));
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(FACING).add(POWERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return getDefaultState().with(
            FACING,
            context.getPlacementHorizontalFacing().getOpposite()
        ); //Place it facing the same horizontal direction you are
    }
    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        if (!state.isValidPosition(worldIn, pos)) {
            worldIn.destroyBlock(pos, true);
            return;
        }
    }
    //TODO: Need to make this figure out if it's not on a block that could hold it and break

    @Mod.EventBusSubscriber(modid = RedstoneAdditionsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerBlock(final RegistryEvent.Register<Block> e) {
            LOGGER.debug("{}::registerBlock", Registration.class.getName());
            e.getRegistry().register(BLOCK);
        }
    }
}