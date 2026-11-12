package com.dotproxy.event.plugin;

import com.dotproxy.plugin.api.PluginInfo;

public record PluginLoadedEvent(PluginInfo pluginInfo) { }
