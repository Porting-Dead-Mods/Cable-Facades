package com.portingdeadmods.cable_facades.rendeer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;

public class ClientStuff {

    public static FacadeItemRenderer FACADE_ITEM_RENDERER;

    public static void init() {
        Minecraft minecraft = Minecraft.getInstance();
        BlockRenderDispatcher blockRenderDispatcher = minecraft.getBlockRenderer();
        EntityModelSet entityModelSet = minecraft.getEntityModels();
        FACADE_ITEM_RENDERER = new FacadeItemRenderer(blockRenderDispatcher, entityModelSet);
    }
}
