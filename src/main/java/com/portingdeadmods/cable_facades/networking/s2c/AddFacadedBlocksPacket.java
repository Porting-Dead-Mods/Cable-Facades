package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.client.FacadeClientUtils;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public record AddFacadedBlocksPacket(ChunkPos chunkPos, Map<BlockPos, FacadeData> facadedBlocks) {
    public static void encode(AddFacadedBlocksPacket pkt, FriendlyByteBuf buf) {
        buf.writeLong(pkt.chunkPos.toLong());
        buf.writeVarInt(pkt.facadedBlocks.size());
        for (Map.Entry<BlockPos, FacadeData> e : pkt.facadedBlocks.entrySet()) {
            buf.writeBlockPos(e.getKey());
            FacadeData.encode(buf, e.getValue());
        }
    }

    public static AddFacadedBlocksPacket decode(FriendlyByteBuf buf) {
        ChunkPos cp = new ChunkPos(buf.readLong());
        int n = buf.readVarInt();
        Map<BlockPos, FacadeData> map = new HashMap<>(n);
        for (int i = 0; i < n; i++) {
            BlockPos pos = buf.readBlockPos();
            FacadeData data = FacadeData.decode(buf);
            map.put(pos, data);
        }
        return new AddFacadedBlocksPacket(cp, map);
    }

    public static void handle(AddFacadedBlocksPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (!ClientFacadeManager.containsChunk(pkt.chunkPos)) {
                ClientFacadeManager.putAll(pkt.facadedBlocks);
                ClientFacadeManager.trackChunk(pkt.chunkPos, pkt.facadedBlocks.keySet().stream().toList());
                for (BlockPos pos : pkt.facadedBlocks.keySet()) {
                    FacadeClientUtils.updateClientBlock(pos);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
