package com.github.chaostheeternal.redstone_additions.blocks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.chaostheeternal.redstone_additions.RedstoneAdditionsMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

public class InstantOffRepeaterBlock extends RedstoneDiodeBlock {
    private static final Logger LOGGER = LogManager.getLogger();    
    public static final IntegerProperty DELAY = BlockStateProperties.DELAY_1_4;
    public static final String REGISTRY_NAME = "instant_off_repeater_block";
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(RedstoneAdditionsMod.MOD_ID, REGISTRY_NAME);
    public static final InstantOffRepeaterBlock BLOCK = new InstantOffRepeaterBlock(Block.Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0F).sound(SoundType.WOOD));

    protected InstantOffRepeaterBlock(Block.Properties properties) {
        super(properties);
        this.setRegistryName(RESOURCE_LOCATION);
        this.setDefaultState(this.stateContainer.getBaseState().with(HORIZONTAL_FACING, Direction.NORTH).with(POWERED, false).with(DELAY, 1));
    }

    //TODO: Consider revisiting the "locked" capability of repeaters

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        // Lifted from RepeaterBlock, since this is a Repeater, it'll just be "slightly" different in appearance (and doesn't currently support locking)
        if (stateIn.get(POWERED)) {
            Direction direction = stateIn.get(HORIZONTAL_FACING);
            double d0 = (double)((float)pos.getX() + 0.5F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
            double d1 = (double)((float)pos.getY() + 0.4F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
            double d2 = (double)((float)pos.getZ() + 0.5F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
            float f = -5.0F;
            if (rand.nextBoolean()) { f = (float)(stateIn.get(DELAY) * 2 - 1); }
            f = f / 16.0F;
            double d3 = (double)(f * (float)direction.getXOffset());
            double d4 = (double)(f * (float)direction.getZOffset());
            worldIn.addParticle(RedstoneParticleData.REDSTONE_DUST, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        if (!this.isLocked(world, pos, state)) {
            boolean isPowered = state.get(POWERED);
            boolean sbPowered = this.shouldBePowered(world, pos, state);
            if (isPowered && !sbPowered) {
                world.setBlockState(pos, state.with(POWERED, false), 2);
            } else if (!isPowered) {
                world.setBlockState(pos, state.with(POWERED, true), 2);
                if (!sbPowered) {
                    world.getPendingBlockTicks().scheduleTick(pos, this, this.getDelay(state), TickPriority.VERY_HIGH);
                }
            }
        }
    }

    @Override
    protected void updateState(World worldIn, BlockPos pos, BlockState state) {
        if (!this.isLocked(worldIn, pos, state)) {
            boolean isPowered = state.get(POWERED);
            boolean sbPowered = this.shouldBePowered(worldIn, pos, state);
            if (isPowered != sbPowered && !worldIn.getPendingBlockTicks().isTickPending(pos, this)) {
                TickPriority tickpriority = TickPriority.HIGH;
                if (this.isFacingTowardsRepeater(worldIn, pos, state)) {
                    tickpriority = TickPriority.EXTREMELY_HIGH;
                } else if (isPowered) {
                    tickpriority = TickPriority.VERY_HIGH;
                }
                if (sbPowered) worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.getDelay(state), tickpriority);
                if (isPowered) worldIn.getPendingBlockTicks().scheduleTick(pos, this, 2, tickpriority); //OVERRIDE
            }
        }
    }
    
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!player.abilities.allowEdit) {
            return ActionResultType.PASS;
        } else {
            worldIn.setBlockState(pos, state.cycle(DELAY), 3);
            return ActionResultType.SUCCESS;
        }
    }
    
    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        if (side == null) return false; //This seems to fire if redstone is placed on a neighboring block on a different Y
        Direction myFacing = state.get(HORIZONTAL_FACING);
        return (myFacing == side || myFacing == side.getOpposite()); //Input is the way this block is facing, output is opposite the way this block is facing
    }

    @Override
    protected int getDelay(BlockState state) {
        return state.get(DELAY) * 2;
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING, DELAY, POWERED);
    }

    @Mod.EventBusSubscriber(modid = RedstoneAdditionsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerBlock(final RegistryEvent.Register<Block> e) {
            LOGGER.debug("{}::registerBlock", Registration.class.getName());
            e.getRegistry().register(BLOCK);
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public static int lineColorMultiplier(boolean isPowered) {
        if (isPowered) {
            return -16777216 | 255 << 16 | 51 << 8 | 0; 
        } else {
            return -16777216 | 77 << 16 | 0 << 8 | 0;
        }
    }
}