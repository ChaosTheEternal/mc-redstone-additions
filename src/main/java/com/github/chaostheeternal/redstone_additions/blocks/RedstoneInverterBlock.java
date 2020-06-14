package com.github.chaostheeternal.redstone_additions.blocks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.chaostheeternal.redstone_additions.RedstoneAdditionsMod;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
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
        false,  // doesBlockMovementOfLiquids
        false,  // isOpaque
        true,   // requiresNoTool
        false,  // canBurn
        false,  // isReplacable
        PushReaction.DESTROY // pushReaction
    );
    public static final RedstoneInverterBlock BLOCK = new RedstoneInverterBlock();

    //TODO: Why does it not realize it's getting powered by redstone lines it attached to when placed?

	public static final VoxelShape SHAPE = Block.makeCuboidShape(0.D, 0.D, 0.D, 16.D, 2.D, 16.D);
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		return SHAPE;
	}

	@Override
	public boolean allowsMovement(BlockState state, IBlockReader world, BlockPos pos, PathType type) {
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
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (!state.get(POWERED) && rand.nextFloat() > 0.249D) { //Only give a puff 75% of the time when giving off power
            double dx, dz, dy = (double)pos.getY() + (rand.nextFloat() * 0.125D + 0.3125D); //Y won't change, regardless of facing
            switch (state.get(FACING)) {
                case NORTH:
                    dx = (double)pos.getX() + (rand.nextFloat() * 0.1875D + 0.4375D);
                    dz = (double)pos.getZ() + (1.0D - (rand.nextFloat() * 0.1875D + 0.5D));
                    break;
                case EAST:
                    dx = (double)pos.getX() + (rand.nextFloat() * 0.1875D + 0.5D);
                    dz = (double)pos.getZ() + (rand.nextFloat() * 0.1875D + 0.4375D);
                    break;
                case WEST:
                    dx = (double)pos.getX() + (1.0D - (rand.nextFloat() * 0.1875D + 0.5D));
                    dz = (double)pos.getZ() + (rand.nextFloat() * 0.1875D + 0.4375D);
                    break;
                default: //includes SOUTH
                    dx = (double)pos.getX() + (rand.nextFloat() * 0.1875D + 0.4375D);
                    dz = (double)pos.getZ() + (rand.nextFloat() * 0.1875D + 0.5D);
                    break;
            }
            float f1 = 1.0F;
            float f2 = Math.max(0.0F, 0.2F);
            float f3 = Math.max(0.0F, -0.1F);
            RedstoneParticleData particle = new RedstoneParticleData(f1, f2, f3, 1.0F);
            world.addParticle(particle, dx, dy, dz, 0.0D, 0.0D, 0.0D);
        }
    }
    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (fromPos.equals(pos.offset(state.get(FACING)))) checkIfPowerChanged(state, world, pos); //If the change is from the input side, check if the power has changed
        if (fromPos.equals(pos.offset(Direction.DOWN)) && !canBePlacedOn(state, world, fromPos)) world.destroyBlock(pos, true); // If the change came from below, check if we're still on a valid block
    }
    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() != state.getBlock() && !world.isRemote()) { checkIfPowerChanged(state, world, pos); }
    }
    private static int POWER_CHANGED_ITERATION = 0; //This is for a burnout circuit, in case you power the inverter with itself
    public void checkIfPowerChanged(BlockState state, World world, BlockPos pos) {
        Direction inputDir = state.get(FACING);
        //NOTE: This may seem backwards, but we're matching how Redstone Repeaters work.  They "face" opposite the direction you're facing when you place them.
        // So, they "face" the input, so you want to check one block forward from the way they're facing, but the power at the opposite side (so the border between input and this).
        boolean shouldBePowered = world.isSidePowered(pos.offset(inputDir), inputDir.getOpposite()) || 
                                  (world.getBlockState(pos.offset(inputDir)).getBlock() instanceof RedstoneWireBlock && world.isBlockPowered(pos.offset(inputDir)));
        //There's probably a better way to check if the adjacent block is redstone wire and powered to know if I'm supposed to be powered
        BlockState newState = state.with(POWERED, shouldBePowered); //BUG: Powered redstone dust doesn't appear to say the side is powered because it hasn't necessarily attached when a block is placed
        if (newState != state && POWER_CHANGED_ITERATION < 3 && newState.get(POWERED)) {
            POWER_CHANGED_ITERATION += 1; 
            world.setBlockState(pos, newState, Constants.BlockFlags.DEFAULT_AND_RERENDER); 
            BlockPos updatePos = pos.offset(inputDir.getOpposite());
            world.notifyNeighborsOfStateChange(updatePos, world.getBlockState(updatePos).getBlock()); //Tell any block on the output side that strong power has changed
            POWER_CHANGED_ITERATION -= 1;
        }
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
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        if (!state.isValidPosition(world, pos)) {
            world.destroyBlock(pos, true);
            return;
        }
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