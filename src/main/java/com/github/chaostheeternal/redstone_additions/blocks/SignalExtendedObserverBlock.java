package com.github.chaostheeternal.redstone_additions.blocks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

import com.github.chaostheeternal.redstone_additions.RedstoneAdditionsMod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class SignalExtendedObserverBlock extends ObserverBlock {
    private static final Logger LOGGER = LogManager.getLogger();    
    public static final IntegerProperty DELAY = BlockStateProperties.DELAY_1_4;
    public static final IntegerProperty COUNTDOWN = IntegerProperty.create("countdown", 0, 4); //How can I make this private?  Meaning, I either don't need to put it in the BlockState or at least doesn't show on BlockState.
    public static final String REGISTRY_NAME = "signal_extended_observer_block";
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(RedstoneAdditionsMod.MOD_ID, REGISTRY_NAME);
    public static final SignalExtendedObserverBlock BLOCK = new SignalExtendedObserverBlock(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.0F));

    protected SignalExtendedObserverBlock(Block.Properties properties) {
        super(properties);
        this.setRegistryName(RESOURCE_LOCATION);
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH).with(POWERED, Boolean.valueOf(false)).with(DELAY, Integer.valueOf(1)));
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, DELAY, COUNTDOWN);
    }
    
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        if (state.get(POWERED)) { //Decrement the countdown, and if we hit 0, turn this block back off
            int newCount = state.get(COUNTDOWN) - 1;
            if (newCount <= 0) {
                worldIn.setBlockState(pos, state.with(COUNTDOWN, 0).with(POWERED, Boolean.valueOf(false)), 2);
            } else {
                worldIn.setBlockState(pos, state.with(COUNTDOWN, newCount), 2);
                worldIn.getPendingBlockTicks().scheduleTick(pos, this, 2);
            }
        } else { //Setting the countdown is done in updatePostPlacement, so I don't have to deal with setting it here
            worldIn.setBlockState(pos, state.with(POWERED, Boolean.valueOf(true)), 2);
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, 2);
        }
        this.updateNeighborsInFront(worldIn, pos, state);
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, net.minecraft.world.IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(FACING) == facing) {
            if (!stateIn.get(POWERED) && !worldIn.isRemote() && !worldIn.getPendingBlockTicks().isTickScheduled(currentPos, this)) {
                worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, 2);
            }
            worldIn.setBlockState(currentPos, stateIn.with(COUNTDOWN, stateIn.get(DELAY)), 2);
        }  
        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }
    
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        //Would consider restricting this to only when you click on the sides with the switches, but not sure how to handle that when UP or DOWN
        if (!player.abilities.allowEdit) {
            return ActionResultType.PASS;
        } else {
            worldIn.setBlockState(pos, state.cycle(DELAY), 3);
            return ActionResultType.SUCCESS;
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