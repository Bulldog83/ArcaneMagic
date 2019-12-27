package com.raphydaphy.arcanemagic.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.raphydaphy.arcanemagic.block.base.OrientableBlockBase;
import com.raphydaphy.arcanemagic.block.entity.CrystalInfuserBlockEntity;
import com.raphydaphy.arcanemagic.util.ArcaneMagicUtils;
import com.raphydaphy.arcanemagic.util.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;

public class CrystalInfuserRenderer extends BlockEntityRenderer<CrystalInfuserBlockEntity> {
    public CrystalInfuserRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	public void render(CrystalInfuserBlockEntity entity, float partialTicks, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity != null) {
            float ticks = ArcaneMagicUtils.lerp(entity.ticksExisted - 1, entity.ticksExisted, partialTicks);

            ItemStack equipment = entity.getInvStack(0);
            ItemStack binder = entity.getInvStack(1);
            ItemStack crystal = entity.getInvStack(2);

            float craftingTime = entity.getCraftingTime();
            boolean active = entity.isActive() && craftingTime > 10;

            if (active && craftingTime > 8000 && craftingTime > 8150) {
                //ParticleUtil.spawnGlowParticle(entity.getWorld(),entity.getPos().x + .5f, entity.getPos().y + 1.1f, entity.getPos().z + .5f,0, 0, 0, 1, 1, 1, 0.1f, 0,0.5f, 100);
            } else {
            	double renderX = dispatcher.camera.getPos().x;
                double renderY = dispatcher.camera.getPos().y;
                double renderZ = dispatcher.camera.getPos().z;

                Vector3f vec_1 = new Vector3f(1F, 0F, 0F);
                Vector3f vec_2 = new Vector3f(0F, 1F, 0F);
                Vector3f vec_3 = new Vector3f(0F, 0F, 1F);
                vec_1.reciprocal();
            	vec_2.reciprocal();
            	vec_3.reciprocal();
                
                
                matrices.push();
                DiffuseLighting.enable();
                RenderSystem.enableLighting();
                RenderSystem.disableRescaleNormal();

                craftingTime /= 2f;

                float scale = 0.5f;
                if (active && craftingTime > 7500) {
                    scale = scale - ((craftingTime - 7500) / 500f) * scale;
                }

                matrices.translate(renderX, renderY, renderZ);
                if (!active) {
                    Direction dir = entity.getWorld().getBlockState(entity.getPos()).get(OrientableBlockBase.FACING);
                    RenderUtils.rotateTo(dir);
                }

                // Render Equipment
                if (!equipment.isEmpty()) {
                    matrices.push();

                    if (active) {
                        matrices.translate(.5, 1 + Math.sin((Math.PI / 180) * (ticks * 4)) / 15, .5);
                        matrices.multiply(new Quaternion(vec_2, 2 * ticks, true));
                    } else {
                        matrices.translate(.5, .635, .5);
                        matrices.multiply(new Quaternion(vec_1, 90, true));
                        if (equipment.getItem() instanceof ArmorItem && ((ArmorItem) equipment.getItem()).getSlotType() == EquipmentSlot.HEAD) {
                            matrices.translate(0, -0.07, 0);
                        }
                        matrices.translate(0, -.08, 0);
                    }
                    matrices.scale(0.8F, 0.8F, 0.8F);
                    MinecraftClient.getInstance().getItemRenderer().renderItem(equipment, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers);

                    matrices.pop();
                }

                ticks += 400;

                scale = 0.6f;
                if (active && craftingTime > 3750) {
                    scale = scale - ((craftingTime - 3750) / 500f) * scale;
                }

                // Render Binder
                if (!binder.isEmpty()) {
                    matrices.push();

                    if (active) {
                        float inverseRadius = (craftingTime) / 1000f + 3;
                        float posX = (float) (.5 + Math.cos((Math.PI / 180) * (ticks * 2)) / inverseRadius);
                        float posY = (float) (1 - Math.sin((Math.PI / 180) * (ticks * 4)) / 8);
                        float posZ = (float) (.5 + Math.sin((Math.PI / 180) * (ticks * 2)) / inverseRadius);
                        matrices.translate(posX, posY, posZ);
                        matrices.multiply(new Quaternion(vec_2, 2 * ticks, true));
                    } else {
                        matrices.translate(.35, .635, .3);
                        if (binder.getItem() == Items.LAPIS_LAZULI) {
                        	matrices.multiply(new Quaternion(vec_1, 90, true));
                        	matrices.multiply(new Quaternion(vec_3, 90, true));
                            if (!equipment.isEmpty()) {
                            	Vector3f vec = new Vector3f(1F, 1F, 0F);
                            	vec.reciprocal();
                            	matrices.multiply(new Quaternion(vec, 10, true));
                            }
                            matrices.translate(.07, -.1, 0);
                        } else if (binder.getItem() == Items.REDSTONE) {
                        	matrices.multiply(new Quaternion(vec_1, 90, true));

                            if (!equipment.isEmpty()) {
                            	matrices.multiply(new Quaternion(vec_1, -10, true));
                            	matrices.multiply(new Quaternion(vec_2, 10, true));
                            }

                        }
                    }

                    matrices.scale(scale, scale, scale);
                    MinecraftClient.getInstance().getItemRenderer().renderItem(binder, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers);
                    matrices.pop();
                }

                ticks += 90;

                // Render Crystal
                if (!crystal.isEmpty()) {
                    matrices.push();

                    if (active) {
                        float inverseRadius = (craftingTime) / 1000f + 3;
                        float posX = (float) (0.5 + Math.cos((Math.PI / 180) * (ticks * 2)) / inverseRadius);
                        float posY = (float) (1 - Math.sin((Math.PI / 180) * ((ticks + 45) * 4)) / 8);
                        float posZ = (float) (0.5 + Math.sin((Math.PI / 180) * (ticks * 2)) / inverseRadius);
                        matrices.translate(posX, posY, posZ);
                        matrices.multiply(new Quaternion(vec_2, 2 * ticks, true));
                    } else {
                        matrices.translate(.69, .635, .6);
                        matrices.multiply(new Quaternion(vec_1, 90, true));
                        matrices.multiply(new Quaternion(vec_3, 50, true));

                        if (!equipment.isEmpty()) {
                            matrices.translate(0, 0, -.01);
                            Vector3f vec = new Vector3f(1F, -1F, 0F);
                        	vec.reciprocal();
                        	matrices.multiply(new Quaternion(vec, 10, true));
                        }
                    }

                    matrices.scale(scale, scale, scale);
                    MinecraftClient.getInstance().getItemRenderer().renderItem(crystal, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers);
                    matrices.pop();
                }

                matrices.pop();
            }

        }
    }
}
