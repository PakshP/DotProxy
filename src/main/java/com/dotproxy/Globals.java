package com.dotproxy;

import com.github.rfresh2.SimpleEventBus;
import com.google.common.io.Files;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.dotproxy.cache.DataCache;
import com.dotproxy.command.CommandManager;
import com.dotproxy.discord.DiscordBot;
import com.dotproxy.feature.gui.InGameGuiManager;
import com.dotproxy.feature.inventory.InventoryManager;
import com.dotproxy.feature.player.Bot;
import com.dotproxy.feature.player.InputManager;
import com.dotproxy.feature.tps.ClientTPSCalculator;
import com.dotproxy.feature.tps.TPSCalculator;
import com.dotproxy.feature.whitelist.PlayerListsManager;
import com.dotproxy.mc.block.BlockDataManager;
import com.dotproxy.mc.entity.EntityDataManager;
import com.dotproxy.mc.language.TranslationRegistryInitializer;
import com.dotproxy.module.ModuleManager;
import com.dotproxy.network.server.handler.player.InGameCommandManager;
import com.dotproxy.plugin.DefaultGsonConfigSerializer;
import com.dotproxy.plugin.PluginManager;
import com.dotproxy.plugin.api.ConfigSerializer;
import com.dotproxy.terminal.TerminalManager;
import com.dotproxy.util.KotlinUtil;
import com.dotproxy.util.Wait;
import com.dotproxy.util.DotProxyScheduledExecutor;
import com.dotproxy.util.config.Config;
import com.dotproxy.via.DotProxyViaInitializer;
import lombok.Locked;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Globals {
    public static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final ComponentLogger DEFAULT_LOG = ComponentLogger.logger("Proxy");
    public static final ComponentLogger AUTH_LOG = ComponentLogger.logger("Auth");
    public static final ComponentLogger CACHE_LOG = ComponentLogger.logger("Cache");
    public static final ComponentLogger CLIENT_LOG = ComponentLogger.logger("Client");
    public static final ComponentLogger CHAT_LOG = ComponentLogger.logger("Chat");
    public static final ComponentLogger MODULE_LOG = ComponentLogger.logger("Module");
    public static final ComponentLogger SERVER_LOG = ComponentLogger.logger("Server");
    public static final ComponentLogger DISCORD_LOG = ComponentLogger.logger("Discord");
    public static final ComponentLogger TERMINAL_LOG = ComponentLogger.logger("Terminal");
    public static final ComponentLogger PLUGIN_LOG = ComponentLogger.logger("Plugin");
    public static final File CONFIG_FILE = new File(System.getProperty("dotproxy.config.file", "config.json"));
    public static final Config CONFIG;
    public static final DataCache CACHE;
    public static final DiscordBot DISCORD;
    public static final SimpleEventBus EVENT_BUS;
    public static final ScheduledExecutorService EXECUTOR;
    public static final PlayerListsManager PLAYER_LISTS;
    public static final BlockDataManager BLOCK_DATA;
    public static final EntityDataManager ENTITY_DATA;
    public static final TPSCalculator TPS;
    public static final ClientTPSCalculator CLIENT_TPS;
    public static final ModuleManager MODULE;
    public static final InputManager INPUTS;
    public static final TerminalManager TERMINAL;
    public static final InGameCommandManager IN_GAME_COMMAND;
    public static final CommandManager COMMAND;
    public static final Bot BOT;
    public static final InventoryManager INVENTORY;
    public static final DotProxyViaInitializer VIA_INITIALIZER;
    public static final PluginManager PLUGIN_MANAGER;
    public static final InGameGuiManager GUI;
    public static final String MC_VERSION;
    public static final String VERSION;
    public static final String SOURCE_URL = "https://github.com/PakshP/DotProxy";

    public static boolean inDevEnv() {
        return System.getenv("DOTPROXY_DEV") != null;
    }

    public static String getVersion() {
        var releaseVersion = getExecutableReleaseVersion();
        if (releaseVersion != null) {
            if (releaseVersion.endsWith("pre")) {
                var commit = getExecutableCommit();
                if (commit != null) {
                    return releaseVersion + "-" + commit;
                }
            }
            return releaseVersion;
        }
        return "0.0.0-dev";
    }

    public static @Nullable String getExecutableCommit() {
        return readResourceTxt("dotproxy_commit.txt");
    }

    public static @Nullable String getExecutableReleaseVersion() {
        return readResourceTxt("dotproxy_release.txt");
    }

    public static @Nullable String getMCVersionFile() {
        return readResourceTxt("dotproxy_mc_version.txt");
    }

    private static @Nullable String readResourceTxt(final String name) {
        try (InputStream in = Globals.class.getClassLoader().getResourceAsStream(name)) {
            if (in == null) return null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                return reader.readLine();
            }
        } catch (IOException e) {
            return null;
        }
    }

    @Locked
    public static Config loadConfig() {
        return loadConfig(CONFIG_FILE, Config.class, DefaultGsonConfigSerializer.INSTANCE);
    }

    @Locked
    public static <T> T loadConfig(File file, Class<T> configClass, ConfigSerializer serializer) {
        try {
            T config;
            if (file.exists()) {
                try (Reader reader = new FileReader(file)) {
                    config = serializer.read(configClass, reader);
                } catch (IOException e) {
                    throw new RuntimeException("Unable to load config: " + file.getName(), e);
                }
            } else {
                config = KotlinUtil.isKotlinObject(configClass)
                    ? KotlinUtil.getKotlinObject(configClass)
                    : configClass.getDeclaredConstructor().newInstance();
            }
            DEFAULT_LOG.info("{} loaded.", file.getName());
            return config;
        } catch (final Throwable e) {
            e.printStackTrace();
            DEFAULT_LOG.error("Unable to load config: {}", file.getName(), e);
            DEFAULT_LOG.error("{} must be manually fixed or deleted", file.getName());
            DEFAULT_LOG.error("Shutting down in 10s");
            Wait.wait(10);
            System.exit(1);
            return null;
        }
    }

    public static void saveConfigAsync() {
        Thread.ofVirtual().name("Async Config Save").start(Globals::saveConfig);
    }

    @Locked
    public static void saveConfig() {
        saveConfig(CONFIG_FILE, CONFIG, DefaultGsonConfigSerializer.INSTANCE);
        PLUGIN_MANAGER.saveConfigs(Globals::saveConfig);
    }

    @Locked
    static void saveConfig(File file, Object config, ConfigSerializer serializer) {
        DEFAULT_LOG.debug("Saving {}...", file.getName());

        if (config == null) {
            DEFAULT_LOG.error("Cannot save unloaded config");
            return;
        }

        try {
            final File tempFile = File.createTempFile(file.getName(), null);
            try (Writer out = new FileWriter(tempFile)) {
                serializer.write(config, out);
            }
            Files.move(tempFile, file);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save config!", e);
        }

        DEFAULT_LOG.debug("Config {} saved.", file.getName());
    }

    static {
        try {
            Thread.setDefaultUncaughtExceptionHandler(
                (thread, e) -> DEFAULT_LOG.error("Uncaught exception in thread {}", thread, e));
            EXECUTOR = new DotProxyScheduledExecutor(4, new ThreadFactoryBuilder()
                .setNameFormat("DotProxy Scheduled Executor - #%d")
                .setDaemon(true)
                .setUncaughtExceptionHandler((thread, e) -> DEFAULT_LOG.error("Uncaught exception in scheduled executor thread {}", thread, e))
                .build());
            EVENT_BUS = new SimpleEventBus(Executors.newFixedThreadPool(2, new ThreadFactoryBuilder()
                .setNameFormat("DotProxy Async EventBus - #%d")
                .setDaemon(true)
                .setUncaughtExceptionHandler((thread, e) -> DEFAULT_LOG.error("Uncaught exception in event bus thread {}", thread, e))
                .build()), DEFAULT_LOG);
            MC_VERSION = getMCVersionFile();
            DISCORD = new DiscordBot();
            CACHE = new DataCache();
            PLAYER_LISTS = new PlayerListsManager();
            BLOCK_DATA = new BlockDataManager();
            ENTITY_DATA = new EntityDataManager();
            MODULE = new ModuleManager();
            INPUTS = new InputManager();
            TERMINAL = new TerminalManager();
            IN_GAME_COMMAND = new InGameCommandManager();
            COMMAND = new CommandManager();
            INVENTORY = new InventoryManager();
            VIA_INITIALIZER = new DotProxyViaInitializer();
            TranslationRegistryInitializer.registerAllTranslations();
            CONFIG = loadConfig();
            VERSION = getVersion();
            PLUGIN_MANAGER = new PluginManager();
            PLAYER_LISTS.init(); // must be init after config
            TPS = new TPSCalculator(CONFIG.client.extra.tpsBufferSize);
            CLIENT_TPS = new ClientTPSCalculator(CONFIG.client.extra.tpsBufferSize * 20);
            BOT = new Bot();
            GUI = new InGameGuiManager();
        } catch (final Throwable e) {
            System.err.println("Unable to initialize!");
            e.printStackTrace();
            DEFAULT_LOG.error("Unable to initialize!", e);
            throw e;
        }
    }

}
