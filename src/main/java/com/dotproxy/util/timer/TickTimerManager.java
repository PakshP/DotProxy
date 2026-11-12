package com.dotproxy.util.timer;

import com.dotproxy.event.client.ClientTickEvent;
import lombok.Getter;

import static com.github.rfresh2.EventConsumer.of;
import static com.dotproxy.Globals.EVENT_BUS;

public final class TickTimerManager {
    // before any modules
    public static final int TICK_PRIORITY = Integer.MAX_VALUE - 1;
    public static final TickTimerManager INSTANCE = new TickTimerManager();

    @Getter
    private volatile long tickTime = 0;

    private TickTimerManager() {
        EVENT_BUS.subscribe(
            this,
            of(ClientTickEvent.class, TICK_PRIORITY, this::onClientTick)
        );
    }

    private void onClientTick(ClientTickEvent event) {
        tickTime++;
    }
}
