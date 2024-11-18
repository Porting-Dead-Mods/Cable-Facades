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

        net.messageBuilder(SyncFacadedBlocks.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(SyncFacadedBlocks::new)
                .encoder(SyncFacadedBlocks::toBytes)
                .consumerMainThread(SyncFacadedBlocks::handle)
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

        net.messageBuilder(AddFacadedBlocksPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AddFacadedBlocksPacket::new)
                .encoder(AddFacadedBlocksPacket::toBytes)
                .consumerMainThread(AddFacadedBlocksPacket::handle)
                .add();

        net.messageBuilder(RemoveFacadedBlocksPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(RemoveFacadedBlocksPacket::new)
                .encoder(RemoveFacadedBlocksPacket::toBytes)
                .consumerMainThread(RemoveFacadedBlocksPacket::handle)
                .add();
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToChunk(MSG message, LevelChunk chunk) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), message);
    }
}
