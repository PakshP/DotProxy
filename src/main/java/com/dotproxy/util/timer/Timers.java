package com.dotproxy.util.timer;

public final class Timers {
    private Timers() {}

    // Synchronized to the dotproxy client tick event
    // Therefore, if the dotproxy client is not connected, no ticks will occur
    public static SyncedTickTimer tickTimer() {
        return new SyncedTickTimer();
    }

    public static StandardTimer unsyncedTickTimer() {
        return new StandardTimer(50L);
    }

    public static StandardTimer timer() {
        return new StandardTimer();
    }

    public static StandardTimer timer(long delay) {
        return new StandardTimer(delay);
    }
}
