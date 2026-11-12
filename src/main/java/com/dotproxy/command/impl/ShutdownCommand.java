package com.dotproxy.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.dotproxy.Proxy;
import com.dotproxy.command.api.Command;
import com.dotproxy.command.api.CommandCategory;
import com.dotproxy.command.api.CommandContext;
import com.dotproxy.command.api.CommandUsage;
import com.dotproxy.discord.Embed;

public class ShutdownCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("shutdown")
            .category(CommandCategory.MANAGE)
            .description("""
                Shuts down DotProxy, without letting the start script restart it.
                """)
            .aliases("exit")
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("shutdown").requires(Command::validateAccountOwner).executes(c -> {
            c.getSource().getSource().logEmbed(c.getSource(), Embed.builder()
                .title("Shutting down...")
                .errorColor()
            );
            c.getSource().setNoOutput(true);
            Proxy.getInstance().stop(false);
        });
    }
}
