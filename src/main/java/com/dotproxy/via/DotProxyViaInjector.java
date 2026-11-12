package com.dotproxy.via;

import com.viaversion.viaversion.platform.NoopInjector;
import com.viaversion.viaversion.platform.ViaCodecHandler;

public class DotProxyViaInjector extends NoopInjector {

    @Override
    public String getEncoderName() {
        return ViaCodecHandler.NAME;
    }

    @Override
    public String getDecoderName() {
        return ViaCodecHandler.NAME;
    }
}
