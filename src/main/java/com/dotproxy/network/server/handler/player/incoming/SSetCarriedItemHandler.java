package com.dotproxy.network.server.handler.player.incoming;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.ServerboundSetCarriedItemPacket;

import static com.dotproxy.Globals.CACHE;

public class SSetCarriedItemHandler implements PacketHandler<ServerboundSetCarriedItemPacket, ServerSession> {
    @Override
    public ServerboundSetCarriedItemPacket apply(final ServerboundSetCarriedItemPacket packet, final ServerSession session) {
        var isClientLoaded = session.isClientLoaded() || (System.nanoTime() >= session.getClientLoadedTimeout());
        if (!isClientLoaded && CACHE.getPlayerCache().isClientLoaded()) {
            return null;
        }
        return packet;
    }
}
