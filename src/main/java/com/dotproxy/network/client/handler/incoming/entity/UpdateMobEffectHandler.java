package com.dotproxy.network.client.handler.incoming.entity;

import com.dotproxy.cache.data.entity.Entity;
import com.dotproxy.cache.data.entity.EntityLiving;
import com.dotproxy.cache.data.entity.PotionEffect;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundUpdateMobEffectPacket;
import org.jspecify.annotations.NonNull;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.Globals.CLIENT_LOG;

public class UpdateMobEffectHandler implements ClientEventLoopPacketHandler<ClientboundUpdateMobEffectPacket, ClientSession> {
    @Override
    public boolean applyAsync(@NonNull ClientboundUpdateMobEffectPacket packet, @NonNull ClientSession session) {
        try {
            Entity entity = CACHE.getEntityCache().get(packet.getEntityId());
            if (entity instanceof EntityLiving) {
                ((EntityLiving) entity).getPotionEffectMap().put(packet.getEffect(), new PotionEffect(
                    packet.getEffect(),
                    packet.getAmplifier(),
                    packet.getDuration(),
                    packet.isAmbient(),
                    packet.isShowParticles(),
                    packet.isShowIcon(),
                    packet.isBlend()
                ));
            } else {
                CLIENT_LOG.debug("Received ServerEntityEffectPacket for invalid entity (id={})", packet.getEntityId());
                return true;
            }
        } catch (ClassCastException e)  {
            CLIENT_LOG.debug("Received ServerEntityEffectPacket for non-equipment entity (id={})", packet.getEntityId(), e);
            return true;
        }
        return true;
    }
}
