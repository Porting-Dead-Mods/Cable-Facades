package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.events.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record AddFacadedBlocksPayload(ChunkPos chunkPos,
                                      Map<BlockPos, Block> facadedBlocks) implements CustomPacketPayload {
    public static final Type<AddFacadedBlocksPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "add_facaded_blocks"));
    private static final StreamCodec<RegistryFriendlyByteBuf, Map<BlockPos, Block>> FACADED_BLOCKS_STREAM_CODEC = ByteBufCodecs.map(HashMap::new, BlockPos.STREAM_CODEC, CodecUtils.BLOCK_STREAM_CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, AddFacadedBlocksPayload> STREAM_CODEC = StreamCodec.composite(
            CodecUtils.CHUNK_POS_STREAM_CODEC,
            AddFacadedBlocksPayload::chunkPos,
            FACADED_BLOCKS_STREAM_CODEC,
            AddFacadedBlocksPayload::facadedBlocks,
            AddFacadedBlocksPayload::new
    );

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!ClientFacadeManager.LOADED_BLOCKS.containsKey(this.chunkPos)) {
                ClientFacadeManager.FACADED_BLOCKS.putAll(this.facadedBlocks);
                ClientFacadeManager.LOADED_BLOCKS.put(this.chunkPos, this.facadedBlocks.keySet().stream().toList());
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
