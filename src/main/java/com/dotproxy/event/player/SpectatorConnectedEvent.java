package com.dotproxy.event.player;

import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.auth.GameProfile;

public record SpectatorConnectedEvent(ServerSession session, GameProfile clientGameProfile) { }
