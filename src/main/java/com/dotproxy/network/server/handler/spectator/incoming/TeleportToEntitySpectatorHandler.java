package com.dotproxy.network.server.handler.spectator.incoming;

import com.dotproxy.cache.data.entity.Entity;
import com.dotproxy.feature.spectator.SpectatorSync;
import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSetCameraPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.level.ServerboundTeleportToEntityPacket;

import static com.dotproxy.Globals.CACHE;

public class TeleportToEntitySpectatorHandler implements PacketHandler<ServerboundTeleportToEntityPacket, ServerSession> {
    @Override
    public ServerboundTeleportToEntityPacket apply(final ServerboundTeleportToEntityPacket packet, final ServerSession session) {
        final Entity targetEntity = CACHE.getEntityCache().get(packet.getTarget());
        if (targetEntity != null) {
            if (session.hasCameraTarget()) {
                session.setCameraTarget(null);
                session.send(new ClientboundSetCameraPacket(session.getSpectatorSelfEntityId()));
            }
            SpectatorSync.syncSpectatorPositionToEntity(session, targetEntity);
        }
        return null;
    }
}
