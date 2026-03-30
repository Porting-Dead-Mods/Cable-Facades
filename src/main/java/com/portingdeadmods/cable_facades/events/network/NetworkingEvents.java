package com.portingdeadmods.cable_facades.events.network;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.networking.s2c.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CFMain.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class NetworkingEvents {
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(CFMain.MODID).versioned("2");
        registrar.playToClient(AddFacadedBlocksPayload.TYPE, AddFacadedBlocksPayload.STREAM_CODEC, AddFacadedBlocksPayload::handle);
        registrar.playToClient(RemoveFacadedBlocksPayload.TYPE, RemoveFacadedBlocksPayload.STREAM_CODEC, RemoveFacadedBlocksPayload::handle);
        registrar.playToClient(AddFacadePayload.TYPE, AddFacadePayload.STREAM_CODEC, AddFacadePayload::handle);
        registrar.playToClient(RemoveFacadePayload.TYPE, RemoveFacadePayload.STREAM_CODEC, RemoveFacadePayload::handle);
        registrar.playToClient(AddDirectionalFacadePayload.TYPE, AddDirectionalFacadePayload.STREAM_CODEC, AddDirectionalFacadePayload::handle);
        registrar.playToClient(RemoveDirectionalFacadePayload.TYPE, RemoveDirectionalFacadePayload.STREAM_CODEC, RemoveDirectionalFacadePayload::handle);
    }
}
