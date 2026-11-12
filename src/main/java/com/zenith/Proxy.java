package com.zenith;

import ar.com.hjg.pngj.PngReader;
import ch.qos.logback.classic.LoggerContext;
import com.zenith.cache.CacheResetType;
import com.zenith.discord.ChatRelayEventListener;
import com.zenith.discord.Embed;
import com.zenith.discord.NotificationEventListener;
import com.zenith.event.client.*;
import com.zenith.event.message.PrivateMessageSendEvent;
import com.zenith.event.server.ServerIconBuildEvent;
import com.zenith.event.system.ProxyLaunchedEvent;
import com.zenith.feature.api.mcstatus.MCStatusApi;
import com.zenith.feature.chatschema.ChatSchemaParser;
import com.zenith.feature.skin.SkinRetriever;
import com.zenith.module.impl.AutoReconnect;
import com.zenith.network.client.Authenticator;
import com.zenith.network.client.ClientSession;
import com.zenith.network.server.LanBroadcaster;
import com.zenith.network.server.ProxyServerListener;
import com.zenith.network.server.ServerSession;
import com.zenith.plugin.bootstrap.BootstrapMessageConsumer;
import com.zenith.terminal.logback.TerminalConsoleAppender;
import com.zenith.util.TimeUtil;
import com.zenith.util.Wait;
import com.zenith.util.config.ConfigVerifier;
import com.zenith.util.struct.FastArrayList;
import com.zenith.via.ZenithClientChannelInitializer;
import com.zenith.via.ZenithServerChannelInitializer;
import dev.omega24.upnp4j.UPnP4J;
import dev.omega24.upnp4j.util.Protocol;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.raphimc.minecraftauth.bedrock.exception.MinecraftRequestException;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.ProxyInfo;
import org.geysermc.mcprotocollib.network.tcp.TcpConnectionManager;
import org.geysermc.mcprotocollib.network.tcp.TcpServer;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ServerboundCookieResponsePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundTabListPacket;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.github.rfresh2.EventConsumer.of;
import static com.zenith.Globals.*;
import static com.zenith.util.DisconnectMessages.*;
import static com.zenith.util.config.Config.Authentication.AccountType.MSA;
import static com.zenith.util.config.Config.Authentication.AccountType.OFFLINE;
import static java.util.Objects.nonNull;


@Getter
public class Proxy {
    @Getter protected static final Proxy instance = new Proxy();
    protected ClientSession client;
    protected TcpServer server;
    protected final Path serverIconFilePath = Path.of("server-icon.png");
    protected byte[] serverIcon;
    protected final AtomicReference<ServerSession> currentPlayer = new AtomicReference<>();
    protected final FastArrayList<ServerSession> activeConnections = new FastArrayList<>(ServerSession.class);
    @Nullable private Instant connectTime;
    private Instant disconnectTime = Instant.now();
    private final AtomicBoolean loggingIn = new AtomicBoolean(false);
    private LanBroadcaster lanBroadcaster;
    private TcpConnectionManager tcpManager;
    private FileLock fileLock;
    private final long startTime = System.currentTimeMillis();

    public static void main(String... args) {
        Locale.setDefault(Locale.ENGLISH);
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        setPropertySafe("io.netty.allocator.type", "pooled");
        setPropertySafe("reactor.schedulers.defaultPoolSize", "1");
        setPropertySafe("io.netty.allocator.numHeapArenas", "2");
        setPropertySafe("io.netty.allocator.numDirectArenas", "2");
        setPropertySafe("io.netty.leakDetection.level", "disabled");
        setPropertySafe("io.netty.noUnsafe", "false");
        instance.start();
    }

    private static void setPropertySafe(String key, String value) {
        if (System.getProperty(key) == null) System.setProperty(key, value);
    }

    public void initEventHandlers() {
        EVENT_BUS.subscribe(
            this,
            of(ClientDisconnectEvent.class, this::handleDisconnectEvent),
            of(ClientConnectEvent.class, this::handleConnectEvent),
            of(PrivateMessageSendEvent.class, this::handlePrivateMessageSendEvent)
        );
    }

