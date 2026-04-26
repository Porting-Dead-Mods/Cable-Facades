package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.CodecUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RemoveFacadedBlocksPayload(ChunkPos chunkPos) implements CustomPacketPayload {
    public static final Type<RemoveFacadedBlocksPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(CFMain.MODID, "remove_facaded_blocks"));
    public static final StreamCodec<ByteBuf, RemoveFacadedBlocksPayload> STREAM_CODEC = CodecUtils.CHUNK_POS_STREAM_CODEC.map(RemoveFacadedBlocksPayload::new, RemoveFacadedBlocksPayload::chunkPos);

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> ClientFacadeManager.untrackChunk(chunkPos));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
