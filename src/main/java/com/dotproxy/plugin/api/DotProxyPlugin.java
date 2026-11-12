package com.dotproxy.plugin.api;

public interface DotProxyPlugin {
    /**
     * Called immediately when the plugin class is loaded.
     *
     * Initialize configurations, modules, and commands here.
     */
    void onLoad(PluginAPI pluginAPI);
}
