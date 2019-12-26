package com.raphydaphy.arcanemagic.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.raphydaphy.arcanemagic.block.TransfigurationTableBlock;
import com.raphydaphy.arcanemagic.block.entity.TransfigurationTableBlockEntity;
import com.raphydaphy.arcanemagic.util.RenderUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.item.ItemStack;

public class TransfigurationTableRenderer extends BlockEntityRenderer<TransfigurationTableBlockEntity> {
    public void render(TransfigurationTableBlockEntity entity, double renderX, double renderY, double renderZ, float partialTicks, int destroyStage) {
        super.render(entity, renderX, renderY, renderZ, partialTicks, destroyStage);

        RenderSystem.pushMatrix();
        GlStateManager.translated(renderX, renderY, renderZ);
        RenderSystem.disableRescaleNormal();


        if (entity != null && entity.getWorld() != null) {
            BlockState state = entity.getWorld().getBlockState(entity.getPos());


            if (state.getBlock() instanceof TransfigurationTableBlock) {
                RenderUtils.rotateTo(state.get(TransfigurationTableBlock.FACING));
                for (int slot = 0; slot < 9; slot++) {
                    ItemStack stack = entity.getInvStack(slot);

                    if (!stack.isEmpty()) {
                        int row = slot % 3;
                        int col = slot / 3;

                        RenderSystem.pushMatrix();
                        DiffuseLighting.enable();
                        DiffuseLighting.enableGuiDepthLighting();
                        GlStateManager.enableLighting();
                        GlStateManager.translated(.69 - .19 * row, 0.695, .69 - .19 * col);
                        if (!MinecraftClient.getInstance().getItemRenderer().getModel(stack).hasDepthInGui()) {
                            GlStateManager.translated(0, -0.064, 0);
                            GlStateManager.rotated(90, 1, 0, 0);
                            GlStateManager.rotated(180, 0, 1, 0);
                        } else {
                            GlStateManager.rotated(-90, 0, 1, 0);
                        }

                        GlStateManager.scaled(.14, .14, .14);
                        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.NONE);
                        RenderSystem.popMatrix();
                    }
                }
            }
        }

        RenderSystem.popMatrix();
    }
}
