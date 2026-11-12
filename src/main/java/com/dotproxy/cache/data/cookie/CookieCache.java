package com.dotproxy.cache.data.cookie;

import com.dotproxy.cache.CacheResetType;
import com.dotproxy.cache.CachedData;
import lombok.Data;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.network.tcp.TcpSession;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCookieRequestPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundStoreCookiePacket;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;

import static com.dotproxy.Globals.CONFIG;
import static com.dotproxy.Globals.SERVER_LOG;
import static java.util.Arrays.asList;

// todo: generify this to support both ClientSession and ServerConnection caches?
@Data
@Accessors(chain = true)
public class CookieCache implements CachedData {
    private static final Key dotproxyTransferSrcKey = Key.key("dotproxy", "dotproxy-transfer-src");
    private static final Key dotproxySpectatorKey = Key.key("dotproxy", "dotproxy-spectator");
    private static final List<Key> dotproxyCookies = asList(dotproxyTransferSrcKey, dotproxySpectatorKey);
    private final Map<Key, @Nullable String> cookies = new HashMap<>(2);
    private final Queue<Key> requestedCookies = new LinkedBlockingDeque<>();

    public Optional<Boolean> getSpectatorCookieValue() {
        var value = cookies.get(dotproxySpectatorKey);
        if (value == null) return Optional.empty();
        return Optional.of(Boolean.parseBoolean(value));
    }

    public boolean receivedAllCookieResponses() {
        return dotproxyCookies.stream().allMatch(cookies::containsKey);
    }

    public boolean validateCookieRequested(Key key) {
        var next = requestedCookies.peek();
        if (next == null) return false;
        if (next.equals(key)) {
            requestedCookies.poll();
            return true;
        }
        return false;
    }

    public Optional<String> getDotProxyTransferSrc() {
        return Optional.ofNullable(cookies.get(dotproxyTransferSrcKey));
    }

    /**
     * S2C packets requesting cookie responses
     */
    @Override
    public void getPackets(@NonNull final Consumer<Packet> consumer, final @NonNull TcpSession session) {
        dotproxyCookies.forEach(c -> consumer.accept(new ClientboundCookieRequestPacket(c)));
    }

    public void getStoreSrcPacket(@NonNull final Consumer<Packet> consumer) {
        consumer.accept(new ClientboundStoreCookiePacket(dotproxyTransferSrcKey, CONFIG.server.proxyIP.getBytes()));
    }

    public void getStoreSpectatorDestPacket(@NonNull final Consumer<Packet> consumer, final boolean spectator) {
        consumer.accept(new ClientboundStoreCookiePacket(dotproxySpectatorKey, String.valueOf(spectator).getBytes()));
    }

    @Override
    public void reset(CacheResetType type) {
        if (type == CacheResetType.FULL) {
            cookies.clear();
        }
    }

    public void handleCookieResponse(final Key key, byte @Nullable [] value) {
        if (value == null) {
            cookies.put(key, null);
        } else {
            try {
                var val = new String(value);
                cookies.put(key, val);
                if (!dotproxyCookies.contains(key))
                    SERVER_LOG.debug("Received unrequested cookie response: {} : {}", key, val);
            } catch (final Throwable e) {
                SERVER_LOG.debug("Unable to parse cookie response to string for key: {}", key, e);
            }
        }
    }
}
