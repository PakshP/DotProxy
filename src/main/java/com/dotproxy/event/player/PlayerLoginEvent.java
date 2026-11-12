package com.dotproxy.event.player;

import com.dotproxy.network.server.ServerSession;

public record PlayerLoginEvent() {
    // after GameProfile but before Login packet is sent
    public record Pre(ServerSession session) { }

    // after Login packet is sent
    public record Post(ServerSession session) { } // triggered after Login packet is sent
}
