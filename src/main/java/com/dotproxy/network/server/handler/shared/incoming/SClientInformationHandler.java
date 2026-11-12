package com.dotproxy.network.server.handler.shared.incoming;

import com.dotproxy.network.codec.PacketHandler;
import com.dotproxy.network.server.ServerSession;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundClientInformationPacket;

public class SClientInformationHandler implements PacketHandler<ServerboundClientInformationPacket, ServerSession> {
    public static final SClientInformationHandler INSTANCE = new SClientInformationHandler();
    @Override
    public ServerboundClientInformationPacket apply(final ServerboundClientInformationPacket packet, final ServerSession session) {
        session.getClientInfoCache()
            .setLocale(packet.getLanguage())
            .setRenderDistance(packet.getViewDistance())
            .setChatVisibility(packet.getChatVisibility())
            .setChatColors(packet.isChatColors())
            .setHandPreference(packet.getMainHand())
            .setTextFilteringEnabled(packet.isTextFilteringEnabled())
            .setAllowsListing(packet.isAllowsListing())
            .setParticleStatus(packet.getParticleStatus());
        return packet;
    }
}
