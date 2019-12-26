package com.raphydaphy.arcanemagic.core.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.raphydaphy.arcanemagic.init.ModRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.GlBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    private ClientWorld world;

    @Shadow
    private boolean vertexBufferObjectsEnabled;

    @Shadow
    private int field_4067;

    @Shadow
    private int field_4117;

    @Shadow
    private GlBuffer field_4087;

    @Shadow
    private int starsDisplayList;

    @Shadow
    private GlBuffer starsBuffer;

    @Inject(at = @At("HEAD"), method = "renderSky", cancellable = true)
    private void renderSky(float tickDelta, CallbackInfo info) {
        if (client.world.dimension.getType() == ModRegistry.VOID_DIM) {
            renderSoulDimensionSky(tickDelta);
            info.cancel();
        }
    }

    private void renderSoulDimensionSky(float tickDelta) {
        GlStateManager.disableTexture();
        Vec3d vec3d_1 = this.world.getSkyColor(this.client.gameRenderer.getCamera().getBlockPos(), tickDelta);
        float float_2 = (float) vec3d_1.x;
        float float_3 = (float) vec3d_1.y;
        float float_4 = (float) vec3d_1.z;
        RenderSystem.color3f(float_2, float_3, float_4);
        Tessellator tessellator_1 = Tessellator.getInstance();
        BufferBuilder bufferBuilder_1 = tessellator_1.getBufferBuilder();
        GlStateManager.depthMask(false);
        GlStateManager.enableFog();
        RenderSystem.color3f(float_2, float_3, float_4);
        if (this.vertexBufferObjectsEnabled) {
            this.field_4087.bind();
            GlStateManager.enableClientState(32884);
            GlStateManager.vertexPointer(3, 5126, 12, 0);
            this.field_4087.draw(7);
            GlBuffer.unbind();
            GlStateManager.disableClientState(32884);
        } else {
            GlStateManager.callList(this.field_4117);
        }
        GlStateManager.disableFog();
        GlStateManager.disableAlphaTest();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA.value, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA.value, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        DiffuseLighting.disableGuiDepthLighting();
DiffuseLighting.disable();
        float[] floats_1 = this.world.dimension.getBackgroundColor(this.world.getSkyAngle(tickDelta), tickDelta);
        float float_11 = 0;
        int int_2;
        float float_8, float_9, float_10, float_12;
        if (floats_1 != null) {
            GlStateManager.disableTexture();
            GlStateManager.shadeModel(7425);
            RenderSystem.pushMatrix();
            RenderSystem.rotatef(90.0F, 1.0F, 0.0F, 0.0F);
            RenderSystem.rotatef(MathHelper.sin(this.world.getSkyAngleRadians(tickDelta)) < 0.0F ? 180.0F : 0.0F, 0.0F, 0.0F, 1.0F);
            RenderSystem.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
            float_11 = floats_1[0];
            float_12 = floats_1[1];
            float float_7 = floats_1[2];
            bufferBuilder_1.begin(6, VertexFormats.POSITION_COLOR);
            bufferBuilder_1.vertex(0.0D, 100.0D, 0.0D).color(float_11, float_12, float_7, floats_1[3]).next();
            for (int_2 = 0; int_2 <= 16; ++int_2) {
                float_8 = (float) int_2 * 6.2831855F / 16.0F;
                float_9 = MathHelper.sin(float_8);
                float_10 = MathHelper.cos(float_8);
                bufferBuilder_1.vertex((double) (float_9 * 120.0F), (double) (float_10 * 120.0F), (double) (-float_10 * 40.0F * floats_1[3])).color(floats_1[0], floats_1[1], floats_1[2], 0.0F).next();
            }

            tessellator_1.draw();
            RenderSystem.popMatrix();
            GlStateManager.shadeModel(7424);
        }

        GlStateManager.enableTexture();
        GlStateManager.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA.value, GlStateManager.DstFactor.ONE.value, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.pushMatrix();

        float_11 = 1.0F - this.world.getRainGradient(tickDelta);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, float_11);
        RenderSystem.rotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        RenderSystem.rotatef(this.world.getSkyAngle(tickDelta) * 360.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.disableTexture();
        float float_17 = this.world.getStarsBrightness(tickDelta) * float_11;
        if (float_17 > 0.0F) {
            GlStateManager.color4f(float_17, float_17, float_17, float_17);
            if (this.vertexBufferObjectsEnabled) {
                this.starsBuffer.bind();
                GlStateManager.enableClientState(32884);
                GlStateManager.vertexPointer(3, 5126, 12, 0);
                this.starsBuffer.draw(7);
                GlBuffer.unbind();
                GlStateManager.disableClientState(32884);
            } else {
                GlStateManager.callList(this.starsDisplayList);
            }
        }

        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
        GlStateManager.enableFog();
        RenderSystem.popMatrix();
        GlStateManager.disableTexture();
        RenderSystem.color3f(0.0F, 0.0F, 0.0F);
        double cameraHeight = this.client.player.getCameraPosVec(tickDelta).y;

        if (this.world.dimension.method_12449()) {
            RenderSystem.color3f(float_2 * 0.2F + 0.04F, float_3 * 0.2F + 0.04F, float_4 * 0.6F + 0.1F);
        } else {
            RenderSystem.color3f(float_2, float_3, float_4);
        }

        RenderSystem.pushMatrix();
        GlStateManager.translatef(0.0F, -((float) (cameraHeight - 16.0D)), 0.0F);
        GlStateManager.callList(this.field_4067);
        RenderSystem.popMatrix();
        GlStateManager.enableTexture();
        GlStateManager.depthMask(true);
    }
}
