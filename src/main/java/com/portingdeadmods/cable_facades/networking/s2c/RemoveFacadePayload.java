package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.events.ClientFacadeManager;
import com.portingdeadmods.cable_facades.utils.ClientFacadeUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RemoveFacadePayload(BlockPos facadePos) implements CustomPacketPayload {
    public static final Type<RemoveFacadePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "remove_facade"));
    public static final StreamCodec<ByteBuf, RemoveFacadePayload> STREAM_CODEC = BlockPos.STREAM_CODEC.map(RemoveFacadePayload::new, RemoveFacadePayload::facadePos);

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientFacadeManager.FACADED_BLOCKS.remove(this.facadePos);
            ClientFacadeUtils.updateBlocks(this.facadePos);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}