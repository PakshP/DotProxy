package com.dotproxy.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.dotproxy.Proxy;
import com.dotproxy.command.api.Command;
import com.dotproxy.command.api.CommandCategory;
import com.dotproxy.command.api.CommandContext;
import com.dotproxy.command.api.CommandUsage;
import com.dotproxy.discord.Embed;

public class RestartCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("restart")
            .category(CommandCategory.MANAGE)
            .description("""
                Restarts DotProxy.

                When started with the start script (start.sh / start.bat) or as a systemd service, DotProxy comes
                back up automatically and loads any new DotProxy.jar. Use this after replacing the jar to update.

                Without a start script or service, this behaves like `shutdown`.
                """)
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("restart").requires(Command::validateAccountOwner).executes(c -> {
            c.getSource().getSource().logEmbed(c.getSource(), Embed.builder()
                .title("Restarting...")
                .pendingColor()
            );
            c.getSource().setNoOutput(true);
            Proxy.getInstance().stop(true);
        });
    }
}
