package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.data.helper.ChunkFacadeMap;
import com.portingdeadmods.cable_facades.networking.CFMessages;
import com.portingdeadmods.cable_facades.networking.s2c.AddFacadedBlocksPacket;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {

    @Shadow @Final ServerLevel level;

    @Inject(
            method = "playerLoadedChunk",
            at = @At("RETURN")
    )
    private void cableFacades$onPlayerLoadedChunk(ServerPlayer player, MutableObject<?> packetHolder, LevelChunk chunk, CallbackInfo ci) {
        CableFacadeSavedData data = CableFacadeSavedData.get(this.level);
        ChunkFacadeMap chunkMap = data.getFacadeMapForChunk(chunk.getPos());
        if (chunkMap != null && !chunkMap.isEmpty()) {
            CFMessages.sendToPlayer(new AddFacadedBlocksPacket(chunk.getPos(), chunkMap.getChunkMap()), player);
        }
    }
}
