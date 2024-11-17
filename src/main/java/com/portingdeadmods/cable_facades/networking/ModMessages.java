package com.portingdeadmods.cable_facades.networking;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.networking.s2c.AddFacadePacket;
import com.portingdeadmods.cable_facades.networking.s2c.RemoveFacadePacket;
import com.portingdeadmods.cable_facades.networking.s2c.SyncFacadedBlocksS2C;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModMessages {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(CFMain.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        net.messageBuilder(SyncFacadedBlocksS2C.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncFacadedBlocksS2C::new)
                .encoder(SyncFacadedBlocksS2C::toBytes)
                .consumerMainThread(SyncFacadedBlocksS2C::handle)
                .add();

        net.messageBuilder(RemoveFacadePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(RemoveFacadePacket::new)
                .encoder(RemoveFacadePacket::toBytes)
                .consumerMainThread(RemoveFacadePacket::handle)
                .add();

        net.messageBuilder(AddFacadePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AddFacadePacket::new)
                .encoder(AddFacadePacket::toBytes)
                .consumerMainThread(AddFacadePacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }
}