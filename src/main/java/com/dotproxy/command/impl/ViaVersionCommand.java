package com.dotproxy.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.dotproxy.command.api.Command;
import com.dotproxy.command.api.CommandCategory;
import com.dotproxy.command.api.CommandContext;
import com.dotproxy.command.api.CommandUsage;
import com.dotproxy.discord.Embed;
import com.dotproxy.via.DotProxyViaCommandSender;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.dotproxy.Globals.CONFIG;
import static com.dotproxy.command.brigadier.CustomStringArgumentType.wordWithChars;
import static com.dotproxy.command.brigadier.ToggleArgumentType.getToggle;
import static com.dotproxy.command.brigadier.ToggleArgumentType.toggle;

public class ViaVersionCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("via")
            .category(CommandCategory.MODULE)
            .description("""
             Configure the integrated ViaVersion module.

             `proxyToServer` -> DotProxy connecting to the MC server
             `playerToProxy` -> players connecting to DotProxy
             """)
            .usageLines(
                "proxyToServer on/off",
                "proxyToServer version auto",
                "proxyToServer version <MC version>",
                "playerToProxy on/off"
            )
            .build();
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("via")
            .then(literal("proxyToServer")
                .then(argument("toggle", toggle()).executes(c -> {
                    CONFIG.client.viaversion.enabled = getToggle(c, "toggle");
                    c.getSource().getEmbed()
                        .title("DotProxy To Server ViaVersion " + toggleStrCaps(CONFIG.client.viaversion.enabled));
                    return OK;
                }))
                .then(literal("autoConfig")
                    .then(argument("toggle", toggle()).executes(c -> {
                        CONFIG.client.viaversion.autoProtocolVersion = getToggle(c, "toggle");
                        c.getSource().getEmbed()
                            .title("DotProxy To Server ViaVersion AutoConfig " + toggleStrCaps(CONFIG.client.viaversion.autoProtocolVersion));
                        return OK;
                    })))
                .then(literal("version")
                    .then(argument("version", wordWithChars()).executes(c -> {
                        final String version = StringArgumentType.getString(c, "version");
                        if ("auto".equalsIgnoreCase(version)) {
                            CONFIG.client.viaversion.autoProtocolVersion = true;
                        } else {
                            ProtocolVersion closest = ProtocolVersion.getClosest(version);
                            if (closest == null) {
                                c.getSource().getEmbed()
                                    .title("Invalid Version!")
                                    .description("Please select a valid version. Example: 1.19.4")
                                    .errorColor();
                                return OK;
                            } else {
                                CONFIG.client.viaversion.protocolVersion = closest.getVersion();
                                CONFIG.client.viaversion.autoProtocolVersion = false;
                            }
                        }
                        c.getSource().getEmbed()
                            .title("DotProxy To Server ViaVersion Version Updated!");
                        return OK;
                    }))))
            .then(literal("playerToProxy")
                .then(argument("toggle", toggle()).executes(c -> {
                    CONFIG.server.viaversion.enabled = getToggle(c, "toggle");
                    c.getSource().getEmbed()
                        .title("Player To DotProxy ViaVersion " + toggleStrCaps(CONFIG.server.viaversion.enabled));
                    return OK;
                })))
            .then(literal("c").then(argument("args", greedyString()).executes(c -> {
                var sender = new DotProxyViaCommandSender(c.getSource());
                String[] args = StringArgumentType.getString(c, "args").split(" ");
                Via.getManager().getCommandHandler().onCommand(sender, args);
                c.getSource().setNoOutput(true);
            })));
    }

    @Override
    public void defaultEmbed(final Embed embedBuilder) {
        embedBuilder
            .addField("DotProxy To Server ViaVersion", toggleStr(CONFIG.client.viaversion.enabled), false)
            .addField("DotProxy To Server Version", CONFIG.client.viaversion.autoProtocolVersion
                ? "Auto (" + ProtocolVersion.getProtocol(CONFIG.client.viaversion.protocolVersion).getName() + ")"
                : ProtocolVersion.getProtocol(CONFIG.client.viaversion.protocolVersion).getName(), false)
            .addField("Player To DotProxy ViaVersion", toggleStr(CONFIG.server.viaversion.enabled), false)
            .primaryColor();
    }}
