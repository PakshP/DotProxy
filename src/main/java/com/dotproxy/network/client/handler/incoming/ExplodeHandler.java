package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.Proxy;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundExplodePacket;

import static com.dotproxy.Globals.BOT;

public class ExplodeHandler implements ClientEventLoopPacketHandler<ClientboundExplodePacket, ClientSession> {
    @Override
    public boolean applyAsync(final ClientboundExplodePacket packet, final ClientSession session) {
        if (!Proxy.getInstance().hasActivePlayer())
            BOT.handleExplosion(packet);
        return true;
    }
}
