package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.events.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.CodecUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record RemoveFacadedBlocksPayload(ChunkPos chunkPos) implements CustomPacketPayload {
    public static final Type<RemoveFacadedBlocksPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "remove_facaded_blocks"));
    public static final StreamCodec<ByteBuf, RemoveFacadedBlocksPayload> STREAM_CODEC = CodecUtils.CHUNK_POS_STREAM_CODEC.map(RemoveFacadedBlocksPayload::new, RemoveFacadedBlocksPayload::chunkPos);

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            List<BlockPos> blockPosList = ClientFacadeManager.LOADED_BLOCKS.get(this.chunkPos);
            if (blockPosList != null) {
                for (BlockPos pos : blockPosList) {
                    ClientFacadeManager.FACADED_BLOCKS.remove(pos);
                }
                ClientFacadeManager.LOADED_BLOCKS.remove(this.chunkPos);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
