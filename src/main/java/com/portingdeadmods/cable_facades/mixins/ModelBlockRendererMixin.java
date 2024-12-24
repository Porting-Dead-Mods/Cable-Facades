package com.portingdeadmods.cable_facades.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.events.GameClientEvents;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelBlockRenderer.class)
public class ModelBlockRendererMixin {
    @WrapOperation(method = "putQuadData", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;[FFFFF[IIZ)V"))
    private void wrapPutQuadData(VertexConsumer instance, PoseStack.Pose f7, BakedQuad f8, float[] f3, float f4, float f5, float f, float f1, int[] f2, int h, boolean k, Operation<Void> original) {
        original.call(instance, f7, f8, f3, f4, f5, f, GameClientEvents.facadeTransparency && GameClientEvents.RENDERING_FACADE.get() ? 0.5f : f1, f2, h, k);
    }
}
