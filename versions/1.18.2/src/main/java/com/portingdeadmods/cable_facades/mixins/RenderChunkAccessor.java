package com.portingdeadmods.cable_facades.mixins;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkRenderDispatcher.RenderChunk.class)
public interface RenderChunkAccessor {
    @Accessor("origin")
    BlockPos.MutableBlockPos cableFacades$getOrigin();
}
