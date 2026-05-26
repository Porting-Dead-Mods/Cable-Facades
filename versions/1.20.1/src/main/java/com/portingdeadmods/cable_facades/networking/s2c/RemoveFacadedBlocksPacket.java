package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RemoveFacadedBlocksPacket(ChunkPos chunkPos) {
    public static void encode(RemoveFacadedBlocksPacket pkt, FriendlyByteBuf buf) {
        buf.writeLong(pkt.chunkPos.toLong());
    }

    public static RemoveFacadedBlocksPacket decode(FriendlyByteBuf buf) {
        return new RemoveFacadedBlocksPacket(new ChunkPos(buf.readLong()));
    }

    public static void handle(RemoveFacadedBlocksPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientFacadeManager.untrackChunk(pkt.chunkPos));
        ctx.get().setPacketHandled(true);
    }
}
