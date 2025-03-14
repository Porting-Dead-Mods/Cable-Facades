package com.portingdeadmods.cable_facades.mixins;

import com.portingdeadmods.cable_facades.utils.FacadeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Mixin(StructureTemplate.class)
public class StructureTemplateMixin {

    @Unique
    private static final String FACADES_TAG = "cable_facades";

    @Unique
    private final Map<BlockPos, BlockState> facadeMap = new HashMap<>();

    @Inject(
            method = "fillFromWorld",
            at = @At("RETURN")
    )
    private void onFillFromWorld(net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.core.Vec3i size, boolean includeEntities, net.minecraft.world.level.block.Block toIgnore, CallbackInfo ci) {
        facadeMap.clear();

        BlockPos.betweenClosed(pos, pos.offset(size).offset(-1, -1, -1)).forEach(blockPos -> {
            BlockState facade = FacadeUtils.getFacade(level, blockPos);
            if (facade != null) {
                BlockPos relativePos = blockPos.subtract(pos);
                facadeMap.put(relativePos, facade);
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

            facadeMap.forEach((pos, state) -> {
                CompoundTag facadeTag = new CompoundTag();
                facadeTag.put("pos", NbtUtils.writeBlockPos(pos));
                facadeTag.put("state", NbtUtils.writeBlockState(state));
                facadesTag.add(facadeTag);
            });

            tag.put(FACADES_TAG, facadesTag);
        }
    }

    @Inject(
            method = "load",
            at = @At("RETURN")
    )
    private void onLoad(net.minecraft.core.HolderGetter<net.minecraft.world.level.block.Block> blockGetter, CompoundTag tag, CallbackInfo ci) {
        facadeMap.clear();

        if (tag.contains(FACADES_TAG, Tag.TAG_LIST)) {
            ListTag facadesTag = tag.getList(FACADES_TAG, Tag.TAG_COMPOUND);

            for (int i = 0; i < facadesTag.size(); i++) {
                CompoundTag facadeTag = facadesTag.getCompound(i);
                Optional<BlockPos> posOpt = NbtUtils.readBlockPos(facadeTag, "pos");
                if (posOpt.isPresent()) {
                    BlockPos pos = posOpt.get();
                    BlockState state = NbtUtils.readBlockState(blockGetter, facadeTag.getCompound("state"));
                    facadeMap.put(pos, state);
                }
            }
        }
    }

    @Inject(
            method = "placeInWorld",
            at = @At("RETURN")
    )
    private void onPlaceInWorld(ServerLevelAccessor level, BlockPos offset, BlockPos pos, StructurePlaceSettings settings, RandomSource random, int flags, CallbackInfoReturnable<Boolean> cir) {
        if (!facadeMap.isEmpty() && cir.getReturnValue()) {
            facadeMap.forEach((relativePos, facadeState) -> {
                BlockPos transformedPos = StructureTemplate.calculateRelativePosition(settings, relativePos);
                BlockPos actualPos = transformedPos.offset(offset);

                BlockState transformedState = facadeState
                        .mirror(settings.getMirror())
                        .rotate(settings.getRotation());

                FacadeUtils.addFacade(level.getLevel(), actualPos, transformedState);
            });
        }
    }
}