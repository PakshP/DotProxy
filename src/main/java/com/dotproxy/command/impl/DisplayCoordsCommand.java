package com.dotproxy.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.dotproxy.command.api.Command;
import com.dotproxy.command.api.CommandCategory;
import com.dotproxy.command.api.CommandContext;
import com.dotproxy.command.api.CommandUsage;

import static com.dotproxy.Globals.CONFIG;
import static com.dotproxy.command.brigadier.ToggleArgumentType.getToggle;
import static com.dotproxy.command.brigadier.ToggleArgumentType.toggle;

public class DisplayCoordsCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("displayCoords")
            .category(CommandCategory.MANAGE)
            .description("""
            Configures whether the discord bot's messages should display coordinates. Only usable by account owner(s).
            """)
            .usageLines("on/off")
            .aliases("coords")
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("displayCoords").requires(Command::validateAccountOwner)
            .then(argument("toggle", toggle()).executes(c -> {
                CONFIG.discord.reportCoords = getToggle(c, "toggle");
                c.getSource().getEmbed()
                    .title("Coordinates " + toggleStrCaps(CONFIG.discord.reportCoords))
                    .primaryColor();
                return OK;
            }));
    }
}
