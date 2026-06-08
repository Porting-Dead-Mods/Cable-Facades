package com.portingdeadmods.cable_facades.compat.iris;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;

final class AlphaWrapperIris extends VertexConsumerWrapper {
    private static final int ALPHA_MASK = 0x88FFFFFF;

    AlphaWrapperIris(VertexConsumer consumer) {
        super(consumer);
    }

    @Override
    public VertexConsumer setColor(int color) {
        super.setColor(color & ALPHA_MASK);
        return this;
    }

    VertexConsumer delegate() {
        return parent;
    }
}
