package com.dotproxy.network.server.handler.spectator.incoming;

import com.dotproxy.Proxy;
import com.dotproxy.cache.data.entity.Entity;
import com.dotproxy.feature.spectator.SpectatorSync;
import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerState;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSetCameraPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundPlayerCommandPacket;

public class PlayerCommandSpectatorHandler implements PacketHandler<ServerboundPlayerCommandPacket, ServerSession> {

    @Override
    public ServerboundPlayerCommandPacket apply(ServerboundPlayerCommandPacket packet, ServerSession session) {
        Entity cameraTarget = session.getCameraTarget();
        if (cameraTarget != null) {
            if (packet.getState() == PlayerState.START_SNEAKING) {
                session.setCameraTarget(null);
                session.send(new ClientboundSetCameraPacket(session.getSpectatorSelfEntityId()));
                SpectatorSync.syncSpectatorPositionToEntity(session, cameraTarget);
            }
        } else {
            if (packet.getState() == PlayerState.START_SNEAKING || packet.getState() == PlayerState.START_SPRINTING) {
                session.getSoundPacket().ifPresent(p -> {
                    var connections = Proxy.getInstance().getActiveConnections().getArray();
                    for (int i = 0; i < connections.length; i++) {
                        var connection = connections[i];
                        connection.send(p);
                    }
                });
            }
        }
        return null;
    }
}
