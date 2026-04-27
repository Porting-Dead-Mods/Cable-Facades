package com.portingdeadmods.cable_facades.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.portingdeadmods.cable_facades.client.render.ChunkFacadeBaker;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ChunkRenderDispatcher.RenderChunk.RebuildTask.class)
public abstract class ChunkRebuildTaskMixin {

    @Inject(
            method = "compile",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"
            )
    )
    private void cableFacades$bakeFacades(float camX, float camY, float camZ, ChunkBufferBuilderPack pack,
                                          CallbackInfoReturnable<ChunkRenderDispatcher.RenderChunk.RebuildTask.CompileResults> cir,
                                          @Local(ordinal = 0) BlockPos origin,
                                          @Local RenderChunkRegion region,
                                          @Local Set<RenderType> usedTypes) {
        if (region == null) return;
        ChunkFacadeBaker.bakeSection(origin, region, pack, usedTypes);
    }
}
