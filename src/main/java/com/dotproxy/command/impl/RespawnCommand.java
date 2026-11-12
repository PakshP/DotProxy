package com.dotproxy.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.dotproxy.Proxy;
import com.dotproxy.cache.data.entity.EntityPlayer;
import com.dotproxy.command.api.Command;
import com.dotproxy.command.api.CommandCategory;
import com.dotproxy.command.api.CommandContext;
import com.dotproxy.command.api.CommandUsage;
import com.dotproxy.network.client.ClientSession;
import org.geysermc.mcprotocollib.protocol.data.game.ClientCommand;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundClientCommandPacket;

import static com.dotproxy.Globals.CACHE;
import static java.util.Objects.nonNull;

public class RespawnCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("respawn")
            .category(CommandCategory.MODULE)
            .description("Performs a respawn")
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("respawn")
            .executes(c -> {
                final ClientSession client = Proxy.getInstance().getClient();
                if (nonNull(client)) {
                    client.sendAsync(new ServerboundClientCommandPacket(ClientCommand.RESPAWN));
                }
                final EntityPlayer player = CACHE.getPlayerCache().getThePlayer();
                if (nonNull(player)) {
                    player.setHealth(20.0f);
                }
                c.getSource().getEmbed()
                    .title("Respawn performed")
                    .primaryColor();
            });
    }
}
