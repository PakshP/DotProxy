package com.dotproxy.network.client.handler.incoming.scoreboard;

import com.dotproxy.cache.data.scoreboard.Score;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetScorePacket;
import org.jspecify.annotations.NonNull;

import static com.dotproxy.Globals.CACHE;

public class SetScoreHandler implements ClientEventLoopPacketHandler<ClientboundSetScorePacket, ClientSession> {
    @Override
    public boolean applyAsync(@NonNull ClientboundSetScorePacket packet, @NonNull ClientSession session) {
        var objective = CACHE.getScoreboardCache().get(packet.getObjective());
        if (objective != null) {
            objective.getScores().put(packet.getOwner(), new Score(packet));
        }
        return true;
    }
}
