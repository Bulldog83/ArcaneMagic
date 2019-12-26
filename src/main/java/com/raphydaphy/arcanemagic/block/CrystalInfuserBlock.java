package com.raphydaphy.arcanemagic.block;

import com.raphydaphy.arcanemagic.ArcaneMagic;
import com.raphydaphy.arcanemagic.block.base.OrientableBlockBase;
import com.raphydaphy.arcanemagic.block.entity.CrystalInfuserBlockEntity;
import com.raphydaphy.arcanemagic.init.ArcaneMagicConstants;
import com.raphydaphy.arcanemagic.network.ProgressionUpdateToastPacket;
import com.raphydaphy.arcanemagic.notebook.NotebookSectionRegistry;
import com.raphydaphy.arcanemagic.util.ArcaneMagicUtils;
import com.raphydaphy.crochet.data.DataHolder;
import com.raphydaphy.crochet.network.PacketHandler;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class CrystalInfuserBlock extends OrientableBlockBase implements BlockEntityProvider {
    private static final VoxelShape shape;

    static {
        VoxelShape cubes_one = VoxelShapes.union(
                VoxelShapes.union(Block.createCuboidShape(12, 0, 2, 14, 2, 4), Block.createCuboidShape(12, 0, 12, 14, 2, 14)),
                VoxelShapes.union(Block.createCuboidShape(2, 0, 12, 4, 2, 14), Block.createCuboidShape(2, 0, 2, 4, 2, 4)));
        VoxelShape cubes_two = VoxelShapes.union(
                VoxelShapes.union(Block.createCuboidShape(9, 1, 5, 11, 3, 7), Block.createCuboidShape(9, 1, 9, 11, 3, 11)),
                VoxelShapes.union(Block.createCuboidShape(5, 1, 9, 7, 3, 11), Block.createCuboidShape(5, 1, 5, 7, 3, 7)));
        VoxelShape cubes_three = VoxelShapes.union(
                VoxelShapes.union(Block.createCuboidShape(4, 4, 10, 6, 6, 12), Block.createCuboidShape(4, 4, 4, 6, 6, 6)),
                VoxelShapes.union(Block.createCuboidShape(10, 4, 4, 12, 6, 6), Block.createCuboidShape(10, 4, 10, 12, 6, 12)));
        VoxelShape cubes_four = VoxelShapes.union(Block.createCuboidShape(2, 6, 2, 14, 8, 14), Block.createCuboidShape(4, 8, 4, 12, 10, 12));

        shape = VoxelShapes.union(VoxelShapes.union(cubes_one, cubes_two), VoxelShapes.union(cubes_three, cubes_four));
    }

    public CrystalInfuserBlock() {
        super(FabricBlockSettings.of(Material.WOOD).strength(2f, 3f).sounds(BlockSoundGroup.WOOD).build());
    }

    public boolean activate(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (!(blockEntity instanceof CrystalInfuserBlockEntity)) {
            return false;
        }

        if (!((CrystalInfuserBlockEntity) blockEntity).isActive()) {
            if (player.isSneaking()) {
                // Try to extract from any slot
                for (int i = 2; i >= 0; i--) {
                    boolean success = ArcaneMagicUtils.pedestalInteraction(world, player, blockEntity, hand, i);
                    if (success) {
                        return true;
                    }
                }
            } else {
                // Try to insert to the right slot
                int slot = ((CrystalInfuserBlockEntity) blockEntity).getSlotForItem(player.getStackInHand(hand));
                if (slot != -1) {
                    return ArcaneMagicUtils.pedestalInteraction(world, player, blockEntity, hand, slot);
                }
            }
        }
        return false;
    }

    @Override
    public void onBlockRemoved(BlockState oldState, World world, BlockPos pos, BlockState newState, boolean boolean_1) {
        if (ArcaneMagicUtils.handleTileEntityBroken(this, oldState, world, pos, newState)) {
            super.onBlockRemoved(oldState, world, pos, newState, boolean_1);
        }
    }

    public RenderLayer getRenderLayer() {
        return RenderLayer.getCutout();
    }

    @Override
    public boolean hasComparatorOutput(BlockState blockState_1) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ArcaneMagicUtils.calculateComparatorOutput(world, pos);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext vep) {
        return shape;
    }

    @Override
    public BlockEntity createBlockEntity(BlockView var1) {
        return new CrystalInfuserBlockEntity();
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!world.isClient && placer instanceof PlayerEntity && !((DataHolder) placer).getAdditionalData(ArcaneMagic.DOMAIN).getBoolean(ArcaneMagicConstants.PLACED_INFUSER_KEY)) {
            PacketHandler.sendToClient(new ProgressionUpdateToastPacket(true), (ServerPlayerEntity) placer);
            ((DataHolder) placer).getAdditionalData(ArcaneMagic.DOMAIN).putBoolean(ArcaneMagicConstants.PLACED_INFUSER_KEY, true);
            ArcaneMagicUtils.updateNotebookSection(world, (DataHolder) placer, NotebookSectionRegistry.INFUSION.getID().toString(), false);
            ((DataHolder) placer).markAdditionalDataDirty();
        }
    }
}
