package com.portingdeadmods.cable_facades.compat.iris;

import com.bawnorton.mixinsquared.api.MixinCanceller;

import java.util.List;

public final class IrisMixinCanceller implements MixinCanceller {
    private static final String IRIS_GAME_CLIENT_EVENTS_MIXIN = "net.irisshaders.iris.mixin.forge.MixinGameClientEvents";

    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        return IRIS_GAME_CLIENT_EVENTS_MIXIN.equals(mixinClassName);
    }
}
