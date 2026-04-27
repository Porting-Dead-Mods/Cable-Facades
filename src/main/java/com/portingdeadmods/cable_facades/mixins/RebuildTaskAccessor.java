package com.portingdeadmods.cable_facades.mixins;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkRenderDispatcher.RenderChunk.RebuildTask.class)
public interface RebuildTaskAccessor {
    @Accessor("region")
    RenderChunkRegion cableFacades$getRegion();
}
