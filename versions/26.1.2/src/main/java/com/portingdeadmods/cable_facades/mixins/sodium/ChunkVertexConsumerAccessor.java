package com.portingdeadmods.cable_facades.mixins.sodium;

import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkVertexConsumer;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ChunkVertexConsumer.class, remap = false)
public interface ChunkVertexConsumerAccessor {
    @Accessor("vertices")
    ChunkVertexEncoder.Vertex[] cableFacades$getVertices();

    @Accessor("vertexIndex")
    int cableFacades$getVertexIndex();
}
