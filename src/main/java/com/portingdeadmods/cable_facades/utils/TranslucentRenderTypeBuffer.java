package com.portingdeadmods.cable_facades.utils;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class TranslucentRenderTypeBuffer implements MultiBufferSource {
    private static final Set<String> MAKE_TRANSPARENT = Set.of("entity_solid", "entity_cutout", "entity_cutout_no_cull", "entity_translucent", "entity_no_outline");

    /**
     * Base render type buffer
     */
    private final MultiBufferSource inner;
    /**
     * Calculated colors to pass into {@link TintedVertexBuilder}
     */
    private final int alpha, red, green, blue;

    /**
     * Creates a new instance of this class
     *
     * @param inner       Base render type buffer
     * @param alpha       Opacity of the item from 0 to 255. 255 is the end of the animation.
     */
    public TranslucentRenderTypeBuffer(MultiBufferSource inner, int alpha) {
        this.inner = inner;
        // alpha is a direct fade from 0 to 255
        this.alpha = Mth.clamp(alpha, 0, 0xFF);
        // RGB based on temp, fades from 0xB06020 tint to 0xFFFFFF
        int temp = Mth.clamp(0, 0, 0xFF);
        this.red = 0xFF - (temp * (0xFF - 0xB0) / 0xFF);
        this.green = 0xFF - (temp * (0xFF - 0x60) / 0xFF);
        this.blue = 0xFF - (temp * (0xFF - 0x20) / 0xFF);
    }

    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType type) {
        return new TintedVertexBuilder(inner.getBuffer(RenderType.translucent()), red, green, blue, alpha);
    }
}
