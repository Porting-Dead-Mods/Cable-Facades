package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.events.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.ClientFacadeUtils;
import com.portingdeadmods.cable_facades.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddFacadePayload(BlockPos facadePos, BlockState blockState) implements CustomPacketPayload {
    public static final Type<AddFacadePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "add_facade"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddFacadePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            AddFacadePayload::facadePos,
            CodecUtils.BLOCKSTATE_STREAM_CODEC,
            AddFacadePayload::blockState,
            AddFacadePayload::new
    );

    public boolean handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientFacadeManager.FACADED_BLOCKS.put(facadePos, blockState);
            ClientFacadeUtils.updateBlocks(this.facadePos);
        });
        return true;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
