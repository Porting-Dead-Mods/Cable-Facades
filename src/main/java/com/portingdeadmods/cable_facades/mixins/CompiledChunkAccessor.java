package com.portingdeadmods.cable_facades.mixins;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ChunkRenderDispatcher.CompiledChunk.class)
public interface CompiledChunkAccessor {
    @Accessor("hasLayer")
    Set<RenderType> cableFacades$getHasLayer();

    @Accessor("hasBlocks")
    Set<RenderType> cableFacades$getHasBlocks();
}
