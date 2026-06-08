package com.portingdeadmods.cable_facades.compat.iris;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.mixins.sodium.ChunkVertexConsumerAccessor;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.buffers.ChunkVertexConsumer;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.sodium.terrain.ChunkVertexExtension;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;

final class IrisLoadedCompat {
    private IrisLoadedCompat() {
    }

    static boolean areShadersEnabled() {
        return IrisApi.getInstance().isShaderPackInUse();
    }

    static VertexConsumer wrapAlpha(VertexConsumer consumer) {
        return new AlphaWrapperIris(consumer);
    }

    static int blockIdFor(BlockState state) {
        var ids = WorldRenderingSettings.INSTANCE.getBlockStateIds();
        return ids == null ? -1 : ids.getInt(state);
    }

    static void tagCurrentVertex(VertexConsumer buffer, int blockId, byte lightEmission, BlockPos pos) {
        if (blockId == -1) return;
        ChunkVertexConsumer raw = unwrap(buffer);
        if (raw == null) return;
        ChunkVertexConsumerAccessor accessor = (ChunkVertexConsumerAccessor) raw;
        ChunkVertexEncoder.Vertex[] vertices = accessor.cableFacades$getVertices();
        int index = accessor.cableFacades$getVertexIndex();
        if (index < 0 || index >= vertices.length) return;
        ((ChunkVertexExtension) vertices[index]).iris$setData(
                lightEmission,
                (byte) 0,
                blockId,
                SectionPos.sectionRelative(pos.getX()),
                SectionPos.sectionRelative(pos.getY()),
                SectionPos.sectionRelative(pos.getZ())
        );
    }

    private static ChunkVertexConsumer unwrap(VertexConsumer buffer) {
        VertexConsumer current = buffer;
        if (current instanceof AlphaWrapperIris alpha) {
            current = alpha.delegate();
        }
        return current instanceof ChunkVertexConsumer cvc ? cvc : null;
    }
}
