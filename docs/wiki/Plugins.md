# Plugins

DotProxy plugins are Java add-ons that add custom modules and commands.

!!! note

    Plugins are DotProxy-specific. Minecraft mods (Fabric/Forge) and server plugins (Paper/Spigot) don't work with DotProxy.

## Installing Plugins

### Option 1: DotProxy Commands

`plugins download <url>`

The URL must be a direct download link to a plugin `.jar` file.

To remove a plugin:

`plugins remove <pluginId>`

To list loaded plugins:

`plugins list`

### Option 2: Manual

DotProxy creates a `plugins` folder in the folder you run it from.

Put plugin jars in the `plugins` folder.

Restart DotProxy to load them.

Loading plugins while DotProxy is running, or hot reloading them, is not supported.

!!! warning

    Plugins run with full access to your computer and your DotProxy instance, including your Minecraft account login. Only install plugins you trust.

## Creating Plugins

A plugin is a jar file containing:

* A class that implements `com.dotproxy.plugin.api.DotProxyPlugin` and is annotated with `@com.dotproxy.plugin.api.Plugin`
* A `dotproxy.plugin.json` descriptor at the jar's root. DotProxy's annotation processor generates this from the `@Plugin` annotation.

To set up your project:

1. Use Java 21+ and add `DotProxy.jar` as a `compileOnly` dependency, so the plugin compiles against DotProxy's classes but doesn't bundle them.
2. Set up DotProxy's annotation processor, `com.dotproxy.plugin.PluginAnnotationProcessor` (in the same jar), so `dotproxy.plugin.json` is generated at build time. In Gradle, add the jar to `annotationProcessor` too. If the processor isn't picked up automatically, pass it explicitly with the `-processor` compiler option.
3. Build your plugin jar and put it in the `plugins` folder.

### Example

```java
package com.example.myplugin;

import com.dotproxy.plugin.api.DotProxyPlugin;
import com.dotproxy.plugin.api.Plugin;
import com.dotproxy.plugin.api.PluginAPI;

@Plugin(
    id = "my-plugin",
    version = "1.0.0",
    description = "An example DotProxy plugin",
    authors = {"you"}
)
public class MyPlugin implements DotProxyPlugin {
    @Override
    public void onLoad(PluginAPI pluginAPI) {
        // Register configs, modules, and commands here
        // MyConfig config = pluginAPI.registerConfig("my-plugin", MyConfig.class);
        // pluginAPI.registerModule(new MyModule());
        // pluginAPI.registerCommand(new MyCommand());
    }
}
```

`onLoad` is called as soon as the plugin class is loaded. The `PluginAPI` it receives lets you:

* `registerConfig(fileName, configClass)`: load and save a JSON config for your plugin
* `registerModule(module)`: add a module (extend DotProxy's `Module` class)
* `registerCommand(command)`: add a command (extend DotProxy's `Command` class). It's available from the terminal, in-game, and Discord.

### Descriptor Fields

The `@Plugin` annotation, and the `dotproxy.plugin.json` generated from it, have these fields:

| Field         | Description                                                                 |
|---------------|-----------------------------------------------------------------------------|
| `id`          | Unique plugin ID: starts with a lowercase letter, then lowercase letters, numbers, `-` or `_` (max 64 characters) |
| `version`     | Plugin version, for example `1.0.0`                                         |
| `description` | Short description                                                           |
| `url`         | Project URL                                                                 |
| `authors`     | List of authors                                                             |
| `mcVersions`  | Supported Minecraft versions (`*` for any)                                  |
| `mixins`      | Optional mixin config files                                                 |

The built-in modules and commands in the [DotProxy source code](https://github.com/PakshP/DotProxy) are good examples of how to write your own.
