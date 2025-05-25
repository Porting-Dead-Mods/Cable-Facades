package com.portingdeadmods.cable_facades.compat.iris;

import net.irisshaders.iris.api.v0.IrisApi;

public class IrisUtil {
    public static boolean areShadersEnabled(){
        return IrisApi.getInstance().isShaderPackInUse();
    }
}
