package com.portingdeadmods.cable_facades.compat.iris;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.portingdeadmods.cable_facades.client.VertexConsumerWrapper;
import net.coderbot.iris.vertices.BlockSensitiveBufferBuilder;

public class AlphaWrapperIris extends VertexConsumerWrapper implements BlockSensitiveBufferBuilder {

    public AlphaWrapperIris(VertexConsumer consumer) {
        super(consumer);
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        return super.color(red, green, blue, Math.min(alpha, 0x88));
    }

    @Override
    public void beginBlock(short block, short renderType, int localPosX, int localPosY, int localPosZ) {
        if (parent instanceof BlockSensitiveBufferBuilder iris) {
            iris.beginBlock(block, renderType, localPosX, localPosY, localPosZ);
        }
    }

    @Override
    public void endBlock() {
        if (parent instanceof BlockSensitiveBufferBuilder iris) {
            iris.endBlock();
        }
    }
}
