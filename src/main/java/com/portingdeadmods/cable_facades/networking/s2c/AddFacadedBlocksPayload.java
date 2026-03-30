package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.CodecUtils;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record AddFacadedBlocksPayload(ChunkPos chunkPos,
                                      Map<BlockPos, FacadeData> facadedBlocks) implements CustomPacketPayload {
    public static final Type<AddFacadedBlocksPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "add_facaded_blocks"));
    private static final StreamCodec<RegistryFriendlyByteBuf, Map<BlockPos, FacadeData>> FACADED_BLOCKS_STREAM_CODEC = ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, FacadeData.STREAM_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, AddFacadedBlocksPayload> STREAM_CODEC = StreamCodec.composite(
            CodecUtils.CHUNK_POS_STREAM_CODEC,
            AddFacadedBlocksPayload::chunkPos,
            FACADED_BLOCKS_STREAM_CODEC,
            AddFacadedBlocksPayload::facadedBlocks,
            AddFacadedBlocksPayload::new
    );

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!ClientFacadeManager.containsChunk(chunkPos)) {
                ClientFacadeManager.putAll(facadedBlocks);
                ClientFacadeManager.trackChunk(chunkPos, facadedBlocks.keySet().stream().toList());
                for (BlockPos pos : facadedBlocks.keySet()) {
                    FacadeUtils.updateClientBlock(pos);
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
