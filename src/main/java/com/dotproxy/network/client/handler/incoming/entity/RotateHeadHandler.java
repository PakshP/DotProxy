package com.dotproxy.network.client.handler.incoming.entity;

import com.dotproxy.cache.data.entity.Entity;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRotateHeadPacket;
import org.jspecify.annotations.NonNull;

import static com.dotproxy.Globals.CACHE;

public class RotateHeadHandler implements ClientEventLoopPacketHandler<ClientboundRotateHeadPacket, ClientSession> {
    @Override
    public boolean applyAsync(@NonNull ClientboundRotateHeadPacket packet, @NonNull ClientSession session) {
        Entity entity = CACHE.getEntityCache().get(packet.getEntityId());
        if (entity != null) {
            entity.lerpHeadTo(packet.getHeadYaw(), 3);
        }
        return true;
    }
}
