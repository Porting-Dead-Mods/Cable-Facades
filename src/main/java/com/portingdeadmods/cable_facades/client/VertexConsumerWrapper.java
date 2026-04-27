package com.portingdeadmods.cable_facades.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class VertexConsumerWrapper implements VertexConsumer {

    protected final VertexConsumer parent;

    public VertexConsumerWrapper(VertexConsumer parent) {
        this.parent = parent;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        parent.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        parent.color(red, green, blue, alpha);
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        parent.uv(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        parent.overlayCoords(u, v);
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        parent.uv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        parent.normal(x, y, z);
        return this;
    }

    @Override
    public void endVertex() {
        parent.endVertex();
    }

    @Override
    public void defaultColor(int red, int green, int blue, int alpha) {
        parent.defaultColor(red, green, blue, alpha);
    }

    @Override
    public void unsetDefaultColor() {
        parent.unsetDefaultColor();
    }
}
