package com.raphydaphy.arcanemagic.block.base;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

import java.util.Objects;

public class OrientableBlockBase extends WaterloggableBlockBase {
    public static final DirectionProperty FACING;

    static {
        FACING = HorizontalFacingBlock.FACING;
    }

    protected OrientableBlockBase(Settings settings) {
        super(settings);
        this.setDefaultState((this.getDefaultState()).with(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction facing = ctx.getPlayerFacing().getOpposite();
        return Objects.requireNonNull(super.getPlacementState(ctx)).with(FACING, facing);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> map) {
        super.appendProperties(map);
        map.add(FACING);
    }
}
