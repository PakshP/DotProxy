package com.dotproxy.network.client.handler.incoming.scoreboard;

import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetDisplayObjectivePacket;
import org.jspecify.annotations.NonNull;

import static com.dotproxy.Globals.CACHE;

public class SetDisplayObjectiveHandler implements ClientEventLoopPacketHandler<ClientboundSetDisplayObjectivePacket, ClientSession> {
    @Override
    public boolean applyAsync(@NonNull ClientboundSetDisplayObjectivePacket packet, @NonNull ClientSession session) {
        CACHE.getScoreboardCache().setPositionObjective(packet.getPosition(), packet.getName());
        return true;
    }
}
