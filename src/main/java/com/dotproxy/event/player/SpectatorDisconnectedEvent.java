package com.dotproxy.event.player;

import org.geysermc.mcprotocollib.auth.GameProfile;

public record SpectatorDisconnectedEvent(com.dotproxy.network.server.ServerSession serverSession, GameProfile clientGameProfile) { }
