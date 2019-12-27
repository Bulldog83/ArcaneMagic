package com.raphydaphy.arcanemagic.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.raphydaphy.arcanemagic.ArcaneMagic;
import com.raphydaphy.arcanemagic.block.entity.MixerBlockEntity;
import com.raphydaphy.arcanemagic.util.ArcaneMagicUtils;
import com.raphydaphy.arcanemagic.util.RenderUtils;
import com.raphydaphy.arcanemagic.util.UVSet;
import io.github.prospector.silk.fluid.FluidInstance;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Quaternion;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class MixerRenderer extends BlockEntityRenderer<MixerBlockEntity> {
    
	public MixerRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	private static RenderUtils.TextureBounds[] ring = {
            new UVSet(8, 0, 8, 8), // Bottom
            new UVSet(8, 0, 8, 8), // Top
            new UVSet(8, 8, 8, 2), // North
            new UVSet(8, 8, 8, 2), // South
            new UVSet(8, 8, 8, 2), // West
            new UVSet(8, 8, 8, 2)}; // East
    private static Identifier ringTexture = new Identifier(ArcaneMagic.DOMAIN, "textures/block/mixer_tank_overlay.png");
    private static Identifier waterTexture = new Identifier("textures/block/water_still.png");

    private static List<MixerRenderInstance> renderQueue = new ArrayList<>();

    public static void renderMixers() {
        if (renderQueue.size() > 0) {
            PlayerEntity player = MinecraftClient.getInstance().player;

            renderQueue.sort((one, two) ->
            {
                BlockPos posOne = one.entity.getPos();
                BlockPos posTwo = two.entity.getPos();

                double distanceOne = Math.abs(posOne.getX() - player.getX()) * Math.abs(posOne.getY() - player.getY()) * Math.abs(posOne.getZ() - player.getZ());
                double distanceTwo = Math.abs(posTwo.getX() - player.getX()) * Math.abs(posTwo.getY() - player.getY()) * Math.abs(posTwo.getZ() - player.getZ());

                return Double.compare(distanceTwo, distanceOne);
            });

            RenderSystem.pushMatrix();

            DiffuseLighting.enable();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA.value, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA.value);
            GlStateManager.depthMask(false);
            GlStateManager.disableCull();
            RenderSystem.enableAlphaTest();
            RenderSystem.alphaFunc(GL11.GL_GREATER, 0.003921569F);

            for (MixerRenderInstance instance : renderQueue) {
                renderTank(instance.entity, instance.renderX, instance.renderY, instance.renderZ);
            }

            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            DiffuseLighting.disable();

            RenderSystem.popMatrix();
            renderQueue.clear();
        }
    }

    private static void renderTank(MixerBlockEntity entity, double renderX, double renderY, double renderZ) {
        int water = 0;
        FluidInstance[] fluids = entity.getFluids(null);
        if (fluids.length >= 1 && fluids[0] != null && fluids[0].getFluid() == Fluids.WATER) {
            water = (int) (fluids[0].getAmount() / (float) MixerBlockEntity.MAX_FLUID * 12);
        }
        if (water > 0) {
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder builder = tess.getBuffer();

            double pixel = 1d / 16d;

            int frame = 0;

            World world;
            if ((world = entity.getWorld()) != null) frame = (int) ((world.getTime() / 2) % 32);

            MinecraftClient.getInstance().getTextureManager().bindTexture(waterTexture);
            builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);
            int waterColor = BiomeColors.getWaterColor(entity.getWorld(), entity.getPos());
            RenderUtils.renderBox(builder, renderX + pixel * 3, renderY + pixel * 1, renderZ + pixel * 3, renderX + pixel * 13, renderY + pixel * (1 + water), renderZ + pixel * 13, (waterColor >> 16 & 255), (waterColor >> 8 & 255), (waterColor & 255), 200, new RenderUtils.TextureBounds[]{
                    new RenderUtils.TextureBounds(0, frame * 16, 12, 10 + frame * 16, 16, 512),
                    new RenderUtils.TextureBounds(0, frame * 16, 12, 10 + frame * 16, 16, 512),
                    new RenderUtils.TextureBounds(0, frame * 16, 12, water + frame * 16, 16, 512),
                    new RenderUtils.TextureBounds(0, frame * 16, 12, water + frame * 16, 16, 512),
                    new RenderUtils.TextureBounds(0, frame * 16, 12, water + frame * 16, 16, 512),
                    new RenderUtils.TextureBounds(0, frame * 16, 12, water + frame * 16, 16, 512)}, new int[]{1, 1, 1, 1, 1, 1});

            tess.draw();
        }
    }

    private void renderRing(MixerBlockEntity entity, MatrixStack matrices, double renderX, double renderY, double renderZ) {
    	matrices.push();
        GlStateManager.disableCull();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder builder = tess.getBuffer();
        double pixel = 1d / 16d;
        MinecraftClient.getInstance().getTextureManager().bindTexture(ringTexture);
        builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE_COLOR_NORMAL);
        int waterColor = BiomeColors.getWaterColor(entity.getWorld(), entity.getPos());
        RenderUtils.renderBox(builder, renderX + pixel * 4, renderY + pixel * 14, renderZ + pixel * 4,
                renderX + pixel * 12, renderY + 1, renderZ + pixel * 12, (waterColor >> 16 & 255), (waterColor >> 8 & 255), (waterColor & 255), 255, ring, new int[]{1, 1, 1, 1, 1, 1});
        tess.draw();
        GlStateManager.enableCull();
        matrices.pop();
    }

    public void render(MixerBlockEntity entity, float partialTicks, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (entity != null) {            
        	double renderX = dispatcher.camera.getPos().x;
            double renderY = dispatcher.camera.getPos().y;
            double renderZ = dispatcher.camera.getPos().z;
        	
        	if (entity.isBottom()) {
                renderRing(entity, matrices, renderX, renderY, renderZ);
                ItemStack stack = entity.getInvStack(0);
                float ticks = ArcaneMagicUtils.lerp(entity.ticks - 1, entity.ticks, partialTicks);
                if (!stack.isEmpty()) {
                	matrices.push();

                    DiffuseLighting.enable();
                    RenderSystem.enableLighting();
                    RenderSystem.disableRescaleNormal();
                    matrices.translate(renderX + .5, renderY + 0.35 + Math.sin((Math.PI / 180) * (ticks * 4)) / 30, renderZ + .5);
                    Vector3f vec = new Vector3f(0F, 1F, 0F);
        			vec.reciprocal();                
                    matrices.multiply(new Quaternion(vec, 2 * ticks, true));
                    matrices.scale(0.7F, 0.7F, 0.7F);
                    MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformation.Mode.GROUND, light, overlay, matrices, vertexConsumers);

                    matrices.pop();
                }
            } else {
                renderQueue.add(new MixerRenderInstance(entity, renderX, renderY, renderZ));
            }
        }
    }

    private class MixerRenderInstance {
        MixerBlockEntity entity;

        double renderX;
        double renderY;
        double renderZ;

        MixerRenderInstance(MixerBlockEntity entity, double renderX, double renderY, double renderZ) {
            this.entity = entity;
            this.renderX = renderX;
            this.renderY = renderY;
            this.renderZ = renderZ;
        }
    }
}
