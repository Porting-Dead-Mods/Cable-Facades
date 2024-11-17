package com.portingdeadmods.cable_facades.events;

import com.portingdeadmods.cable_facades.client.renderer.item.FacadeItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientStuff {
    public static final FacadeItemRenderer FACADE_ITEM_RENDERER = new FacadeItemRenderer();
}
