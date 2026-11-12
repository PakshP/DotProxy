package com.dotproxy.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.dotproxy.Proxy;
import com.dotproxy.command.api.Command;
import com.dotproxy.command.api.CommandCategory;
import com.dotproxy.command.api.CommandContext;
import com.dotproxy.command.api.CommandUsage;

import static com.dotproxy.Globals.EXECUTOR;

public class ConnectCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("connect")
            .category(CommandCategory.CORE)
            .description("""
             Connects DotProxy to the destination MC server
             """)
            .aliases("c")
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("connect").executes(c -> {
            if (Proxy.getInstance().isConnected()) {
                c.getSource().getEmbed()
                        .title("Already Connected!");
            } else {
                EXECUTOR.execute(Proxy.getInstance()::connectAndCatchExceptions);
                c.getSource().setNoOutput(true);
            }
        });
    }
}
