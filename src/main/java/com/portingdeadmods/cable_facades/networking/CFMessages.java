package com.portingdeadmods.cable_facades.networking;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.networking.s2c.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class CFMessages {
    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;

    private CFMessages() {}

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel channel = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(CFMain.MODID, "messages"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        INSTANCE = channel;

        channel.messageBuilder(AddFacadePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AddFacadePacket::encode)
                .decoder(AddFacadePacket::decode)
                .consumerMainThread(AddFacadePacket::handle)
                .add();

        channel.messageBuilder(AddDirectionalFacadePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AddDirectionalFacadePacket::encode)
                .decoder(AddDirectionalFacadePacket::decode)
                .consumerMainThread(AddDirectionalFacadePacket::handle)
                .add();

        channel.messageBuilder(RemoveFacadePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RemoveFacadePacket::encode)
                .decoder(RemoveFacadePacket::decode)
                .consumerMainThread(RemoveFacadePacket::handle)
                .add();

        channel.messageBuilder(RemoveDirectionalFacadePacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RemoveDirectionalFacadePacket::encode)
                .decoder(RemoveDirectionalFacadePacket::decode)
                .consumerMainThread(RemoveDirectionalFacadePacket::handle)
                .add();

        channel.messageBuilder(AddFacadedBlocksPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AddFacadedBlocksPacket::encode)
                .decoder(AddFacadedBlocksPacket::decode)
                .consumerMainThread(AddFacadedBlocksPacket::handle)
                .add();

        channel.messageBuilder(RemoveFacadedBlocksPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RemoveFacadedBlocksPacket::encode)
                .decoder(RemoveFacadedBlocksPacket::decode)
                .consumerMainThread(RemoveFacadedBlocksPacket::handle)
                .add();
    }

    public static <MSG> void sendToChunk(MSG message, LevelChunk chunk) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
