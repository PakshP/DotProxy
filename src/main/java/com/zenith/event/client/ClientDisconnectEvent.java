package com.zenith.event.client;

import java.time.Duration;

import static com.zenith.util.DisconnectMessages.MANUAL_DISCONNECT;

public record ClientDisconnectEvent(
    String reason,
    boolean manualDisconnect,
    Duration onlineDuration
) {
    public ClientDisconnectEvent(String reason, final Duration onlineDuration) {
        this(reason, (MANUAL_DISCONNECT.equals(reason)), onlineDuration);
    }

    public ClientDisconnectEvent(String reason) {
        this(reason, Duration.ZERO);
    }
}
