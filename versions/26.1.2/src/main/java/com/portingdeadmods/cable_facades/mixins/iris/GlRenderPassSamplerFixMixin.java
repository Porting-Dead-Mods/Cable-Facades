package com.portingdeadmods.cable_facades.mixins.iris;

import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlRenderPipeline;
import com.mojang.blaze3d.opengl.Uniform;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlRenderPass")
public abstract class GlRenderPassSamplerFixMixin {
    @Inject(method = "draw(II)V", at = @At("HEAD"), remap = false)
    private void cableFacades$populateOnDraw(int firstVertex, int vertexCount, CallbackInfo ci) {
        cableFacades$populateIrisSamplers();
    }

    @Inject(method = "drawIndexed(IIII)V", at = @At("HEAD"), remap = false)
    private void cableFacades$populateOnDrawIndexed(int baseVertex, int firstIndex, int indexCount, int instanceCount, CallbackInfo ci) {
        cableFacades$populateIrisSamplers();
    }

    @Inject(method = "drawMultipleIndexed", at = @At("HEAD"), remap = false)
    private void cableFacades$populateOnDrawMultiple(CallbackInfo ci) {
        cableFacades$populateIrisSamplers();
    }

    private void cableFacades$populateIrisSamplers() {
        GlRenderPassAccessor accessor = (GlRenderPassAccessor) this;
        GlRenderPipeline pipeline = accessor.cableFacades$getPipeline();
        if (pipeline == null) return;
        GlProgram program = pipeline.program();
        if (!(program instanceof ExtendedShader)) return;

        HashMap<String, Object> samplers = accessor.cableFacades$getSamplers();
        Object placeholder = null;
        for (Object existing : samplers.values()) {
            placeholder = existing;
            break;
        }
        if (placeholder == null) return;

        for (Map.Entry<String, Uniform> entry : program.getUniforms().entrySet()) {
            if (entry.getValue() instanceof Uniform.Sampler && !samplers.containsKey(entry.getKey())) {
                samplers.put(entry.getKey(), placeholder);
            }
        }
    }
}
