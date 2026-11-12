package com.dotproxy.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.dotproxy.command.api.Command;
import com.dotproxy.command.api.CommandCategory;
import com.dotproxy.command.api.CommandContext;
import com.dotproxy.command.api.CommandUsage;
import com.dotproxy.discord.Embed;
import com.dotproxy.util.config.ConfigColor;

import java.util.Arrays;

import static com.dotproxy.Globals.CONFIG;
import static com.dotproxy.command.brigadier.CustomStringArgumentType.getString;
import static com.dotproxy.discord.DiscordBot.escape;

public class ThemeCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("theme")
            .category(CommandCategory.MANAGE)
            .description("""
            Changes the color theme of alerts and messages.

            Use `theme list` to see available colors.

            Where Colors Are Used:
              * Primary: Most embeds and command responses if not an error.
              * Success: General "this worked" responses, server join, and friends
              * Error: Error responses, server leave, and enemies
            """)
            .usageLines(
                "list",
                "primary <color>",
                "success <color>",
                "error <color>"
            )
            .aliases("color")
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("theme")
            .then(literal("list").executes(c -> {
                var allColors = Arrays.stream(ConfigColor.values())
                    .map(color -> escape(color.name().toLowerCase()))
                    .toList();
                c.getSource().getEmbed()
                    .title("Available Colors")
                    .description(String.join("\n", allColors));
            }))
            .then(literal("primary").then(argument("color", enumStrings(ConfigColor.values())).executes(c -> {
                var colorStr = getString(c, "color").toUpperCase();
                try {
                    CONFIG.theme.primary = ConfigColor.valueOf(colorStr);
                    c.getSource().getEmbed()
                        .title("Primary Color Set!");
                    return OK;
                } catch (final Throwable e) {
                    c.getSource().getEmbed()
                        .title("Invalid Color!")
                        .addField("Help", "Use `theme list` to see available colors");
                    return ERROR;
                }
            })))
            .then(literal("success").then(argument("color", enumStrings(ConfigColor.values())).executes(c -> {
                var colorStr = getString(c, "color").toUpperCase();
                try {
                    CONFIG.theme.success = ConfigColor.valueOf(colorStr);
                    c.getSource().getEmbed()
                        .title("Success Color Set!");
                    return OK;
                } catch (final Throwable e) {
                    c.getSource().getEmbed()
                        .title("Invalid Color!")
                        .addField("Help", "Use `theme list` to see available colors");
                    return ERROR;
                }
            })))
            .then(literal("error").then(argument("color", enumStrings(ConfigColor.values())).executes(c -> {
                var colorStr = getString(c, "color").toUpperCase();
                try {
                    CONFIG.theme.error = ConfigColor.valueOf(colorStr);
                    c.getSource().getEmbed()
                        .title("Error Color Set!");
                    return OK;
                } catch (final Throwable e) {
                    c.getSource().getEmbed()
                        .title("Invalid Color!")
                        .addField("Help", "Use `theme list` to see available colors");
                    return ERROR;
                }
            })));
    }

    @Override
    public void defaultEmbed(Embed embed) {
        embed
            .primaryColor()
            .addField("Primary", escape(CONFIG.theme.primary.name().toLowerCase()))
            .addField("Success", escape(CONFIG.theme.success.name().toLowerCase()))
            .addField("Error", escape(CONFIG.theme.error.name().toLowerCase()));
    }
}
