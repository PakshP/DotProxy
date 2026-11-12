package com.dotproxy.network.server.handler.spectator.outgoing;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.data.game.level.notify.GameEvent;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket;

public class GameEventSpectatorOutgoingHandler implements PacketHandler<ClientboundGameEventPacket, ServerSession> {
    @Override
    public ClientboundGameEventPacket apply(final ClientboundGameEventPacket packet, final ServerSession session) {
        if (packet.getNotification() == GameEvent.CHANGE_GAMEMODE) {
            return null;
        }
        return packet;
    }
}
