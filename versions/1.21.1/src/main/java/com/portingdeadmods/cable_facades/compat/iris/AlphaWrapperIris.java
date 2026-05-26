package com.portingdeadmods.cable_facades.compat.iris;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

public class AlphaWrapperIris extends VertexConsumerWrapper implements BlockSensitiveBufferBuilder {
    public AlphaWrapperIris(VertexConsumer consumer) {
        super(consumer);
    }

    @Override
    public VertexConsumer setColor(int color) {
        super.setColor(color & 0x88FFFFFF);
        return this;
    }

    @Override
    public void beginBlock(int blockId, byte renderType, byte blockLayer, int blockX, int blockY, int blockZ) {
        if (parent instanceof BlockSensitiveBufferBuilder iris) {
            iris.beginBlock(blockId, renderType, blockLayer, blockX, blockY, blockZ);
        }
    }

    @Override
    public void overrideBlock(int blockId) {
        if (parent instanceof BlockSensitiveBufferBuilder iris) {
            iris.overrideBlock(blockId);
        }
    }

    @Override
    public void restoreBlock() {
        if (parent instanceof BlockSensitiveBufferBuilder iris) {
            iris.restoreBlock();
        }
    }

    @Override
    public void endBlock() {
        if (parent instanceof BlockSensitiveBufferBuilder iris) {
            iris.endBlock();
        }
    }

    @Override
    public void ignoreMidBlock(boolean ignore) {
        if (parent instanceof BlockSensitiveBufferBuilder iris) {
            iris.ignoreMidBlock(ignore);
        }
    }
}
