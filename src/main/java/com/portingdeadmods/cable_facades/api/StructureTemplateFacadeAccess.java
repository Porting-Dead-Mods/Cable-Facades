package com.portingdeadmods.cable_facades.api;

import com.portingdeadmods.cable_facades.data.FacadeData;
import net.minecraft.core.BlockPos;

import java.util.Map;

public interface StructureTemplateFacadeAccess {
    Map<BlockPos, FacadeData> cableFacades$getFacadeMap();
}
