package com.portingdeadmods.cable_facades.events.client;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.client.renderer.item.FacadeItemModel;
import com.portingdeadmods.cable_facades.client.renderer.item.FacadeItemSpecialRenderer;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

@EventBusSubscriber(modid = CFMain.MODID, value = Dist.CLIENT)
public final class ClientRegisterEvents {
    public static final Identifier FACADE_OUTLINE_ID = Identifier.fromNamespaceAndPath(CFMain.MODID, "item/facade_outline");
    public static final Identifier FACADE_ITEM_MODEL_ID = Identifier.fromNamespaceAndPath(CFMain.MODID, "facade");
    public static final Identifier FACADE_SPECIAL_RENDERER_ID = Identifier.fromNamespaceAndPath(CFMain.MODID, "facade");

    private ClientRegisterEvents() {
    }

    @SubscribeEvent
    public static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(FACADE_ITEM_MODEL_ID, FacadeItemModel.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(FACADE_SPECIAL_RENDERER_ID, FacadeItemSpecialRenderer.Unbaked.MAP_CODEC);
    }
}
