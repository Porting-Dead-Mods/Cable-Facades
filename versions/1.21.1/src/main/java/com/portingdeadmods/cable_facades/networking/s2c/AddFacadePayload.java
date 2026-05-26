package com.portingdeadmods.cable_facades.networking.s2c;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.client.FacadeClientUtils;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.ClientFacadeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddFacadePayload(BlockPos facadePos, FacadeData facadeData) implements CustomPacketPayload {
    public static final Type<AddFacadePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "add_facade"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddFacadePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            AddFacadePayload::facadePos,
            FacadeData.STREAM_CODEC,
            AddFacadePayload::facadeData,
            AddFacadePayload::new
    );

    public boolean handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientFacadeManager.put(facadePos, facadeData);
            FacadeClientUtils.updateClientBlock(facadePos);
        });
        return true;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
