package com.dotproxy.event.player;

import com.dotproxy.network.server.ServerSession;

public record PlayerConnectionRemovedEvent(ServerSession serverConnection) { }
