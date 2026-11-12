package com.dotproxy.network.client.handler.outgoing;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.PacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundPlayerLoadedPacket;

import static com.dotproxy.Globals.CACHE;

public class OutgoingPlayerLoadedHandler implements PacketHandler<ServerboundPlayerLoadedPacket, ClientSession> {
    @Override
    public ServerboundPlayerLoadedPacket apply(final ServerboundPlayerLoadedPacket packet, final ClientSession session) {
        if (CACHE.getPlayerCache().isClientLoaded()) {
            return null;
        } else {
            CACHE.getPlayerCache().setClientLoaded(true);
            return packet;
        }
    }
}
