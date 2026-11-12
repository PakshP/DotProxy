package com.dotproxy.event.player;

import com.dotproxy.network.server.ServerSession;

public record PlayerConfigurationEvent() {
    /**
     * Fired when the player has entered the initial login -> configuration phase, on dotproxy having received the ServerboundLoginAcknowledgedPacket
     */
    public record Entered(ServerSession session) { }

    /**
     * Fired right before the client exits the initial configuration phase, right before dotproxy sends the ClientboundFinishConfigurationPacket
     */
    public record Exiting(ServerSession session) { }

    /**
     * Fired when the player has exited the configuration phase, on dotproxy receiving the ServerboundFinishConfigurationPacket
     */
    public record Exited(ServerSession session) { }
}
