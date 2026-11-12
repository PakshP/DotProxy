package com.dotproxy.event.system;

/**
 * Posted when DotProxy has completed launching
 */
public record ProxyLaunchedEvent() {
    public static final ProxyLaunchedEvent INSTANCE = new ProxyLaunchedEvent();
}
