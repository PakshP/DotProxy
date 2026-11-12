package com.dotproxy.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.dotproxy.command.api.Command;
import com.dotproxy.command.api.CommandCategory;
import com.dotproxy.command.api.CommandContext;
import com.dotproxy.command.api.CommandUsage;
import com.dotproxy.discord.Embed;
import com.dotproxy.module.impl.AntiKick;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.dotproxy.Globals.CONFIG;
import static com.dotproxy.Globals.MODULE;
import static com.dotproxy.command.brigadier.ToggleArgumentType.getToggle;
import static com.dotproxy.command.brigadier.ToggleArgumentType.toggle;

public class AntiKickCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("antiKick")
            .category(CommandCategory.MODULE)
            .description("""
            AntiKick kicks players controlling the proxy client if they are inactive for a set amount of time.
            
            Inactivity is defined as not moving or swinging.
            """)
            .usageLines(
                "on/off",
                "playerInactivityKickMins <minutes>",
                "minWalkDistance <blocks>"
            )
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("antiKick")
            .then(argument("toggle", toggle()).executes(c -> {
                CONFIG.client.extra.antiKick.enabled = getToggle(c, "toggle");
                MODULE.get(AntiKick.class).syncEnabledFromConfig();
                c.getSource().getEmbed()
                    .title("AntiKick " + toggleStrCaps(CONFIG.client.extra.antiKick.enabled));
                return OK;
            }))
            .then(literal("playerInactivityKickMins")
                      .then(argument("minutes", integer(1, 30)).executes(c -> {
                          CONFIG.client.extra.antiKick.playerInactivityKickMins = getInteger(c, "minutes");
                          c.getSource().getEmbed()
                              .title("Inactivity Time Set!");
                          return OK;
                      })))
            .then(literal("minWalkDistance")
                      .then(argument("blocks", integer(1, 100)).executes(c -> {
                          CONFIG.client.extra.antiKick.minWalkDistance = getInteger(c, "blocks");
                          c.getSource().getEmbed()
                              .title("Min Walk Distance Set!");
                          return OK;
                      })));
    }

    @Override
    public void defaultEmbed(final Embed builder) {
        builder
            .primaryColor()
            .addField("Enabled", toggleStrCaps(CONFIG.client.extra.antiKick.enabled), false)
            .addField("Player Inactivity Minutes", CONFIG.client.extra.antiKick.playerInactivityKickMins, false)
            .addField("Min Walk Distance", CONFIG.client.extra.antiKick.minWalkDistance, false);
    }
}
