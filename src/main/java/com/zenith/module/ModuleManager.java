package com.zenith.module;

import com.zenith.module.api.Module;
import com.zenith.module.impl.*;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static com.zenith.Globals.MODULE_LOG;
import static java.util.Arrays.asList;

public class ModuleManager {
    private final Map<Class<? extends Module>, Module> moduleClassMap = new IdentityHashMap<>();

    public void init() {
        asList(
            new ActionLimiter(),
            new ActiveHours(),
            new AntiAFK(),
            new AntiKick(),
            new AutoDisconnect(),
            new AutoReconnect(),
            new AutoRespawn(),
            new ChatHistory(),
            new ExtraChat(),
            new VisualRange()
        ).forEach(m -> {
            addModule(m);
            m.syncEnabledFromConfig();
        });
    }

    private void addModule(Module module) {
        moduleClassMap.put(module.getClass(), module);
    }

    public <T extends Module> T get(final Class<T> clazz) {
        try {
            return (T) moduleClassMap.get(clazz);
        } catch (final Throwable e) {
            return null;
        }
    }

    public void registerModule(Module module) {
        if (moduleClassMap.containsKey(module.getClass())) {
            MODULE_LOG.warn("Duplicate module class being registered: {}", module.getClass().getSimpleName(), new RuntimeException());
        }
        addModule(module);
        module.syncEnabledFromConfig();
    }

    public List<Module> getModules() {
        return moduleClassMap.values().stream().toList();
    }
}
