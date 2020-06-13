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
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
        true,   // isSolid
        true,   // doesBlockMovement
        true,   // isOpaque
        true,   // requiresNoTool
        false,  // canBurn
        false,  // isReplacable
        PushReaction.DESTROY // pushReaction
    );
    public static final RedstoneInverterBlock BLOCK = new RedstoneInverterBlock();

    private RedstoneInverterBlock() {
        super(
            Properties.create(MATERIAL)
            .doesNotBlockMovement()
            .sound(SoundType.STONE)
        );
        LOGGER.debug("{}::ctor", getClass().getName());
        setRegistryName(RESOURCE_LOCATION);
        setDefaultState(stateContainer.getBaseState().with(FACING, Direction.NORTH).with(POWERED, false));
    }

    //TODO: Logic around how the block can be placed
    //  Specifically, that it has to remain under a solid block

    //TODO: Voxel Shape stuff here
    //  This will give me the targetting area AND the "what am I walking on" area

    //TODO: Redstone power input stuff here
    //  As in, how specifically do I tell if I'm getting power?
    //  OK, this seems to be right, but somehow it's like it's powering itself?
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        Direction inputDir = state.get(FACING);
        int inputPow = worldIn.getRedstonePower(pos.offset(inputDir), inputDir.getOpposite());
        //NOTE: This may seem backwards, but we're matching how Redstone Repeaters work.  They face opposite the direction you're facing when you place them.
        // So, they "face" the input, so you want to check one block forward from the way they're facing, but the power at the opposite side (so the border between input and repeater).
        BlockState newState = state.with(POWERED, inputPow != 0);
        if (newState != state) { worldIn.setBlockState(pos, newState, Constants.BlockFlags.DEFAULT_AND_RERENDER); }
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true; //it CAN provide power, WILL it is the other question
    }
    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        if (side == null) return false; //This seems to fire if redstone is placed on a neighboring block on a different Y
        Direction myFacing = state.get(FACING);
        return (myFacing == side || myFacing == side.getOpposite()); //Front is the way this block is facing, back is opposite the way this block is facing
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
    //TODO: Figure out why this gets power "stuck" when neighboring blocks update but not when this one does

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
    } //TODO: Need to make this figure out if it's not on a block that could hold it and break

    @Mod.EventBusSubscriber(modid = RedstoneAdditionsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerBlock(final RegistryEvent.Register<Block> e) {
            LOGGER.debug("{}::registerBlock", Registration.class.getName());
            e.getRegistry().register(BLOCK);
        }
    }
}