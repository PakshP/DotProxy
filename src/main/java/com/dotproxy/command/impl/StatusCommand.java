package com.dotproxy.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.dotproxy.Proxy;
import com.dotproxy.cache.data.entity.Entity;
import com.dotproxy.command.api.Command;
import com.dotproxy.command.api.CommandCategory;
import com.dotproxy.command.api.CommandContext;
import com.dotproxy.command.api.CommandUsage;
import com.dotproxy.module.impl.*;
import com.dotproxy.network.server.ServerSession;
import com.dotproxy.util.ImageInfo;
import com.dotproxy.util.math.MathHelper;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.dotproxy.Globals.*;
import static com.dotproxy.discord.DiscordBot.escape;
import static java.util.Objects.nonNull;

public class StatusCommand extends Command {
    @Override
    public CommandUsage commandUsage() {
        return CommandUsage.builder()
            .name("status")
            .category(CommandCategory.CORE)
            .description("""
            Prints the current status of DotProxy, the in-game player, and modules.
            """)
            .usageLines(
                "",
                "modules"
            )
            .aliases("s")
            .build();
    }

    public static String getCoordinates(final Entity entity) {
        if (CONFIG.discord.reportCoords) {
            return "||[%d, %d, %d]||".formatted(
                MathHelper.floorI(entity.getX()),
                MathHelper.floorI(entity.getY()),
                MathHelper.floorI(entity.getZ())
            );
        } else {
            return "Coords disabled";
        }
    }

    private String getCurrentClientUserName() {
        ServerSession currentConnection = Proxy.getInstance().getCurrentPlayer().get();
        if (nonNull(currentConnection)) {
            return "%s [%dms]".formatted(
                escape(currentConnection.getName()),
                currentConnection.getPing()
            );
        } else {
            return "None";
        }
    }

    private List<String> getSpectatorUserNames() {
        return Proxy.getInstance().getSpectatorConnections().stream()
                .map(connection -> "%s [%dms]".formatted(
                    escape(connection.getName()),
                    connection.getPing())
                )
                .collect(Collectors.toList());
    }

    private String getStatus() {
        if (Proxy.getInstance().isConnected()) {
            return "Online";
        } else {
            return "Disconnected";
        }
    }

    public String getOnlineTime() {
        return Proxy.getInstance().isConnected()
            ? Proxy.getInstance().getOnlineTimeString()
            : "Offline";
    }

    @Override
    public LiteralArgumentBuilder<CommandContext> register() {
        return command("status")
            .then(literal("modules").executes(c -> {
                c.getSource().getEmbed()
                    .title("DotProxy " + VERSION + " - " + CONFIG.authentication.username)
                    .color(Proxy.getInstance().isConnected()
                               ? CONFIG.theme.success.discord()
                               : CONFIG.theme.error.discord())
                    .thumbnail(getThumbnailImage())
                    .addField("Plugins", ImageInfo.inImageCode() ? "N/A (`java` required)" : toggleStr(CONFIG.plugins.enabled), true)
                    .addField("AutoDisconnect", toggleStr(MODULE.get(AutoDisconnect.class).isEnabled()), true)
                    .addField("AutoReconnect", toggleStr(MODULE.get(AutoReconnect.class).isEnabled()), true)
                    .addField("AntiAFK", toggleStr(MODULE.get(AntiAFK.class).isEnabled()), true)
                    .addField("AutoRespawn", toggleStr(MODULE.get(AutoRespawn.class).isEnabled()), true)
                    .addField("ViaVersion", "Z->S: " + toggleStr(CONFIG.client.viaversion.enabled)
                        + "\nP->Z: " + toggleStr(CONFIG.server.viaversion.enabled), true)
                    .addField("VisualRange", toggleStr(MODULE.get(VisualRange.class).isEnabled()), true)
                    .addField("AntiKick", toggleStr(MODULE.get(AntiKick.class).isEnabled()), true)
                    .addField("Active Hours", toggleStr(MODULE.get(ActiveHours.class).isEnabled()), true)
                    .addField("ActionLimiter", toggleStr(MODULE.get(ActionLimiter.class).isEnabled()), true)
                    .addField("ChatHistory", toggleStr(MODULE.get(ChatHistory.class).isEnabled()), true);
            }))
            .executes(c -> {
                final var embed = c.getSource().getEmbed();
                embed
                    .title("DotProxy " + VERSION + " - " + CONFIG.authentication.username)
                    .color(Proxy.getInstance().isConnected()
                               ? CONFIG.theme.success.discord()
                               : CONFIG.theme.error.discord())
                    .thumbnail(getThumbnailImage())
                    .addField("Status", getStatus())
                    //end row 1
                    .addField("Uptime", MathHelper.formatDuration(Duration.ofMillis(System.currentTimeMillis() - Proxy.getInstance().getStartTime())), true)
                    .addField("Online Duration", getOnlineTime(), true)
                    .addField("Ping", (Proxy.getInstance().isConnected() ? Proxy.getInstance().getClient().getPing() : 0) + "ms", true)
                    // end row 2
                    .addField("Health",  (CACHE.getPlayerCache().getThePlayer().getHealth()), true)
                    .addField("Dimension",
                              (nonNull(CACHE.getChunkCache().getCurrentDimension()) ? CACHE.getChunkCache().getCurrentDimension().name(): "None"),
                              true)
                    .addField("TPS", "%s [%s]".formatted(TPS.getTPS(), CLIENT_TPS.getTPS()), true)
                    // end row 3
                    .addField("Proxy IP", escape(CONFIG.server.getProxyAddress()), true)
                    .addField("Server", escape(CONFIG.client.server.address + ':' + CONFIG.client.server.port), true)
                    .addField("Connected Player", getCurrentClientUserName(), true);
                    // end row 4
                if (!getSpectatorUserNames().isEmpty())
                    embed.addField("Spectators", String.join(", ", getSpectatorUserNames()), true);
                if (CONFIG.discord.reportCoords)
                    embed.addField("Coordinates", getCoordinates(CACHE.getPlayerCache().getThePlayer()), true);
                 return OK;
            });
    }

    private static @Nullable String getThumbnailImage() {
        return "Unknown".equals(CONFIG.authentication.username)
            ? null
            : Proxy.getInstance().getPlayerHeadURL(CONFIG.authentication.username).toString();
    }
}
