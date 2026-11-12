package com.dotproxy.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.dotproxy.Proxy;
import com.dotproxy.command.api.Command;
import com.dotproxy.command.api.CommandCategory;
import com.dotproxy.command.api.CommandContext;
import com.dotproxy.command.api.CommandUsage;
import com.dotproxy.module.impl.AutoReconnect;

import static com.dotproxy.Globals.DISCORD_LOG;
import static com.dotproxy.Globals.MODULE;

public class DisconnectCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("disconnect")
            .category(CommandCategory.CORE)
            .description("Disconnects DotProxy from the destination MC server")
            .aliases("dc")
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("disconnect").executes(c -> {
            if (!Proxy.getInstance().isConnected()) {
                boolean loginCancelled = Proxy.getInstance().cancelLogin();
                boolean autoReconnectCancelled = MODULE.get(AutoReconnect.class).cancelAutoReconnect();
                if (autoReconnectCancelled) {
                    c.getSource().getEmbed()
                        .title("AutoReconnect Cancelled");
                    return;
                }
                if (loginCancelled) {
                    c.getSource().getEmbed()
                            .title("Login Cancelled");
                    return;
                }
                c.getSource().getEmbed()
                        .title("Already Disconnected!");
            } else {
                try {
                    Proxy.getInstance().disconnect();
                    MODULE.get(AutoReconnect.class).cancelAutoReconnect();
                    c.getSource().setNoOutput(true);
                } catch (final Exception e) {
                    DISCORD_LOG.error("Failed to disconnect", e);
                    c.getSource().getEmbed()
                            .title("Proxy Failed to Disconnect");
                }
            }
        });
    }
}