    public void start() {
        BootstrapMessageConsumer.drainLogs();
        DEFAULT_LOG.info("Starting ZenithProxy-{}", VERSION);
        if (getExecutableReleaseVersion() == null) {
            DEFAULT_LOG.warn("Detected unofficial ZenithProxy development build!");
        }
        initEventHandlers();
        try {
            var configVerifyResult = ConfigVerifier.verifyConfigs();
            if (!configVerifyResult.pass()) {
                DEFAULT_LOG.error("Config verification failed: {}", configVerifyResult.msg());
                System.exit(69);
                return;
            }
            if (inDevEnv()) CONFIG.debug.debugLogs = true;
            if (CONFIG.debug.clearOldLogs) EXECUTOR.schedule(Proxy::clearOldLogs, 10L, TimeUnit.SECONDS);
            TerminalConsoleAppender.setPrintStdout(false); // cut-over to jline console
            TERMINAL.start();
            if (CONFIG.debug.lockFile) tryOpenLockFile();
            MODULE.init();
            this.tcpManager = new TcpConnectionManager();
            if (CONFIG.discord.enable) {
                try {
                    DISCORD.start();
                    DISCORD_LOG.info("Started Discord Bot");
                } catch (final Throwable e) {
                    DISCORD_LOG.error("Failed starting discord bot: {}", e.getMessage());
                    DISCORD_LOG.debug("Failed starting discord bot", e);
                }
            }
            BootstrapMessageConsumer.drain();
            NotificationEventListener.INSTANCE.subscribeEvents();
            ChatRelayEventListener.INSTANCE.subscribeEvents();
            if (CONFIG.plugins.enabled) PLUGIN_MANAGER.initialize();
            saveConfigAsync();
            if (CONFIG.client.viaversion.enabled || CONFIG.server.viaversion.enabled) {
                VIA_INITIALIZER.init();
            }
            loadServerIcon();
            startServer();
            EXECUTOR.execute(DISCORD::updateBotInfo);
            EXECUTOR.execute(DISCORD::updateBotAvatar);
            CACHE.reset(CacheResetType.FULL);
            EXECUTOR.scheduleAtFixedRate(this::serverHealthCheck, 1L, 5L, TimeUnit.MINUTES);
            EXECUTOR.scheduleAtFixedRate(this::tablistUpdate, 20L, 3L, TimeUnit.SECONDS);
            EXECUTOR.scheduleAtFixedRate(this::maxPlaytimeTick, CONFIG.client.maxPlaytimeReconnectMins, 1L, TimeUnit.MINUTES);
            EXECUTOR.schedule(this::serverConnectionTest, 10L, TimeUnit.SECONDS);
            boolean connected = false;
            if (CONFIG.client.autoConnect && !isConnected()) {
                connectAndCatchExceptions();
                connected = true;
            }
            DEFAULT_LOG.info("ZenithProxy started!");
            if (!connected) {
                DEFAULT_LOG.info("Commands Help: use the `help` command");
                DEFAULT_LOG.info("Proxy IP: {}", CONFIG.server.getProxyAddress());
                DEFAULT_LOG.info("Use the `connect` command to log in!");
            }
            EVENT_BUS.post(ProxyLaunchedEvent.INSTANCE);
            Wait.waitSpinLoop();
        } catch (Exception e) {
            DEFAULT_LOG.error("", e);
        } finally {
            DEFAULT_LOG.info("Shutting down...");
            if (this.server != null) this.server.close(true);
            saveConfig();
        }
    }

