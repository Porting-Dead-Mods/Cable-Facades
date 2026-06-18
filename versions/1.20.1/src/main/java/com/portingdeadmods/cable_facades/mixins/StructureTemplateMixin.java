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
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

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

    @Inject(method = "fillFromWorld", at = @At("RETURN"))
    private void onFillFromWorld(Level level, BlockPos pos, Vec3i size, boolean includeEntities, Block toIgnore, CallbackInfo ci) {
        facadeMap.clear();
        BlockPos.betweenClosed(pos, pos.offset(size).offset(-1, -1, -1)).forEach(blockPos -> {
            FacadeData data = FacadeUtils.getFacadeData(level, blockPos);
            if (data != null) {
                BlockPos relativePos = blockPos.subtract(pos);
                facadeMap.put(relativePos.immutable(), data);
            }
        });
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void onSave(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (!facadeMap.isEmpty()) {
            ListTag facadesTag = new ListTag();
            facadeMap.forEach((pos, data) -> {
                CompoundTag facadeTag = new CompoundTag();
                facadeTag.put("pos", NbtUtils.writeBlockPos(pos));
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

    @Inject(method = "load", at = @At("RETURN"))
    private void onLoad(HolderGetter<Block> blockGetter, CompoundTag tag, CallbackInfo ci) {
        facadeMap.clear();
        if (tag.contains(FACADES_TAG, Tag.TAG_LIST)) {
            ListTag facadesTag = tag.getList(FACADES_TAG, Tag.TAG_COMPOUND);
            for (int i = 0; i < facadesTag.size(); i++) {
                CompoundTag facadeTag = facadesTag.getCompound(i);
                BlockPos pos = NbtUtils.readBlockPos(facadeTag.getCompound("pos"));
                String facadeType = facadeTag.getString("facade_type");
                ResourceLocation typeId = facadeTag.contains("facade_type_id")
                        ? new ResourceLocation(facadeTag.getString("facade_type_id"))
                        : FacadeTypes.DEFAULT_ID;

                if ("directional".equals(facadeType)) {
                    CompoundTag facesTag = facadeTag.getCompound("faces");
                    EnumMap<Direction, BlockState> faces = new EnumMap<>(Direction.class);
                    for (Direction dir : Direction.values()) {
                        String key = dir.getSerializedName();
                        if (facesTag.contains(key)) {
                            BlockState state = NbtUtils.readBlockState(blockGetter, facesTag.getCompound(key));
                            faces.put(dir, state);
                        }
                    }
                    if (!faces.isEmpty()) {
                        facadeMap.put(pos, FacadeData.directional(typeId, faces));
                    }
                } else {
                    BlockState state = NbtUtils.readBlockState(blockGetter, facadeTag.getCompound("state"));
                    facadeMap.put(pos, FacadeData.fullBlock(typeId, state));
                }
            }
        }
    }

    @Inject(method = "placeInWorld", at = @At("RETURN"))
    private void onPlaceInWorld(ServerLevelAccessor level, BlockPos offset, BlockPos pivot, StructurePlaceSettings settings, RandomSource random, int flags, CallbackInfoReturnable<Boolean> cir) {
        if (!facadeMap.isEmpty() && Boolean.TRUE.equals(cir.getReturnValue())) {
            ServerLevel serverLevel = level.getLevel();
            boolean worldGen = !(level instanceof ServerLevel);
            facadeMap.forEach((relativePos, facadeData) -> {
                BlockPos transformedPos = StructureTemplate.calculateRelativePosition(settings, relativePos);
                BlockPos actualPos = transformedPos.offset(offset);

                if (facadeData.isFullBlock()) {
                    BlockState transformedState = facadeData.getFullBlock()
                            .mirror(settings.getMirror())
                            .rotate(settings.getRotation());
                    if (worldGen) {
                        FacadeUtils.addFacadeWorldGen(serverLevel, actualPos, transformedState, facadeData.facadeType());
                    } else {
                        FacadeUtils.addFacade(serverLevel, actualPos, transformedState, facadeData.facadeType());
                    }
                } else if (facadeData.isDirectional()) {
                    facadeData.directional().forEach((dir, state) -> {
                        BlockState transformedState = state
                                .mirror(settings.getMirror())
                                .rotate(settings.getRotation());
                        if (worldGen) {
                            FacadeUtils.addDirectionalFacadeWorldGen(serverLevel, actualPos, dir, transformedState, facadeData.facadeType());
                        } else {
                            FacadeUtils.addDirectionalFacade(serverLevel, actualPos, dir, transformedState, facadeData.facadeType());
                        }
                    });
                }
            });
        }
    }
}
