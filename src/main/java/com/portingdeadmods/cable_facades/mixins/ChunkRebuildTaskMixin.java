package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.client.render.ChunkFacadeBaker;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ChunkRenderDispatcher.RenderChunk.RebuildTask.class)
public abstract class ChunkRebuildTaskMixin {

    @Shadow
    @Final
    ChunkRenderDispatcher.RenderChunk this$1;

    @Inject(
            method = "compile",
            at = @At("HEAD")
    )
    private void cableFacades$bakeFacades(float camX, float camY, float camZ,
                                          ChunkRenderDispatcher.CompiledChunk compiledChunk,
                                          ChunkBufferBuilderPack pack,
                                          CallbackInfoReturnable<Set<BlockEntity>> cir) {
        RenderChunkRegion region = ((RebuildTaskAccessor) this).cableFacades$getRegion();
        if (region == null) return;
        BlockPos origin = ((RenderChunkAccessor) (Object) this.this$1).cableFacades$getOrigin().immutable();
        Set<RenderType> hasLayer = ((CompiledChunkAccessor) (Object) compiledChunk).cableFacades$getHasLayer();
        ChunkFacadeBaker.bakeSection(origin, region, pack, hasLayer);
    }
}
