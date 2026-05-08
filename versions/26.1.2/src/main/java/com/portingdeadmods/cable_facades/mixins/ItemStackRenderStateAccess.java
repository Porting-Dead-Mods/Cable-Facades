package com.portingdeadmods.cable_facades.mixins;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.class)
public interface ItemStackRenderStateAccess {
    @Accessor("activeLayerCount")
    int cableFacades$getActiveLayerCount();

    @Accessor("layers")
    ItemStackRenderState.LayerRenderState[] cableFacades$getLayers();
}
