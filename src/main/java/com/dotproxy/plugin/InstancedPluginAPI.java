package com.dotproxy.plugin;

import com.dotproxy.command.api.Command;
import com.dotproxy.module.api.Module;
import com.dotproxy.plugin.api.ConfigSerializer;
import com.dotproxy.plugin.api.PluginAPI;
import com.dotproxy.plugin.api.PluginInfo;
import com.dotproxy.plugin.api.DotProxyPlugin;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import static com.dotproxy.Globals.*;
import static com.dotproxy.util.KotlinUtil.isKotlinObject;

public record InstancedPluginAPI(
    DotProxyPlugin pluginInstance,
    PluginInfo pluginInfo
) implements PluginAPI {
    @Override
    public <T> T registerConfig(String fileName, Class<T> configClass) {
        return registerConfig(fileName, configClass, isKotlinObject(configClass)
            ? new KotlinObjectGsonConfigSerializer(configClass)
            : DefaultGsonConfigSerializer.INSTANCE
        );
    }

    @Override
    public <T> T registerConfig(String fileName, Class<T> configClass, ConfigSerializer serializer) {
        getLogger().debug("Registering config: {} [{}] using serializer: {}", fileName, configClass.getSimpleName(), serializer.getClass().getSimpleName());
        return PLUGIN_MANAGER.registerConfig(fileName, configClass, serializer);
    }

    @Override
    public void registerModule(final Module module) {
        getLogger().debug("Registering module: {}", module);
        MODULE.registerModule(module);
    }

    @Override
    public void registerCommand(final Command command) {
        getLogger().debug("Registering command: {}", command);
        COMMAND.registerPluginCommand(command);
    }

    @Override
    public ComponentLogger getLogger() {
        return ComponentLogger.logger("Plugin." + pluginInfo.id());
    }

    @Override
    public PluginInfo getPluginInfo() {
        return pluginInfo;
    }
}
