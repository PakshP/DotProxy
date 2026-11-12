package com.dotproxy.network.client.handler.incoming.scoreboard;

import com.dotproxy.cache.data.scoreboard.Objective;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.scoreboard.ClientboundSetObjectivePacket;
import org.jspecify.annotations.NonNull;

import static com.dotproxy.Globals.CACHE;

public class SetObjectiveHandler implements ClientEventLoopPacketHandler<ClientboundSetObjectivePacket, ClientSession> {
    @Override
    public boolean applyAsync(@NonNull ClientboundSetObjectivePacket packet, @NonNull ClientSession session) {
        switch (packet.getAction()) {
            case ADD -> CACHE.getScoreboardCache().add(packet);
            case REMOVE -> CACHE.getScoreboardCache().remove(packet);
            case UPDATE -> {
                final Objective objective = CACHE.getScoreboardCache().get(packet.getName());
                if (objective != null) {
                    objective
                        .setDisplayName(packet.getDisplayName())
                        .setScoreType(packet.getType())
                        .setNumberFormat(packet.getNumberFormat());
                }
            }
        }

        return true;
    }
}
