package com.portingdeadmods.cable_facades.events;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.networking.s2c.AddFacadePayload;
import com.portingdeadmods.cable_facades.networking.s2c.AddFacadedBlocksPayload;
import com.portingdeadmods.cable_facades.networking.s2c.RemoveFacadePayload;
import com.portingdeadmods.cable_facades.networking.s2c.RemoveFacadedBlocksPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = CFMain.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class NetworkingEvents {
    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(CFMain.MODID);
        registrar.playToClient(AddFacadedBlocksPayload.TYPE, AddFacadedBlocksPayload.STREAM_CODEC, AddFacadedBlocksPayload::handle);
        registrar.playToClient(RemoveFacadedBlocksPayload.TYPE, RemoveFacadedBlocksPayload.STREAM_CODEC, RemoveFacadedBlocksPayload::handle);
        registrar.playToClient(AddFacadePayload.TYPE, AddFacadePayload.STREAM_CODEC, AddFacadePayload::handle);
        registrar.playToClient(RemoveFacadePayload.TYPE, RemoveFacadePayload.STREAM_CODEC, RemoveFacadePayload::handle);
    }
}
