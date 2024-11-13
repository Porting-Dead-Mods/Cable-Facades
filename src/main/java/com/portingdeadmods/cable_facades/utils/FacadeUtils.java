package com.portingdeadmods.cable_facades.utils;

import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.events.CFClientEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class FacadeUtils {
    public static boolean hasFacade(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            return CableFacadeSavedData.get(serverLevel).contains(pos);
        }
        return CFClientEvents.CAMOUFLAGED_BLOCKS.containsKey(pos);
    }
}
