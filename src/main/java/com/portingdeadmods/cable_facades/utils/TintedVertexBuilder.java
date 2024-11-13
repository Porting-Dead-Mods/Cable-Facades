package com.portingdeadmods.cable_facades.utils;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

public class TintedVertexBuilder implements VertexConsumer {
    /** Base vertex builder */
    private final VertexConsumer inner;
    /** Tint color from 0-255 */
    private final int tintRed, tintGreen, tintBlue, tintAlpha;

    public TintedVertexBuilder(VertexConsumer inner, int tintRed, int tintGreen, int tintBlue, int tintAlpha) {
        this.inner = inner;
        this.tintRed = tintRed;
        this.tintGreen = tintGreen;
        this.tintBlue = tintBlue;
        this.tintAlpha = tintAlpha;
    }

    @Override
    public @NotNull VertexConsumer vertex(double v, double v1, double v2) {
        return inner.vertex(v, v1, v2);
    }

    @Override
    public @NotNull VertexConsumer color(int red, int green, int blue, int alpha) {
        return inner.color((red * tintRed) / 0xFF, (green * tintGreen) / 0xFF, (blue * tintBlue) / 0xFF, (alpha * tintAlpha) / 0xFF);
    }

    @Override
    public @NotNull VertexConsumer uv(float u, float v) {
        return inner.uv(u, v);
    }

    @Override
    public VertexConsumer overlayCoords(int i, int i1) {
        return null;
    }

    @Override
    public @NotNull VertexConsumer uv2(int i, int i1) {
        return inner.uv2(i, i1);
    }

    @Override
    public @NotNull VertexConsumer normal(float v, float v1, float v2) {
        return inner.normal(v, v1, v2);
    }

    @Override
    public void endVertex() {
        inner.endVertex();
    }

    @Override
    public void defaultColor(int i, int i1, int i2, int i3) {
    }

    @Override
    public void unsetDefaultColor() {
    }
}