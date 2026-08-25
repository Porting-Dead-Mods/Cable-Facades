package com.portingdeadmods.cable_facades.mixins.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkVertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ChunkVertexConsumer.class, remap = false)
public abstract class ChunkVertexConsumerMixin {
    @Shadow
    private int vertexIndex;

    @ModifyExpressionValue(
            method = "potentiallyEndVertex",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/render/chunk/translucent_sorting/TranslucentGeometryCollector;appendQuad([Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexEncoder$Vertex;Lnet/caffeinemc/mods/sodium/client/model/quad/properties/ModelQuadFacing;I)Z"
            )
    )
    private boolean cableFacades$resetVertexIndexOnDiscardedQuad(boolean discarded) {
        if (discarded) {
            this.vertexIndex = 0;
        }
        return discarded;
    }
}
