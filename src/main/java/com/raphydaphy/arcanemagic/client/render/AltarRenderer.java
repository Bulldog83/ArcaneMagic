package com.raphydaphy.arcanemagic.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raphydaphy.arcanemagic.block.entity.AltarBlockEntity;
import com.raphydaphy.arcanemagic.util.ArcaneMagicUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

public class AltarRenderer extends BlockEntityRenderer<AltarBlockEntity> {
    public AltarRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}
    
    public AltarRenderer get() {
    	return this;
    }

	public void render(AltarBlockEntity entity, float partialTicks, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity != null) {
            ItemStack stack = entity.getInvStack(0);
            float ticks = ArcaneMagicUtils.lerp(entity.ticks - 1, entity.ticks, partialTicks);

            if (!stack.isEmpty()) {
            	matrices.push();

                DiffuseLighting.enable();
                DiffuseLighting.enableGuiDepthLighting();
                RenderSystem.enableLighting();
                RenderSystem.disableRescaleNormal();
                matrices.translate(0.5, 0.9 + Math.sin((Math.PI / 180) * (ticks * 4)) / 15, 0.5);
                RenderSystem.rotatef(2 * ticks, 0, 1, 0);
                MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers);

                matrices.pop();
            }
        }
    }
}
