package com.raphydaphy.arcanemagic.block;

import com.raphydaphy.arcanemagic.ArcaneMagic;
import com.raphydaphy.arcanemagic.block.base.OrientableBlockBase;
import com.raphydaphy.arcanemagic.block.entity.AnalyzerBlockEntity;
import com.raphydaphy.arcanemagic.client.render.IExtraRenderLayers;
import com.raphydaphy.arcanemagic.init.ArcaneMagicConstants;
import com.raphydaphy.arcanemagic.init.ModCutscenes;
import com.raphydaphy.arcanemagic.init.ModRegistry;
import com.raphydaphy.arcanemagic.network.ProgressionUpdateToastPacket;
import com.raphydaphy.arcanemagic.notebook.NotebookSectionRegistry;
import com.raphydaphy.arcanemagic.parchment.DiscoveryParchment;
import com.raphydaphy.arcanemagic.util.ArcaneMagicUtils;
import com.raphydaphy.crochet.data.DataHolder;
import com.raphydaphy.crochet.network.PacketHandler;
import com.raphydaphy.cutsceneapi.cutscene.CutsceneManager;
import net.fabricmc.fabric.api.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class AnalyzerBlock extends OrientableBlockBase implements BlockEntityProvider, IExtraRenderLayers {
    private static final VoxelShape shape;

    static {
        VoxelShape one = VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(2, 0, 7, 4, 2, 9), Block.createCuboidShape(7, 0, 2, 9, 2, 4)),
                VoxelShapes.union(Block.createCuboidShape(7, 0, 12, 9, 2, 14), Block.createCuboidShape(12, 0, 7, 14, 2, 9)));
        VoxelShape two = VoxelShapes.union(VoxelShapes.union(Block.createCuboidShape(4, 0, 4, 12, 4, 12), Block.createCuboidShape(2, 4, 2, 4, 8, 14)),
                VoxelShapes.union(Block.createCuboidShape(4, 4, 2, 12, 8, 4), Block.createCuboidShape(12, 4, 2, 14, 8, 14)));
        VoxelShape three = VoxelShapes.union(Block.createCuboidShape(6, 4, 6, 10, 6, 10), Block.createCuboidShape(4, 4, 12, 12, 8, 14));

        shape = VoxelShapes.union(VoxelShapes.union(one, two), three);
    }

    public AnalyzerBlock() {
        super(FabricBlockSettings.of(Material.WOOD).strength(2f, 2f).sounds(BlockSoundGroup.WOOD).build());
    }

    public boolean activate(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (!(blockEntity instanceof AnalyzerBlockEntity)) {
            return false;
        }

        return ArcaneMagicUtils.pedestalInteraction(world, player, blockEntity, hand, 0, (stack) ->
        {
            // All of this is only called on the server-side
            DataHolder dataPlayer = ((DataHolder) player);
            CompoundTag data = dataPlayer.getAdditionalData(ArcaneMagic.DOMAIN);
            boolean notebookUpdate = false;
            if (stack.getItem() == Items.STICK) {
                if (!data.getBoolean(ArcaneMagicConstants.ANALYZED_STICK_KEY)) {
                    PacketHandler.sendToClient(new ProgressionUpdateToastPacket(false), (ServerPlayerEntity) player);
                    data.putBoolean(ArcaneMagicConstants.ANALYZED_STICK_KEY, true);
                    ArcaneMagicUtils.unlockRecipe(player, "golden_scepter");
                    dataPlayer.markAdditionalDataDirty();
                }
            } else if (stack.getItem() == Blocks.CRAFTING_TABLE.asItem()) {
                if (data.getBoolean(ArcaneMagicConstants.ANALYZED_STICK_KEY) && !data.getBoolean(ArcaneMagicConstants.ANALYZED_CRAFTING_TABLE_KEY)) {
                    data.putBoolean(ArcaneMagicConstants.ANALYZED_CRAFTING_TABLE_KEY, true);
                    ArcaneMagicUtils.updateNotebookSection(world, dataPlayer, NotebookSectionRegistry.TRANSFIGURATION.getID().toString(), false);
                    ArcaneMagicUtils.unlockRecipe(player, "transfiguration_table");
                    notebookUpdate = true;
                }
            } else if (stack.getItem() == Blocks.OBSIDIAN.asItem()) {
                if (data.getBoolean(ArcaneMagicConstants.CRAFTED_SOUL_PENDANT_KEY) && !data.getBoolean(ArcaneMagicConstants.ANALYZED_OBSIDIAN_KEY)) {
                    data.putBoolean(ArcaneMagicConstants.ANALYZED_OBSIDIAN_KEY, true);
                    ArcaneMagicUtils.updateNotebookSection(world, dataPlayer, NotebookSectionRegistry.SOUL_COLLECTION.getID().toString(), false);
                    notebookUpdate = true;
                }
            } else if (stack.getItem() instanceof SwordItem) {
                if (data.getBoolean(ArcaneMagicConstants.ANALYZED_OBSIDIAN_KEY) && !data.getBoolean(ArcaneMagicConstants.ANALYZED_SWORD_KEY)) {
                    data.putBoolean(ArcaneMagicConstants.ANALYZED_SWORD_KEY, true);
                    ArcaneMagicUtils.updateNotebookSection(world, dataPlayer, NotebookSectionRegistry.ARMOURY.getID().toString(), false);
                    ArcaneMagicUtils.unlockRecipe(player, "iron_dagger");
                    notebookUpdate = true;
                }
            } else if (stack.getItem() == Blocks.BLAST_FURNACE.asItem()) {
                if (data.getBoolean(ArcaneMagicConstants.CRAFTED_GOLD_CRYSTAL_KEY) && !data.getBoolean(ArcaneMagicConstants.ANALYZED_BLAST_FURNACE_KEY)) {
                    data.putBoolean(ArcaneMagicConstants.ANALYZED_BLAST_FURNACE_KEY, true);
                    ArcaneMagicUtils.updateNotebookSection(world, dataPlayer, NotebookSectionRegistry.SMELTING.getID().toString(), false);
                    notebookUpdate = true;
                }
            } else if (stack.getItem() == Blocks.ENCHANTING_TABLE.asItem()) {
                if (data.getBoolean(ArcaneMagicConstants.CRAFTED_DAGGER_KEY) && !data.getBoolean(ArcaneMagicConstants.ANALYZED_ENCHANTING_TABLE_KEY)) {
                    data.putBoolean(ArcaneMagicConstants.ANALYZED_ENCHANTING_TABLE_KEY, true);
                    ArcaneMagicUtils.updateNotebookSection(world, dataPlayer, NotebookSectionRegistry.INFUSION.getID().toString(), false);
                    notebookUpdate = true;
                }
            } else if (stack.getItem() == Blocks.DISPENSER.asItem()) {
                if (data.getBoolean(ArcaneMagicConstants.PLACED_SMELTER_KEY) && !data.getBoolean(ArcaneMagicConstants.ANALYZED_DISPENSER_KEY)) {
                    data.putBoolean(ArcaneMagicConstants.ANALYZED_DISPENSER_KEY, true);
                    ArcaneMagicUtils.updateNotebookSection(world, dataPlayer, NotebookSectionRegistry.LIQUEFACTION.getID().toString(), false);
                    notebookUpdate = true;
                }
            } else if (stack.getItem() == Items.REDSTONE) {
                if (data.getBoolean(ArcaneMagicConstants.PLACED_MIXER_KEY) && !data.getBoolean(ArcaneMagicConstants.ANALYZED_REDSTONE_KEY)) {
                    data.putBoolean(ArcaneMagicConstants.ANALYZED_REDSTONE_KEY, true);
                    ArcaneMagicUtils.updateNotebookSection(world, dataPlayer, NotebookSectionRegistry.FLUID_TRANSPORT.getID().toString(), false);
                    notebookUpdate = true;
                }
            } else if (stack.getItem() == Items.WATER_BUCKET) {
                if (data.getBoolean(ArcaneMagicConstants.PLACED_MIXER_KEY) && !data.getBoolean(ArcaneMagicConstants.ANALYZED_WATER_BUCKET_KEY)) {
                    data.putBoolean(ArcaneMagicConstants.ANALYZED_WATER_BUCKET_KEY, true);
                    ArcaneMagicUtils.updateNotebookSection(world, dataPlayer, NotebookSectionRegistry.PUMPING.getID().toString(), false);
                    notebookUpdate = true;
                }
            } else if (stack.getItem() == Blocks.SOUL_SAND.asItem()) {
                if (data.getBoolean(ArcaneMagicConstants.EXPERIENCED_TREMOR_KEY) && !data.getBoolean(ArcaneMagicConstants.ANALYZED_SOUL_SAND_KEY)) {
                    data.putBoolean(ArcaneMagicConstants.ANALYZED_SOUL_SAND_KEY, true);
                    ArcaneMagicUtils.updateNotebookSection(world, dataPlayer, NotebookSectionRegistry.DECONSTRUCTION.getID().toString(), false);
                    notebookUpdate = true;
                }
            } else if (stack.getItem() == ModRegistry.RELIC) {
                CutsceneManager.startServer((ServerPlayerEntity) player, ModCutscenes.RELIC_NETHER.getID());
            } else {
                if (data.getIntArray(ArcaneMagicConstants.GATHER_QUEST_ANALYZED_INDEXES_KEY).length < 4) {
                    for (int index : data.getIntArray(ArcaneMagicConstants.GATHER_QUEST_INDEXES_KEY)) {
                        Ingredient ingredient = DiscoveryParchment.GATHER_QUEST_OPTIONS[index];
                        if (ingredient.test(stack)) // apply
                        {
                            int[] analyzedArray = data.getIntArray(ArcaneMagicConstants.GATHER_QUEST_ANALYZED_INDEXES_KEY);
                            List<Integer> analyzed = new ArrayList<>();
                            for (int analyzedID : analyzedArray) {
                                analyzed.add(analyzedID);
                            }
                            analyzed.add(index);
                            data.putIntArray(ArcaneMagicConstants.GATHER_QUEST_ANALYZED_INDEXES_KEY, analyzed);
                            dataPlayer.markAdditionalDataDirty();
                            break;
                        }
                    }

                    if (data.getIntArray(ArcaneMagicConstants.GATHER_QUEST_ANALYZED_INDEXES_KEY).length >= 4) {
                        PacketHandler.sendToClient(new ProgressionUpdateToastPacket(false), (ServerPlayerEntity) player);
                    }
                } else if (!data.getBoolean(ArcaneMagicConstants.ANALYZED_STICK_KEY)) {
                    for (int index : data.getIntArray(ArcaneMagicConstants.ANALYSIS_QUEST_INDEXES_KEY)) {
                        if (index != -1) {
                            Ingredient ingredient = DiscoveryParchment.ANALYSIS_QUEST_OPTIONS[index];
                            if (ingredient.test(stack)) // apply
                            {
                                int[] analyzedArray = data.getIntArray(ArcaneMagicConstants.ANALYSIS_QUEST_ANALYZED_INDEXES_KEY);
                                List<Integer> analyzed = new ArrayList<>();
                                for (int analyzedID : analyzedArray) {
                                    analyzed.add(analyzedID);
                                }
                                analyzed.add(index);
                                data.putIntArray(ArcaneMagicConstants.ANALYSIS_QUEST_ANALYZED_INDEXES_KEY, analyzed);
                                dataPlayer.markAdditionalDataDirty();
                                return;
                            }
                        }
                    }
                }
            }

            if (notebookUpdate) {
                PacketHandler.sendToClient(new ProgressionUpdateToastPacket(true), (ServerPlayerEntity) player);
                dataPlayer.markAdditionalDataDirty();
            }
        });
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
        return new AnalyzerBlockEntity();
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!world.isClient && placer instanceof PlayerEntity && !((DataHolder) placer).getAdditionalData(ArcaneMagic.DOMAIN).getBoolean(ArcaneMagicConstants.PLACED_ANALYZER_KEY)) {
            PacketHandler.sendToClient(new ProgressionUpdateToastPacket(false), (ServerPlayerEntity) placer);
            ((DataHolder) placer).getAdditionalData(ArcaneMagic.DOMAIN).putBoolean(ArcaneMagicConstants.PLACED_ANALYZER_KEY, true);
            ((DataHolder) placer).markAdditionalDataDirty();
        }
    }

    public RenderLayer[] getExtraRenderLayers() {
        return new RenderLayer[]{ RenderLayer.getTranslucent() };
    }
}
