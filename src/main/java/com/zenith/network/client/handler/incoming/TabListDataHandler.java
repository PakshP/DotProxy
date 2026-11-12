package com.zenith.network.client.handler.incoming;

import com.zenith.event.client.ClientOnlineEvent;
import com.zenith.network.client.ClientSession;
import com.zenith.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundTabListPacket;
import org.jspecify.annotations.NonNull;

import static com.zenith.Globals.CACHE;
import static com.zenith.Globals.EVENT_BUS;

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
