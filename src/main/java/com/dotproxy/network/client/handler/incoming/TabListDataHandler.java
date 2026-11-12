package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.event.client.ClientOnlineEvent;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundTabListPacket;
import org.jspecify.annotations.NonNull;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.Globals.EVENT_BUS;

public class TabListDataHandler implements ClientEventLoopPacketHandler<ClientboundTabListPacket, ClientSession> {
    @Override
    public boolean applyAsync(@NonNull ClientboundTabListPacket packet, @NonNull ClientSession session) {
        CACHE.getTabListCache()
            .setHeader(packet.getHeader())
            .setFooter(packet.getFooter());
        if (!session.isOnline()) {
            session.setOnline(true);
            EVENT_BUS.post(new ClientOnlineEvent());
        }
        return true;
    }
}
