package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.NetworkingUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public record AddFacadedBlocksPacket(ChunkPos chunkPos, Map<BlockPos, BlockState> facadedBlocks) {
    public AddFacadedBlocksPacket(FriendlyByteBuf buf) {
        this(buf.readChunkPos(), NetworkingUtils.getFacades(buf));
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeChunkPos(this.chunkPos);
        buf.writeInt(this.facadedBlocks.size());
        for (Map.Entry<BlockPos, BlockState> entry : this.facadedBlocks.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            NetworkingUtils.writeBlockState(buf, entry.getValue());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (!ClientFacadeManager.LOADED_BLOCKS.containsKey(this.chunkPos)) {
                ClientFacadeManager.FACADED_BLOCKS.putAll(this.facadedBlocks);
                ClientFacadeManager.LOADED_BLOCKS.put(this.chunkPos, this.facadedBlocks.keySet().stream().toList());
            }
        });
    }
}
