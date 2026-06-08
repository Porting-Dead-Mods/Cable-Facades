package com.portingdeadmods.cable_facades.mixins.iris;

import com.mojang.blaze3d.opengl.GlRenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.HashMap;

@Mixin(targets = "com.mojang.blaze3d.opengl.GlRenderPass")
public interface GlRenderPassAccessor {
    @Accessor("pipeline")
    GlRenderPipeline cableFacades$getPipeline();

    @Accessor("samplers")
    HashMap<String, Object> cableFacades$getSamplers();
}
