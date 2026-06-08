package com.portingdeadmods.cable_facades.compat.iris;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

final class TaggingVertexConsumer extends VertexConsumerWrapper {
    private final int blockId;
    private final byte lightEmission;
    private final BlockPos pos;

    TaggingVertexConsumer(VertexConsumer parent, int blockId, byte lightEmission, BlockPos pos) {
        super(parent);
        this.blockId = blockId;
        this.lightEmission = lightEmission;
        this.pos = pos;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        IrisLoadedCompat.tagCurrentVertex(parent, blockId, lightEmission, pos);
        return super.addVertex(x, y, z);
    }
}