    private static void clearOldLogs() {
        try (Stream<Path> walk = Files.walk(Path.of("log/"))) {
            walk.filter(path -> path.toString().endsWith(".zip")).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (final IOException e) {
                    DEFAULT_LOG.error("Error deleting old log file", e);
                }
            });
        } catch (final IOException e) {
            DEFAULT_LOG.error("Error deleting old log file", e);
        }
    }

    private void tryOpenLockFile() {
        var lockFile = new File(".lock");
        if (!lockFile.exists()) {
            try {
                lockFile.createNewFile();
            } catch (final Exception e) {
                DEFAULT_LOG.debug("Error creating lock file", e);
                return;
            }
        }
        try {
            var fos = new FileOutputStream(lockFile);
            fileLock = fos.getChannel().tryLock();
            if (fileLock == null) {
                var isTmux = System.getenv("TMUX") != null;
                var isScreen = Optional.ofNullable(System.getenv("TERM")).filter("screen"::equals).isPresent();
                var helpText = """
                   Close other ZenithProxy instances open in the current directory: `%s`
                   """.formatted(lockFile.getAbsoluteFile().getParent());
                if (isTmux || isScreen) {
                    var multiplexerName = isTmux ? "tmux" : "screen";
                    var cheatSheet = isTmux ? "https://tmuxcheatsheet.com" : "https://devhints.io/screen";
                    helpText += """
                        Most likely you have more than one %s session open.

                        For %s help: %s

                        Close other %s sessions open in the current directory
                        """.formatted(multiplexerName, multiplexerName, cheatSheet, multiplexerName);
                }
                DISCORD.sendEmbedMessage(Embed.builder()
                    .title("Error: Multiple ZenithProxy Instances Open")
                    .description(helpText)
                    .errorColor());
                Wait.wait(5);
                System.exit(1);
            }
        } catch (Exception e) {
            // fall through
        }
    }

    private void serverHealthCheck() {
        if (!CONFIG.server.enabled || !CONFIG.server.healthCheck) return;
        if (server != null && server.isListening()) return;
        SERVER_LOG.error("Server is not listening! Is another service on this port?");
        this.startServer();
        EXECUTOR.schedule(() -> {
            if (server == null || !server.isListening()) {
                var errorMessage = """
                    The ZenithProxy MC server was unable to start correctly.

                    Most likely you have two or more ZenithProxy instance running on the same configured port: %s.

                    Shut down duplicate instances, or change the configured port: `serverConnection port <port>`
                    """.formatted(CONFIG.server.bind.port);
                DISCORD.sendEmbedMessage(
                    Embed.builder()
                        .title("ZenithProxy Server Error")
                        .description(errorMessage)
                        .errorColor());
            }
        }, 30, TimeUnit.SECONDS);
    }

    private void serverConnectionTest() {
        if (!CONFIG.server.connectionTestOnStart) return;
        if (!CONFIG.server.enabled) return;
        if (server == null || !server.isListening()) return;
        if (!CONFIG.server.ping.enabled) return;
        var address = CONFIG.server.getProxyAddress();
        if (address.startsWith("localhost")) {
            SERVER_LOG.debug("Proxy IP is set to localhost, skipping connection test");
            return;
        }
        MCStatusApi.INSTANCE.getMCServerStatus(CONFIG.server.getProxyAddress())
            .ifPresentOrElse(response -> {
                if (response.online()) {
                    SERVER_LOG.debug("Connection test successful: {}", address);
                } else {
                    SERVER_LOG.error(
                        """
                        Unable to ping the configured `proxyIP`: {}

                        If you are actually able to connect to ZenithProxy you can disable this test: `connectionTest testOnStart off`

                        This test is most likely failing due to a firewall needing to be disabled.

                        If the `proxyIP` is incorrect, set `serverConnection proxyIP <ip>` with the correct IP.

                        For instructions on how to disable the firewall consult with your VPS provider. Each provider varies in steps and what word they refer to firewalls with.
                        """, address);
                }
            }, () -> {
                SERVER_LOG.debug("Failed trying to perform connection test");
                // reschedule another attempt?
            });
    }

    private void maxPlaytimeTick() {
        if (CONFIG.client.maxPlaytimeReconnect && isOnlineForAtLeastDuration(Duration.ofMinutes(CONFIG.client.maxPlaytimeReconnectMins))) {
            CLIENT_LOG.info("Max playtime minutes reached: {}, reconnecting...", CONFIG.client.maxPlaytimeReconnectMins);
            disconnect(MAX_PT_DISCONNECT);
            MODULE.get(AutoReconnect.class).cancelAutoReconnect();
            connect();
        }
    }

    private void tablistUpdate() {
        var playerConnection = currentPlayer.get();
        if (!this.isConnected() || playerConnection == null) return;
        if (!playerConnection.isLoggedIn()) return;
        long lastUpdate = CACHE.getTabListCache().getLastUpdate();
        if (lastUpdate < System.currentTimeMillis() - 3000) {
            playerConnection.sendAsync(new ClientboundTabListPacket(CACHE.getTabListCache().getHeader(), CACHE.getTabListCache().getFooter()));
            CACHE.getTabListCache().setLastUpdate(System.currentTimeMillis());
        }
    }

    /**
     * Launcher will restart it after this
     */
    public void stop() {
        stop(true);
    }

    public void stop(boolean restart) {
        DEFAULT_LOG.info("Shutting Down...");
        try {
            CompletableFuture.runAsync(() -> {
                if (nonNull(this.client)) this.client.disconnect(SERVER_CLOSING_MESSAGE);
                var autoReconnect = MODULE.get(AutoReconnect.class);
                if (nonNull(autoReconnect)) autoReconnect.cancelAutoReconnect();
                stopServer();
                if (nonNull(tcpManager)) tcpManager.close();
                saveConfig();
                DISCORD.stop(true);
            }).get(10L, TimeUnit.SECONDS);
        } catch (final Exception e) {
            DEFAULT_LOG.error("Error shutting down gracefully", e);
        } finally {
            try {
                ((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
            } finally {
                System.exit(restart ? 0 : 69);
            }
        }
    }

    public void disconnect() {
        disconnect(MANUAL_DISCONNECT);
    }

    public void disconnect(final String reason, final Throwable cause) {
        if (this.isConnected()) {
            if (CONFIG.debug.kickDisconnect) this.kickDisconnect(reason, cause);
            else this.client.disconnect(reason, cause);
        }
    }

    public void disconnect(final String reason) {
        if (this.isConnected()) {
            if (CONFIG.debug.kickDisconnect) this.kickDisconnect(reason, null);
            else this.client.disconnect(reason);
        }
    }

    public void kickDisconnect(final String reason, final Throwable cause) {
        if (!isConnected()) return;
        var client = this.client;
        try {
            client.send(new ServerboundCookieResponsePacket(Key.key("minecraft", "pls_kick"), null)).get();
        } catch (final Exception e) {
            CLIENT_LOG.error("Error performing kick disconnect", e);
        }
        // note: this will occur before the server sends us back a disconnect packet, but before our channel close is received by the server
        client.disconnect(reason, cause);
    }

    public void connectAndCatchExceptions() {
        try {
            this.connect();
        } catch (final Exception e) {
            DEFAULT_LOG.error("Error connecting", e);
        }
    }

    /**
     * @throws IllegalStateException if already connected
     */
    public synchronized void connect() {
        connect(CONFIG.client.server.address, CONFIG.client.server.port);
    }

    public synchronized void connect(final String address, final int port) {
        if (this.isConnected()) throw new IllegalStateException("Already connected!");
        if (this.client != null && !this.client.isTerminalState()) throw new IllegalStateException("Not Disconnected!");
        this.connectTime = Instant.now();
        final MinecraftProtocol minecraftProtocol;
        try {
            EVENT_BUS.postAsync(new ClientStartConnectEvent());
            minecraftProtocol = this.logIn();
        } catch (final Exception e) {
            EVENT_BUS.post(new ClientLoginFailedEvent(e));
            disconnectAllServerSessions(Component.text("Login failed"));
            EXECUTOR.schedule(() -> EVENT_BUS.post(new ClientDisconnectEvent(LOGIN_FAILED)), 1L, TimeUnit.SECONDS);
            return;
        }
        CLIENT_LOG.info("Connecting to {}:{}...", address, port);
        this.client = new ClientSession(address, port, CONFIG.client.bindAddress, minecraftProtocol, getClientProxyInfo(), tcpManager);
        this.client.setReadTimeout(CONFIG.client.timeout.enable ? CONFIG.client.timeout.seconds : 0);
        this.client.setFlag(MinecraftConstants.CLIENT_CHANNEL_INITIALIZER, ZenithClientChannelInitializer.FACTORY);
        this.client.connect(true);
        // wait for connection state to stabilize
        Wait.waitUntil(() -> this.client.isConnected() || this.client.isTerminalState(), 30);
    }

    @Nullable
    private static ProxyInfo getClientProxyInfo() {
        ProxyInfo proxyInfo = null;
        if (CONFIG.client.connectionProxy.enabled) {
            if (!CONFIG.client.connectionProxy.user.isEmpty() || !CONFIG.client.connectionProxy.password.isEmpty())
                proxyInfo = new ProxyInfo(CONFIG.client.connectionProxy.type,
                                          new InetSocketAddress(CONFIG.client.connectionProxy.host,
                                                                CONFIG.client.connectionProxy.port),
                                          CONFIG.client.connectionProxy.user,
                                          CONFIG.client.connectionProxy.password);
            else proxyInfo = new ProxyInfo(CONFIG.client.connectionProxy.type,
                                           new InetSocketAddress(CONFIG.client.connectionProxy.host,
                                                                 CONFIG.client.connectionProxy.port));
        }
        return proxyInfo;
    }

    public boolean isConnected() {
        return this.client != null && this.client.isConnected();
    }

    public boolean isOnline() {
        var client = this.client;
        return client != null && client.isConnected() && client.isOnline();
    }

    private void loadServerIcon() {
        this.serverIcon = loadServerIconFile().orElse(loadServerIconDefault());
    }

    private void writeServerIconFile() {
        try (var out = Files.newOutputStream(serverIconFilePath)) {
            out.write(serverIcon);
        } catch (Exception e) {
            DEFAULT_LOG.error("Error writing server icon", e);
        }
    }

    private Optional<byte[]> loadServerIconFile() {
        if (!serverIconFilePath.toFile().exists()) {
            return Optional.empty();
        }
        try {
            var iconBytes = Files.readAllBytes(serverIconFilePath);
            var pngReader = new PngReader(new ByteArrayInputStream(iconBytes));
            if (pngReader.imgInfo.rows != 64 || pngReader.imgInfo.cols != 64) {
                DEFAULT_LOG.error("Server icon must be 64x64, currently {}x{}", pngReader.imgInfo.cols, pngReader.imgInfo.rows);
                return Optional.empty();
            }
            return Optional.of(iconBytes);
        } catch (Exception e) {
            DEFAULT_LOG.error("Error loading server icon file", e);
            return Optional.empty();
        }
    }

    private byte[] loadServerIconDefault() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("server-icon.png")) {
            return in.readAllBytes();
        } catch (final Exception e) {
            SERVER_LOG.error("Failed loading server icon", e);
            return new byte[0];
        }
    }

    @SneakyThrows
    public synchronized void startServer() {
        if (this.server != null && this.server.isListening())
            throw new IllegalStateException("Server already started!");
        if (!CONFIG.server.enabled) return;
        var address = CONFIG.server.bind.address;
        var port = CONFIG.server.bind.port;
        SERVER_LOG.info("Starting server on {}:{}...", address, port);
        this.server = new TcpServer(address, port, MinecraftProtocol::new, tcpManager, (socketAddress) -> new ServerSession(socketAddress.getHostName(), socketAddress.getPort(), (MinecraftProtocol) server.createPacketProtocol(), server));
        this.server.setGlobalFlag(MinecraftConstants.SERVER_CHANNEL_INITIALIZER, ZenithServerChannelInitializer.FACTORY);
        if (this.lanBroadcaster == null && CONFIG.server.ping.lanBroadcast) {
            this.lanBroadcaster = new LanBroadcaster();
            lanBroadcaster.start();
        }
        this.server.addListener(new ProxyServerListener());
        this.server.bind(false);
        if (CONFIG.server.upnp) {
            EXECUTOR.execute(this::openUpnp);
        }
    }

    public void openUpnp() {
        try {
            if (UPnP4J.isUPnPAvailable()) {
                if (UPnP4J.open(server.getPort(), Protocol.TCP)) {
                    SERVER_LOG.info("Opened UPnP address: {}:{}", UPnP4J.getExternalIP(), server.getPort());
                } else {
                    SERVER_LOG.info("Failed to open UPnP address: {}:{}", UPnP4J.getExternalIP(), server.getPort());
                }
            } else {
                SERVER_LOG.debug("UPnP not available!");
            }
        } catch (final Exception e) {
            SERVER_LOG.error(e.getMessage(), e);
        }
    }

    public void closeUpnp() {
        try {
            if (UPnP4J.isUPnPAvailable()) {
                if (UPnP4J.close(server.getPort(), Protocol.TCP)) {
                    SERVER_LOG.info("Closed UPnP address: {}:{}", UPnP4J.getExternalIP(), server.getPort());
                } else {
                    SERVER_LOG.info("Failed to close UPnP address: {}:{}", UPnP4J.getExternalIP(), server.getPort());
                }
            } else {
                SERVER_LOG.debug("UPnP not available!");
            }
        } catch (final Exception e) {
            SERVER_LOG.error(e.getMessage(), e);
        }
    }

    public synchronized void stopServer() {
        SERVER_LOG.info("Stopping server...");
        if (this.server != null && this.server.isListening()) this.server.close(true);
        if (this.lanBroadcaster != null) {
            this.lanBroadcaster.stop();
            this.lanBroadcaster = null;
        }
        if (this.server != null && CONFIG.server.upnp) {
            closeUpnp();
        }
    }

    public synchronized @NonNull MinecraftProtocol logIn() {
        if (!loggingIn.compareAndSet(false, true)) throw new RuntimeException("Already logging in!");
        AUTH_LOG.info("Logging in {}...", CONFIG.authentication.username);
        MinecraftProtocol minecraftProtocol = null;
        for (int tries = 0; tries < 3; tries++) {
            minecraftProtocol = retrieveLoginTaskResult(loginTask());
            if (minecraftProtocol != null || !loggingIn.get()) break;
            AUTH_LOG.warn("Failed login attempt {}", tries + 1);
            Wait.wait(ThreadLocalRandom.current().nextInt(3, 8));
        }
        if (!loggingIn.compareAndSet(true, false)) throw new RuntimeException("Login Cancelled");
        if (minecraftProtocol == null) throw new RuntimeException("Auth failed");
        var username = minecraftProtocol.getProfile().getName();
        var uuid = minecraftProtocol.getProfile().getId();
        CACHE.getChatCache().setPlayerCertificates(minecraftProtocol.getProfile().getPlayerCertificates());
        AUTH_LOG.info("Logged in as {} [{}].", username, uuid);
        if (CONFIG.server.extra.whitelist.autoAddClient && CONFIG.authentication.accountType != OFFLINE)
            if (PLAYER_LISTS.getWhitelist().add(username, uuid))
                SERVER_LOG.info("Auto added {} [{}] to whitelist", username, uuid);
        if (CONFIG.server.updateServerIcon) {
            final GameProfile profile = minecraftProtocol.getProfile();
            EXECUTOR.execute(() -> {
                updateServerIcon(profile);
                DISCORD.updateBotAvatar();
            });
        }
        if (CONFIG.discord.manageNickname) {
            EXECUTOR.execute(DISCORD::updateBotNickname);
        }
        return minecraftProtocol;
    }

    public Future<MinecraftProtocol> loginTask() {
        return EXECUTOR.submit(() -> {
            try {
                return Authenticator.INSTANCE.login();
            } catch (final Exception e) {
                if (e instanceof InterruptedException) {
                    return null;
                }
                CLIENT_LOG.error("Login failed", e);
                EVENT_BUS.postAsync(new ClientLoginFailedEvent(e));
                if (e instanceof MinecraftRequestException mre) {
                    if (mre.getResponse().getStatusCode() == 404) {
                        AUTH_LOG.error("""
                          [Help]
                          Log into the account with the vanilla MC launcher and join a server. Then try again with ZenithProxy.

                          Another possible cause is your microsoft account needs to have a password set. Meaning are using email codes to log in instead of passwords.
                          """);
                    }
                }
                return null;
            }
        });
    }

    public MinecraftProtocol retrieveLoginTaskResult(Future<MinecraftProtocol> loginTask) {
        try {
            var maxWait = CONFIG.authentication.accountType == MSA ? 10 : 300;
            for (int currentWait = 0; currentWait < maxWait; currentWait++) {
                if (loginTask.isDone()) break;
                if (!loggingIn.get()) {
                    loginTask.cancel(true);
                    return null;
                }
                Wait.wait(1);
            }
            return loginTask.get(1L, TimeUnit.SECONDS);
        } catch (Exception e) {
            loginTask.cancel(true);
            return null;
        }
    }

    public URL getPlayerHeadURL(UUID uuid) {
        return getPlayerHeadURL(uuid.toString().replace("-", ""));
    }

    public URL getPlayerHeadURL(String playerName) {
        try {
            return URI.create(String.format("https://minotar.net/helm/%s/64", playerName)).toURL();
        } catch (MalformedURLException e) {
            SERVER_LOG.error("Failed to get player head URL for: {}", playerName, e);
            throw new UncheckedIOException(e);
        }
    }

    public URL getPlayerBodyURL(UUID uuid) {
        try {
            return URI.create(String.format("https://api.mineatar.io/body/full/%s", uuid)).toURL();
        } catch (MalformedURLException e) {
            SERVER_LOG.error("Failed to get player body URL for: {}", uuid, e);
            throw new UncheckedIOException(e);
        }
    }

    // returns true if we were previously trying to log in
    public boolean cancelLogin() {
        return this.loggingIn.getAndSet(false);
    }

    public List<ServerSession> getSpectatorConnections() {
        var connections = getActiveConnections().getArray();
        // optimize most frequent cases as fast-paths to avoid list alloc
        if (connections.length == 0) return Collections.emptyList();
        if (connections.length == 1 && hasActivePlayer()) return Collections.emptyList();
        final List<ServerSession> result = new ArrayList<>(hasActivePlayer() ? connections.length - 1 : connections.length);
        for (int i = 0; i < connections.length; i++) {
            var connection = connections[i];
            if (connection.isSpectator()) {
                result.add(connection);
            }
        }
        return result;
    }

    public boolean hasActivePlayer() {
        ServerSession player = this.currentPlayer.get();
        return player != null && player.isLoggedIn();
    }

    public @Nullable ServerSession getActivePlayer() {
        ServerSession player = this.currentPlayer.get();
        if (player != null && player.isLoggedIn()) return player;
        else return null;
    }

    public void kickNonWhitelistedPlayers() {
        var connections = Proxy.getInstance().getActiveConnections().getArray();
        for (int i = 0; i < connections.length; i++) {
            var connection = connections[i];
            if (connection.getProfileCache().getProfile() == null) continue;
            if (PLAYER_LISTS.getWhitelist().contains(connection.getProfileCache().getProfile())) continue;
            if (PLAYER_LISTS.getSpectatorWhitelist().contains(connection.getProfileCache().getProfile()) && connection.isSpectator()) continue;
            connection.disconnect("Not whitelisted");
        }
    }

    public boolean isOnlineForAtLeastDuration(Duration duration) {
        return isConnected()
            && nonNull(getConnectTime())
            && getConnectTime().isBefore(Instant.now().minus(duration));
    }

    void updateServerIcon(@NonNull GameProfile profile) {
        try {
            byte[] icon = SkinRetriever.getRenderedAvatar(profile)
                .orElse(serverIcon);
            var event = new ServerIconBuildEvent(icon);
            EVENT_BUS.post(event);
            this.serverIcon = event.getIcon();
            writeServerIconFile();
        } catch (final Throwable e) {
            SERVER_LOG.error("Failed updating server icon");
            SERVER_LOG.debug("Failed updating server icon", e);
        }
    }

    public long getOnlineTimeSeconds() {
        var proxyConnectTime = this.connectTime;
        return proxyConnectTime != null
            ? TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - proxyConnectTime.getEpochSecond()
            : 0L;
    }

    public String getOnlineTimeString() {
        return TimeUtil.formatDuration(getOnlineTimeSeconds());
    }

    public void disconnectAllServerSessions(Component reason) {
        var connections = Proxy.getInstance().getActiveConnections().getArray();
        for (int i = 0; i < connections.length; i++) {
            var connection = connections[i];
            connection.disconnect(reason);
        }
        var server = Proxy.getInstance().getServer();
        if (server != null) {
            var sessions = server.getSessions();
            for (int i = 0; i < sessions.size(); i++) {
                final var session = sessions.get(i);
                if (session.isConnected()) {
                    session.disconnect(reason);
                }
            }
        }
    }

    public void handleDisconnectEvent(ClientDisconnectEvent event) {
        CACHE.reset(CacheResetType.FULL);
        this.disconnectTime = Instant.now();
        TPS.reset();
    }

    public void handleConnectEvent(ClientConnectEvent event) {
        this.connectTime = Instant.now();
        if (!ChatSchemaParser.hasCustomSchema()) {
            CLIENT_LOG.warn("No custom chat schema found for server: {}, setting one may be required for chats and whispers to parse correctly: `help chatSchema`", ChatSchemaParser.getServerAddress());
        }
    }

    public void handlePrivateMessageSendEvent(PrivateMessageSendEvent event) {
        if (!isConnected()) return;
        CHAT_LOG.info(event.getContents());
        var connections = getActiveConnections().getArray();
        for (int i = 0; i < connections.length; i++) {
            var connection = connections[i];
            connection.sendAsyncMessage(event.getContents());
        }
    }
}
