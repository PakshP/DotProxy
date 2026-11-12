package com.dotproxy.event.player;

import com.dotproxy.network.server.ServerSession;

// the spectator has logged in and been sent the ClientboundLoginPacket
public record SpectatorLoggedInEvent(ServerSession session) { }
