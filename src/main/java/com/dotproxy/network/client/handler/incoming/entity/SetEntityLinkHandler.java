package com.dotproxy.network.client.handler.incoming.entity;

import com.dotproxy.cache.data.entity.Entity;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityLinkPacket;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.Globals.CLIENT_LOG;

public class SetEntityLinkHandler implements ClientEventLoopPacketHandler<ClientboundSetEntityLinkPacket, ClientSession> {

    @Override
    public boolean applyAsync(ClientboundSetEntityLinkPacket packet, ClientSession session) {
        Entity entity = CACHE.getEntityCache().get(packet.getEntityId());
        if (entity != null) {
            if (packet.getAttachedToId() == -1) {
                entity.setLeashed(false).setLeashedId(-1);
            } else {
                entity.setLeashed(true).setLeashedId(packet.getAttachedToId());
            }
            return true;
        } else {
            CLIENT_LOG.debug("Received ServerEntityAttachPacket for invalid entity (id={})", packet.getEntityId());
            return true;
        }
    }
}
