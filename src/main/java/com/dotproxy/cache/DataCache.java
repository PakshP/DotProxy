package com.dotproxy.cache;

import com.dotproxy.cache.data.PlayerCache;
import com.dotproxy.cache.data.ServerProfileCache;
import com.dotproxy.cache.data.bossbar.BossBarCache;
import com.dotproxy.cache.data.chat.ChatCache;
import com.dotproxy.cache.data.chunk.ChunkCache;
import com.dotproxy.cache.data.config.ConfigurationCache;
import com.dotproxy.cache.data.entity.EntityCache;
import com.dotproxy.cache.data.info.ClientInfoCache;
import com.dotproxy.cache.data.map.MapDataCache;
import com.dotproxy.cache.data.recipe.RecipeCache;
import com.dotproxy.cache.data.registry.RegistriesCache;
import com.dotproxy.cache.data.scoreboard.ScoreboardCache;
import com.dotproxy.cache.data.stats.StatisticsCache;
import com.dotproxy.cache.data.tab.TabListCache;
import com.dotproxy.cache.data.team.TeamCache;
import com.dotproxy.network.server.ServerSession;
import lombok.Getter;

import java.util.Collection;
import java.util.List;

import static com.dotproxy.Globals.CACHE_LOG;
import static com.dotproxy.Globals.SERVER_LOG;


@Getter
public class DataCache {
    protected final ChunkCache chunkCache = new ChunkCache();
    protected final TabListCache tabListCache = new TabListCache();
    protected final BossBarCache bossBarCache = new BossBarCache();
    protected final EntityCache entityCache = new EntityCache();
    protected final PlayerCache playerCache = new PlayerCache(entityCache);
    protected final ChatCache chatCache = new ChatCache();
    protected final ServerProfileCache profileCache = new ServerProfileCache();
    protected final StatisticsCache statsCache = new StatisticsCache();
    protected final MapDataCache mapDataCache = new MapDataCache();
    protected final RecipeCache recipeCache = new RecipeCache();
    protected final TeamCache teamCache = new TeamCache();
    protected final ScoreboardCache scoreboardCache = new ScoreboardCache();
    protected final ConfigurationCache configurationCache = new ConfigurationCache();
    protected final ClientInfoCache clientInfoCache = new ClientInfoCache();
    protected final RegistriesCache registriesCache = new RegistriesCache();
    {
        entityCache.subscribeEvents();
        chunkCache.subscribeEvents();
    }

    public Collection<CachedData> getAllData() {
        // order is important, matches vanilla
        return List.of(
            profileCache, playerCache, chunkCache, statsCache, tabListCache, bossBarCache, entityCache,
            chatCache, mapDataCache, recipeCache, teamCache, scoreboardCache,
            // special case caches that don't provide packets on world join
            // only here so they are still reset
            // todo: cleaner interface for caches that send at different times
            clientInfoCache, configurationCache, registriesCache
        );
    }

    // limited selection of relevant cache data for spectators
    // and replacing player cache with spectator's
    public Collection<CachedData> getAllDataSpectator(final PlayerCache spectatorPlayerCache) {
        return List.of(
            spectatorPlayerCache, chunkCache, tabListCache, bossBarCache, entityCache, chatCache,
            mapDataCache, recipeCache, teamCache, scoreboardCache
        );
    }

    public boolean reset(CacheResetType type) {
        CACHE_LOG.debug("Clearing cache using type: {}...", type.name().toLowerCase());
        try {
            this.getAllData().forEach(d -> d.reset(type));
            CACHE_LOG.debug("Cache cleared.");
        } catch (Exception e) {
            throw new RuntimeException("Unable to clear cache", e);
        }
        return true;
    }

    public static void sendCacheData(final Collection<CachedData> cacheData, final ServerSession connection) {
        cacheData.forEach(data -> {
            String msg = data.getSendingMessage();
            if (msg == null) SERVER_LOG.debug("Sending data to client {}", data.getClass().getSimpleName());
            else SERVER_LOG.debug(msg);
            data.getPackets(connection::send, connection);
        });
    }
}
