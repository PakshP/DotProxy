package com.dotproxy.feature.tps;

import com.dotproxy.Proxy;
import com.dotproxy.event.client.ClientConnectEvent;
import com.dotproxy.event.client.ClientTickEvent;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.github.rfresh2.EventConsumer.of;
import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.Globals.EVENT_BUS;

/**
 * Instances of this class will not be garbage collected by default
 *
 * If you want to dispose of it, you must unsubscribe it from the event bus manually
 */
public class TPSCalculator extends BaseTPSCalculator {
    private long prevWorldTimeUpdate = -1;

    public TPSCalculator(int tpsBufferSize) {
        super(tpsBufferSize);
        EVENT_BUS.subscribe(this,
            of(ClientConnectEvent.class, (e) -> reset()),
            of(ClientTickEvent.class, this::onTick),
            of(ClientTickEvent.Starting.class, (e) -> reset()),
            of(ClientTickEvent.Stopped.class, (e) -> reset())
        );
    }

    private void onTick(ClientTickEvent event) {
        if (!Proxy.getInstance().isOnlineForAtLeastDuration(Duration.ofSeconds(1))) return;
        var worldTimeData = CACHE.getChunkCache().getWorldTimeData();
        if (worldTimeData == null) return;
        long lastUpdate = worldTimeData.getLastUpdate();
        if (prevWorldTimeUpdate == -1) {
            prevWorldTimeUpdate = lastUpdate;
            return;
        }
        if (prevWorldTimeUpdate == lastUpdate) return;
        var msElapsed = lastUpdate - prevWorldTimeUpdate;
        prevWorldTimeUpdate = lastUpdate;
        recordTick(TimeUnit.MILLISECONDS.toNanos(msElapsed), 20.0);
    }

    @Override
    public void reset() {
        super.reset();
        prevWorldTimeUpdate = -1;
    }
}
