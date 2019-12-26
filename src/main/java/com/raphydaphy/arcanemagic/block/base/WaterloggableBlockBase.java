package com.raphydaphy.arcanemagic.block.base;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;

public class WaterloggableBlockBase extends Block implements Waterloggable {
    public static final BooleanProperty WATERLOGGED;

    static {
        WATERLOGGED = Properties.WATERLOGGED;
    }

    protected WaterloggableBlockBase(Settings settings) {
        super(settings);
        this.setDefaultState((this.stateManager.getDefaultState()).with(WATERLOGGED, false));
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction dir, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, dir, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluid = ctx.getWorld().getFluidState(ctx.getBlockPos());
        return this.getDefaultState().with(WATERLOGGED, fluid.getFluid() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> map) {
        map.add(WATERLOGGED);
    }
}
