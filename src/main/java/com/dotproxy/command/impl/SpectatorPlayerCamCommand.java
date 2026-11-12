package com.dotproxy.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.dotproxy.Proxy;
import com.dotproxy.cache.data.entity.Entity;
import com.dotproxy.command.api.*;
import com.dotproxy.feature.spectator.SpectatorSync;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSetCameraPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundRemoveEntitiesPacket;

import static com.dotproxy.Globals.CACHE;
import static com.dotproxy.util.ComponentSerializer.minimessage;

public class SpectatorPlayerCamCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("playerCam")
            .category(CommandCategory.MANAGE)
            .description("Toggles spectators between player and entity cameras. Only usable by spectators")
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("playerCam").requires(c -> Command.validateCommandSource(c, CommandSources.SPECTATOR)).executes(c -> {
            var session = c.getSource().getInGamePlayerInfo().session();
            final Entity existingTarget = session.getCameraTarget();
            if (existingTarget != null) {
                session.setCameraTarget(null);
                session.sendAsync(new ClientboundSetCameraPacket(session.getSpectatorSelfEntityId()));
                SpectatorSync.syncSpectatorPositionToEntity(session, existingTarget);
                session.sendAsyncMessage(minimessage("<blue>Exited playercam!"));
            } else {
                session.setCameraTarget(CACHE.getPlayerCache().getThePlayer());
                session.sendAsync(new ClientboundSetCameraPacket(CACHE.getPlayerCache().getEntityId()));
                var connections = Proxy.getInstance().getActiveConnections().getArray();
                for (int i = 0; i < connections.length; i++) {
                    var connection = connections[i];
                    connection.sendAsync(new ClientboundRemoveEntitiesPacket(new int[]{session.getSpectatorEntityId()}));
                }
                session.sendAsyncMessage(minimessage("<blue>Entered playercam!"));
            }
            c.getSource().setNoOutput(true);
        });
    }
}
