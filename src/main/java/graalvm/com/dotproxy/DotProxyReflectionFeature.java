package graalvm.com.dotproxy;

import java.util.List;

public class DotProxyReflectionFeature extends AbstractGraalVMReflectionFeature {
    public DotProxyReflectionFeature() {
        super(List.of(
            "com.viaversion.nbt",
            "com.viaversion.viabackwards.api",
            "com.viaversion.viabackwards.protocol",
            "com.viaversion.viarewind.api",
            "com.viaversion.viarewind.protocol",
            "com.viaversion.viaversion.api",
            "com.viaversion.viaversion.protocols",
            "com.dotproxy.event",
            "com.dotproxy.feature.api",
            "com.dotproxy.feature.ping",
            "com.dotproxy.terminal",
            "com.dotproxy.util.config",
            "com.dotproxy.via"
        ));
    }
}
