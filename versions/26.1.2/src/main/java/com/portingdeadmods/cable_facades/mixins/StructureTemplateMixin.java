package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.api.StructureTemplateFacadeAccess;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin implements StructureTemplateFacadeAccess {

    @Unique
    private static final String FACADES_TAG = "cable_facades";

    @Unique
    private final Map<BlockPos, FacadeData> facadeMap = new HashMap<>();

    @Override
    public Map<BlockPos, FacadeData> cableFacades$getFacadeMap() {
        return facadeMap;
    }

    @Inject(
            method = "fillFromWorld",
            at = @At("RETURN")
    )
    private void onFillFromWorld(Level level, BlockPos pos, Vec3i size, boolean inludeEntities, List<Block> ignoreBlocks, CallbackInfo ci) {
        facadeMap.clear();

        BlockPos.betweenClosed(pos, pos.offset(size).offset(-1, -1, -1)).forEach(blockPos -> {
            FacadeData data = FacadeUtils.getFacadeData(level, blockPos);
            if (data != null) {
                BlockPos relativePos = blockPos.subtract(pos);
                facadeMap.put(relativePos, data);
            }
        });
    }

    @Inject(
            method = "save",
            at = @At("RETURN")
    )
    private void onSave(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (!facadeMap.isEmpty()) {
            ListTag facadesTag = new ListTag();

            facadeMap.forEach((pos, data) -> {
                CompoundTag facadeTag = new CompoundTag();
                facadeTag.putLong("pos", pos.asLong());
                facadeTag.putString("facade_type_id", data.facadeType().toString());

                if (data.isFullBlock()) {
                    facadeTag.putString("facade_type", "full");
                    facadeTag.put("state", NbtUtils.writeBlockState(data.getFullBlock()));
                } else if (data.isDirectional()) {
                    facadeTag.putString("facade_type", "directional");
                    CompoundTag facesTag = new CompoundTag();
                    data.directional().forEach((dir, state) ->
                            facesTag.put(dir.getSerializedName(), NbtUtils.writeBlockState(state)));
                    facadeTag.put("faces", facesTag);
                }

                facadesTag.add(facadeTag);
            });

            tag.put(FACADES_TAG, facadesTag);
        }
    }

    @Inject(
            method = "load",
            at = @At("RETURN")
    )
    private void onLoad(HolderGetter<Block> blockGetter, CompoundTag tag, CallbackInfo ci) {
        facadeMap.clear();

        Optional<ListTag> facadesTagOpt = tag.getList(FACADES_TAG);
        if (facadesTagOpt.isEmpty()) return;
        ListTag facadesTag = facadesTagOpt.get();

        for (int i = 0; i < facadesTag.size(); i++) {
            Optional<CompoundTag> facadeTagOpt = facadesTag.getCompound(i);
            if (facadeTagOpt.isEmpty()) continue;
            CompoundTag facadeTag = facadeTagOpt.get();

            BlockPos pos = BlockPos.of(facadeTag.getLongOr("pos", 0L));
            String facadeType = facadeTag.getString("facade_type").orElse("");
            Identifier typeId = facadeTag.getString("facade_type_id")
                    .map(Identifier::parse)
                    .orElse(FacadeTypes.DEFAULT_ID);

            if ("directional".equals(facadeType)) {
                Optional<CompoundTag> facesTagOpt = facadeTag.getCompound("faces");
                if (facesTagOpt.isEmpty()) continue;
                CompoundTag facesTag = facesTagOpt.get();
                EnumMap<Direction, BlockState> faces = new EnumMap<>(Direction.class);
                for (Direction dir : Direction.values()) {
                    String key = dir.getSerializedName();
                    facesTag.getCompound(key).ifPresent(faceTag ->
                            faces.put(dir, NbtUtils.readBlockState(blockGetter, faceTag)));
                }
                if (!faces.isEmpty()) {
                    facadeMap.put(pos, FacadeData.directional(typeId, faces));
                }
            } else {
                facadeTag.getCompound("state").ifPresent(stateTag -> {
                    BlockState state = NbtUtils.readBlockState(blockGetter, stateTag);
                    facadeMap.put(pos, FacadeData.fullBlock(typeId, state));
                });
            }
        }
    }

    @Inject(
            method = "placeInWorld",
            at = @At("RETURN")
    )
    private void onPlaceInWorld(ServerLevelAccessor level, BlockPos offset, BlockPos pos, StructurePlaceSettings settings, RandomSource random, int flags, CallbackInfoReturnable<Boolean> cir) {
        if (!facadeMap.isEmpty() && cir.getReturnValue()) {
            facadeMap.forEach((relativePos, facadeData) -> {
                BlockPos transformedPos = StructureTemplate.calculateRelativePosition(settings, relativePos);
                BlockPos actualPos = transformedPos.offset(offset);

                if (facadeData.isFullBlock()) {
                    BlockState transformedState = facadeData.getFullBlock()
                            .mirror(settings.getMirror())
                            .rotate(settings.getRotation());
                    FacadeUtils.addFacade(level.getLevel(), actualPos, transformedState, facadeData.facadeType());
                } else if (facadeData.isDirectional()) {
                    facadeData.directional().forEach((dir, state) -> {
                        BlockState transformedState = state
                                .mirror(settings.getMirror())
                                .rotate(settings.getRotation());
                        FacadeUtils.addDirectionalFacade(level.getLevel(), actualPos, dir, transformedState, facadeData.facadeType());
                    });
                }
            });
        }
    }
}
