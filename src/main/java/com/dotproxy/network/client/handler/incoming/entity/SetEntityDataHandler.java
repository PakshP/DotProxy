package com.dotproxy.network.client.handler.incoming.entity;

import com.dotproxy.Proxy;
import com.dotproxy.cache.data.entity.Entity;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityDataPacket;
import org.jspecify.annotations.NonNull;

import static com.dotproxy.Globals.BOT;
import static com.dotproxy.Globals.CACHE;

public class SetEntityDataHandler implements ClientEventLoopPacketHandler<ClientboundSetEntityDataPacket, ClientSession> {
    @Override
    public boolean applyAsync(@NonNull ClientboundSetEntityDataPacket packet, @NonNull ClientSession session) {
        Entity entity = CACHE.getEntityCache().get(packet.getEntityId());
        if (entity != null) {
            for (int i = 0; i < packet.getMetadata().size(); i++) {
                var metadata = packet.getMetadata().get(i);
                entity.getMetadata().put(metadata.getId(), metadata);
            }
            if (!Proxy.getInstance().hasActivePlayer() && entity == CACHE.getPlayerCache().getThePlayer()) {
                BOT.onMetadataUpdated(packet.getMetadata());
            }
        }
        return true;
    }
}
