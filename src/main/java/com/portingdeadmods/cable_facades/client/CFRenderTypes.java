package com.portingdeadmods.cable_facades.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;

public final class CFRenderTypes {
    public static final RenderType TRANSLUCENT_FACADE = RenderType.create("translucent_facade", RenderSetup.builder(RenderPipelines.TRANSLUCENT_BLOCK)
            .useLightmap()
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS, () -> RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, true))
            .affectsCrumbling()
            .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
            .sortOnUpload()
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
            .createRenderSetup());

}
