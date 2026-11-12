package com.dotproxy.via;

import com.dotproxy.feature.ping.MCPing;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProtocolVersionDetector {
    public int getProtocolVersion(final String hostname, final int port) {
        try {
            return MCPing.INSTANCE.getProtocolVersion(hostname, port, 3000, true);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
