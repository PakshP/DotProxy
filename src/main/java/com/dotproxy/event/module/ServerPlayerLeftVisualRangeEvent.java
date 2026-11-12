package com.dotproxy.event.module;

import com.dotproxy.cache.data.entity.EntityPlayer;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;

public record ServerPlayerLeftVisualRangeEvent(PlayerListEntry playerEntry, EntityPlayer playerEntity) { }
