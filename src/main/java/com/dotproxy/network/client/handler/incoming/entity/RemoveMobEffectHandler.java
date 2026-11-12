package com.dotproxy.network.client.handler.incoming.entity;

import com.dotproxy.cache.data.entity.Entity;
import com.dotproxy.cache.data.entity.EntityLiving;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveMobEffectPacket;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.Globals.CLIENT_LOG;

public class RemoveMobEffectHandler implements ClientEventLoopPacketHandler<ClientboundRemoveMobEffectPacket, ClientSession> {

    @Override
    public boolean applyAsync(ClientboundRemoveMobEffectPacket packet, ClientSession session) {
        try {
            Entity entity = CACHE.getEntityCache().get(packet.getEntityId());
            if (entity instanceof EntityLiving e) {
                e.getPotionEffectMap().remove(packet.getEffect());
            } else {
                CLIENT_LOG.debug("Received ClientboundRemoveMobEffectPacket for invalid entity (id={})", packet.getEntityId());
                return true;
            }
        } catch (Exception e) {
            CLIENT_LOG.debug("Failed handling ClientboundRemoveMobEffectPacket for entity (id={})", packet.getEntityId(), e);
        }
        return true;
    }
}
