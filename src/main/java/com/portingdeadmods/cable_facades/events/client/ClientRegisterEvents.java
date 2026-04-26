package com.portingdeadmods.cable_facades.events.client;

import com.mojang.serialization.MapCodec;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.client.renderer.item.FacadeItemModel;
import com.portingdeadmods.cable_facades.client.renderer.item.FacadeItemSpecialRenderer;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

@EventBusSubscriber(modid = CFMain.MODID, value = Dist.CLIENT)
public final class ClientRegisterEvents {
    public static final FacadeItemSpecialRenderer FACADE_ITEM_RENDERER = new FacadeItemSpecialRenderer();
    public static final StandaloneModelKey<ItemModel> FACADE_OUTLINE = new StandaloneModelKey<>(
            () -> Identifier.fromNamespaceAndPath(CFMain.MODID, "item/facade_outline").toString());

//    @SubscribeEvent
//    public static void registerAdditionalModels(ModelEvent.RegisterStandalone event) {
//        event.register(FACADE_OUTLINE, SimpleUnbakedStandaloneModel.simpleModelWrapper());
//    }

    @SubscribeEvent
    public static void registerSpecialModeRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(Identifier.fromNamespaceAndPath(CFMain.MODID, "facade"), FacadeItemSpecialRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(Identifier.fromNamespaceAndPath(CFMain.MODID, "facade_block"), FacadeItemModel.Unbaked.MAP_CODEC);
    }
}
