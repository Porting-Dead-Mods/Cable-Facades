package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RemoveFacadedBlocksPacket(ChunkPos chunkPos) {
    public RemoveFacadedBlocksPacket(FriendlyByteBuf buf) {
        this(buf.readChunkPos());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeChunkPos(this.chunkPos);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            for (BlockPos pos : ClientFacadeManager.LOADED_BLOCKS.get(this.chunkPos)) {
                ClientFacadeManager.FACADED_BLOCKS.remove(pos);
            }
            ClientFacadeManager.LOADED_BLOCKS.remove(this.chunkPos);
        });
    }
}
