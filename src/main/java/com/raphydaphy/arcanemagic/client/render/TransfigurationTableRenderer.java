package com.raphydaphy.arcanemagic.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raphydaphy.arcanemagic.block.TransfigurationTableBlock;
import com.raphydaphy.arcanemagic.block.entity.TransfigurationTableBlockEntity;
import com.raphydaphy.arcanemagic.util.RenderUtils;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Quaternion;

public class TransfigurationTableRenderer extends BlockEntityRenderer<TransfigurationTableBlockEntity> {
    public TransfigurationTableRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	public void render(TransfigurationTableBlockEntity entity, float partialTicks, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		double renderX = dispatcher.camera.getPos().x;
        double renderY = dispatcher.camera.getPos().y;
        double renderZ = dispatcher.camera.getPos().z;		
		
        matrices.push();
        matrices.translate(renderX, renderY, renderZ);
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

                        matrices.push();
                        DiffuseLighting.enable();
                        RenderSystem.enableLighting();
                        matrices.translate(.69 - .19 * row, 0.695, .69 - .19 * col);
                        
                        Vector3f vec_1 = new Vector3f(0F, 1F, 0F);
            			vec_1.reciprocal();
                        if (!MinecraftClient.getInstance().getItemRenderer().getHeldItemModel(stack, entity.getWorld(), null).hasDepthInGui()) {
                        	matrices.translate(0, -0.064, 0);
                        	Vector3f vec_2 = new Vector3f(1F, 0F, 0F);
                			vec_2.reciprocal();                
                            matrices.multiply(new Quaternion(vec_2, 90, true));
                            matrices.multiply(new Quaternion(vec_1, 180, true));
                        } else {
                        	matrices.multiply(new Quaternion(vec_1, -90, true));
                        }

                        matrices.scale(.14F, .14F, .14F);
                        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.NONE, light, overlay, matrices, vertexConsumers);
                        matrices.pop();
                    }
                }
            }
        }
        matrices.pop();
    }
}
