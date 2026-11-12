package com.dotproxy.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.dotproxy.Proxy;
import com.dotproxy.command.api.Command;
import com.dotproxy.command.api.CommandCategory;
import com.dotproxy.command.api.CommandContext;
import com.dotproxy.command.api.CommandUsage;
import com.dotproxy.module.impl.AutoReconnect;

import static com.dotproxy.Globals.EXECUTOR;
import static com.dotproxy.Globals.MODULE;
import static com.dotproxy.util.DisconnectMessages.SYSTEM_DISCONNECT;

public class ReconnectCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("reconnect")
            .category(CommandCategory.MANAGE)
            .description("""
            Disconnect and reconnects from the destination MC server.
            """)
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("reconnect").executes(c -> {
            EXECUTOR.execute(() -> {
                Proxy.getInstance().disconnect(SYSTEM_DISCONNECT);
                MODULE.get(AutoReconnect.class).cancelAutoReconnect();
                MODULE.get(AutoReconnect.class).scheduleAutoReconnect(2);
            });
        });
    }
}
