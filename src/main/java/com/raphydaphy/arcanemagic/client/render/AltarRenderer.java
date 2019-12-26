package com.raphydaphy.arcanemagic.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raphydaphy.arcanemagic.block.entity.AltarBlockEntity;
import com.raphydaphy.arcanemagic.util.ArcaneMagicUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.item.ItemStack;

public class AltarRenderer extends BlockEntityRenderer<AltarBlockEntity> {
    public void render(AltarBlockEntity entity, double renderX, double renderY, double renderZ, float partialTicks, int destroyStage) {
        super.render(entity, renderX, renderY, renderZ, partialTicks, destroyStage);

        if (entity != null) {
            ItemStack stack = entity.getInvStack(0);
            float ticks = ArcaneMagicUtils.lerp(entity.ticks - 1, entity.ticks, partialTicks);

            if (!stack.isEmpty()) {
                RenderSystem.pushMatrix();

                DiffuseLighting.enable();
                DiffuseLighting.enableGuiDepthLighting();
                RenderSystem.enableLighting();
                RenderSystem.disableRescaleNormal();
                RenderSystem.translated(renderX + .5, renderY + 0.9 + Math.sin((Math.PI / 180) * (ticks * 4)) / 15, renderZ + .5);
                RenderSystem.rotated(2 * ticks, 0, 1, 0);
                MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND);

                RenderSystem.popMatrix();
            }
        }
    }
}
