package com.github.chaostheeternal.redstone_additions.blocks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.chaostheeternal.redstone_additions.RedstoneAdditionsMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

public class RedstoneInverterBlock extends RedstoneDiodeBlock {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final BooleanProperty BURNED_OUT = BooleanProperty.create("burned_out");
    public static final String REGISTRY_NAME = "redstone_inverter_block";
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(RedstoneAdditionsMod.MOD_ID, REGISTRY_NAME);
    public static final RegistryObject<RedstoneInverterBlock> REGISTRY_OBJECT = RegistryObject.of(RESOURCE_LOCATION, ForgeRegistries.BLOCKS);
    private static final java.util.Map<IBlockReader, java.util.List<RedstoneInverterBlock.Toggle>> BURNED_INVERTERS = new java.util.WeakHashMap<>();
    public static final RedstoneInverterBlock BLOCK = new RedstoneInverterBlock(Block.Properties.create(Material.MISCELLANEOUS).hardnessAndResistance(0.0F).sound(SoundType.WOOD));
    
    private RedstoneInverterBlock(Block.Properties properties) {
        super(properties);
        this.setRegistryName(RESOURCE_LOCATION);
        this.setDefaultState(this.stateContainer.getBaseState().with(HORIZONTAL_FACING, Direction.NORTH).with(POWERED, Boolean.valueOf(false)).with(BURNED_OUT, Boolean.valueOf(false)));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        // Lifted from RepeaterBlock, since this is a Repeater, it'll just be "slightly" different in appearance (and doesn't currently support locking)
        if (!stateIn.get(POWERED) && !stateIn.get(BURNED_OUT)) {
            Direction direction = stateIn.get(HORIZONTAL_FACING);
            double d0 = (double)((float)pos.getX() + 0.5F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
            double d1 = (double)((float)pos.getY() + 0.4F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
            double d2 = (double)((float)pos.getZ() + 0.5F) + (double)(rand.nextFloat() - 0.5F) * 0.2D;
            float f = -5.0F;
            if (rand.nextBoolean()) { f = (float)((rand.nextInt(4) + 1) * 2 - 1); }
            f = f / 16.0F;
            double d3 = (double)(f * (float)direction.getXOffset());
            double d4 = (double)(f * (float)direction.getZOffset());
            worldIn.addParticle(RedstoneParticleData.REDSTONE_DUST, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
        if (!this.isLocked(world, pos, state) && !state.get(BURNED_OUT) && !burnoutCheck(state, world, pos, rand, this.shouldBePowered(world, pos, state))) {
            boolean isPowered = state.get(POWERED);
            boolean sbPowered = this.shouldBePowered(world, pos, state);
            if (isPowered && !sbPowered) {
                world.setBlockState(pos, state.with(POWERED, Boolean.valueOf(false)), 2);
            } else if (!isPowered) {
                world.setBlockState(pos, state.with(POWERED, Boolean.valueOf(true)), 2);
                if (!sbPowered) {
                    world.getPendingBlockTicks().scheduleTick(pos, this, this.getDelay(state), TickPriority.VERY_HIGH);
                }
            }
        }
    }

    @Override
    protected void updateState(World worldIn, BlockPos pos, BlockState state) {
        if (!this.isLocked(worldIn, pos, state) && !state.get(BURNED_OUT)) {
            boolean isPowered = state.get(POWERED);
            boolean sbPowered = this.shouldBePowered(worldIn, pos, state);
            if (isPowered != sbPowered && !worldIn.getPendingBlockTicks().isTickPending(pos, this)) {
                TickPriority tickpriority = TickPriority.HIGH;
                if (this.isFacingTowardsRepeater(worldIn, pos, state)) {
                    tickpriority = TickPriority.EXTREMELY_HIGH;
                } else if (isPowered) {
                    tickpriority = TickPriority.VERY_HIGH;
                }
                worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.getDelay(state), tickpriority);
            }
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
        return 0;
    }

    @Override
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return getWeakPower(blockState, blockAccess, pos, side);
    }

    @Override
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {  
        if (blockState.get(POWERED) || blockState.get(BURNED_OUT)) { //Since this is an inverter, it doesn't output power when it is itself powered
            return 0;
        } else {
            return blockState.get(HORIZONTAL_FACING) == side ? this.getActiveSignal(blockAccess, pos, blockState) : 0;
        }
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING, POWERED, BURNED_OUT);
    }

    @Mod.EventBusSubscriber(modid = RedstoneAdditionsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registration {
        @SubscribeEvent
        public static void registerBlock(final RegistryEvent.Register<Block> e) {
            LOGGER.debug("{}::registerBlock", Registration.class.getName());
            e.getRegistry().register(BLOCK);
        }
    }

    public static boolean burnoutCheck(BlockState state, World worldIn, BlockPos pos, Random rand, boolean shouldBePowered) {
        java.util.List<RedstoneInverterBlock.Toggle> list = BURNED_INVERTERS.get(worldIn);

        while(list != null && !list.isEmpty() && worldIn.getGameTime() - (list.get(0)).time > 60L) {
            list.remove(0);
        }

        if (!state.get(POWERED)) {
            if (shouldBePowered) {
                if (isBurnedOut(worldIn, pos, false)) {
                    worldIn.playEvent(1502, pos, 0); //burnout effect
                    worldIn.setBlockState(pos, state.with(POWERED, Boolean.valueOf(true)).with(BURNED_OUT, Boolean.valueOf(true)), 3);
                    worldIn.getPendingBlockTicks().scheduleTick(pos, worldIn.getBlockState(pos).getBlock(), 160);
                    return true;
                } else {
                    worldIn.setBlockState(pos, state.with(POWERED, Boolean.valueOf(true)), 3);
                }
            }
        } else if (!shouldBePowered && !isBurnedOut(worldIn, pos, true)) {
            worldIn.setBlockState(pos, state.with(POWERED, Boolean.valueOf(false)), 3);
        }  
        return false;
    }
    private static boolean isBurnedOut(World worldIn, BlockPos pos, boolean isPowered) {
        java.util.List<RedstoneInverterBlock.Toggle> list = BURNED_INVERTERS.computeIfAbsent(worldIn, (i) -> { return com.google.common.collect.Lists.newArrayList(); });
        if (isPowered) { list.add(new RedstoneInverterBlock.Toggle(pos.toImmutable(), worldIn.getGameTime())); }
        int i = 0;  
        for(int j = 0; j < list.size(); ++j) {
            RedstoneInverterBlock.Toggle inverterblock$toggle = list.get(j);
            if (inverterblock$toggle.pos.equals(pos) && ++i >= 8) { return true; }
        }  
        return false;
    }

    public static class Toggle {
        private final BlockPos pos;
        private final long time;
  
        public Toggle(BlockPos pos, long time) {
            this.pos = pos;
            this.time = time;
        }
    }
}