package com.dotproxy.discord;

import com.dotproxy.Proxy;
import com.dotproxy.event.client.*;
import com.dotproxy.event.discord.NotificationSentEvent;
import com.dotproxy.event.module.*;
import com.dotproxy.event.player.*;
import com.dotproxy.event.plugin.PluginLoadFailureEvent;
import com.dotproxy.event.plugin.PluginLoadedEvent;
import com.dotproxy.feature.player.World;
import com.dotproxy.util.ChatUtil;
import com.dotproxy.util.DisconnectReasonInfo;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import static com.github.rfresh2.EventConsumer.of;
import static com.dotproxy.Globals.*;
import static com.dotproxy.command.impl.StatusCommand.getCoordinates;
import static com.dotproxy.discord.DiscordBot.*;
import static com.dotproxy.util.math.MathHelper.formatDuration;
import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;

public class NotificationEventListener {
    public static final NotificationEventListener INSTANCE = new NotificationEventListener();

    public void subscribeEvents() {
        EVENT_BUS.subscribe(
            this,
            of(ClientConnectEvent.class, this::handleConnectEvent),
            of(ClientOnlineEvent.class, this::handlePlayerOnlineEvent),
            of(ClientConfigurationEvent.Entering.class, this::handleClientConfigurationEnteringEvent),
            of(ClientDisconnectEvent.class, this::handleDisconnectEvent),
            of(ClientDeathEvent.class, this::handleDeathEvent),
            of(HealthAutoDisconnectEvent.class, this::handleHealthAutoDisconnectEvent),
            of(PlayerConnectedEvent.class, this::handleProxyClientConnectedEvent),
            of(SpectatorConnectedEvent.class, this::handleProxySpectatorConnectedEvent),
            of(PlayerDisconnectedEvent.class, this::handleProxyClientDisconnectedEvent),
            of(VisualRangeEnterEvent.class, this::handleVisualRangeEnterEvent),
            of(VisualRangeLeaveEvent.class, this::handleVisualRangeLeaveEvent),
            of(VisualRangeLogoutEvent.class, this::handleVisualRangeLogoutEvent),
            of(NonWhitelistedPlayerConnectedEvent.class, this::handleNonWhitelistedPlayerConnectedEvent),
            of(BlacklistedPlayerConnectedEvent.class, this::handleBlacklistedPlayerConnectedEvent),
            of(SpectatorDisconnectedEvent.class, this::handleProxySpectatorDisconnectedEvent),
            of(ActiveHoursConnectEvent.class, this::handleActiveHoursConnectEvent),
            of(ClientLoginFailedEvent.class, this::handleProxyLoginFailedEvent),
            of(ClientStartConnectEvent.class, this::handleStartConnectEvent),
            of(AutoReconnectEvent.class, this::handleAutoReconnectEvent),
            of(MsaDeviceCodeLoginEvent.class, this::handleMsaDeviceCodeLoginEvent),
            of(PluginLoadFailureEvent.class, this::handlePluginLoadFailure),
            of(PluginLoadedEvent.class, this::handlePluginLoadedEvent)
        );
    }

    public static String notificationMention() {
        return mentionRole(
            CONFIG.discord.notificationMentionRoleId.isEmpty()
                ? CONFIG.discord.accountOwnerRoleId
                : CONFIG.discord.notificationMentionRoleId
        );
    }

    public void handleConnectEvent(ClientConnectEvent event) {
        var embed = Embed.builder()
            .title("Connected")
            .pendingColor()
            .addField("Server", CONFIG.client.server.address, true)
            .addField("Proxy IP", CONFIG.server.getProxyAddress(), false);
        if (CONFIG.discord.mentionRoleOnConnect) {
            sendEmbedMessage(notificationMention(), embed);
        } else {
            sendEmbedMessage(embed);
        }
        updatePresence();
    }

    public void handlePlayerOnlineEvent(ClientOnlineEvent event) {
        var embedBuilder = Embed.builder()
            .title("Online")
            .successColor();
        if (CONFIG.discord.mentionRoleOnPlayerOnline) {
            sendEmbedMessage(notificationMention(), embedBuilder);
        } else {
            sendEmbedMessage(embedBuilder);
        }
    }

    public void handleClientConfigurationEnteringEvent(ClientConfigurationEvent.Entering event) {
        if (!CONFIG.client.extra.reconfiguringNotification) return;
        var embedBuilder = Embed.builder()
            .title("Reconfiguring...")
            .pendingColor();
        sendEmbedMessage(embedBuilder);
    }

    public void handleDisconnectEvent(ClientDisconnectEvent event) {
        var category = DisconnectReasonInfo.getDisconnectCategory(event.reason());
        var embed = Embed.builder()
            .title("Disconnected")
            .addField("Reason", event.reason(), false)
            .addField("Category", category.toString(), false)
            .addField("Online Duration", formatDuration(event.onlineDuration()), false)
            .errorColor();
        if (CONFIG.discord.mentionRoleOnDisconnect) {
            sendEmbedMessage(notificationMention(), embed);
        } else {
            sendEmbedMessage(embed);
        }
        EXECUTOR.execute(this::updatePresence);
    }

    public void handleDeathEvent(ClientDeathEvent event) {
        var embed = Embed.builder()
            .title("Player Death")
            .errorColor()
            .addField("Coordinates", getCoordinates(CACHE.getPlayerCache().getThePlayer()), false)
            .addField("Dimension", World.getCurrentDimension().name(), false);
        if (CONFIG.discord.mentionRoleOnDeath) {
            sendEmbedMessage(notificationMention(), embed);
        } else {
            sendEmbedMessage(embed);
        }
    }

    public void handleHealthAutoDisconnectEvent(HealthAutoDisconnectEvent event) {
        var embed = Embed.builder()
            .title("Health AutoDisconnect Triggered")
            .addField("Health", CACHE.getPlayerCache().getThePlayer().getHealth(), true)
            .primaryColor();
        if (CONFIG.client.extra.utility.actions.autoDisconnect.mentionOnDisconnect) {
            sendEmbedMessage(notificationMention(), embed);
        } else {
            sendEmbedMessage(embed);
        }
    }

    public void handleProxyClientConnectedEvent(PlayerConnectedEvent event) {
        if (!CONFIG.discord.clientConnectionMessages) return;
        var embed = Embed.builder()
            .title("Client Connected")
            .addField("Username", escape(event.clientGameProfile().getName()), false)
            .addField("MC Version", event.session().getMCVersion(), false)
            .thumbnail(Proxy.getInstance().getPlayerBodyURL(event.clientGameProfile().getId()).toString())
            .primaryColor();
        if (CONFIG.discord.mentionOnClientConnected) {
            sendEmbedMessage(notificationMention(), embed);
        } else {
            sendEmbedMessage(embed);
        }
    }

    public void handleProxySpectatorConnectedEvent(SpectatorConnectedEvent event) {
        if (!CONFIG.discord.clientConnectionMessages) return;
        var embed = Embed.builder()
            .title("Spectator Connected")
            .addField("Username", escape(event.clientGameProfile().getName()), false)
            .addField("MC Version", event.session().getMCVersion(), false)
            .thumbnail(Proxy.getInstance().getPlayerBodyURL(event.clientGameProfile().getId()).toString())
            .primaryColor();
        if (CONFIG.discord.mentionOnSpectatorConnected) {
            sendEmbedMessage(notificationMention(), embed);
        } else {
            sendEmbedMessage(embed);
        }
    }

    public void handleProxyClientDisconnectedEvent(PlayerDisconnectedEvent event) {
        if (!CONFIG.discord.clientConnectionMessages) return;
        var embed = Embed.builder()
            .title("Client Disconnected")
            .errorColor();
        if (nonNull(event.clientGameProfile())) {
            embed = embed.addField("Username", escape(event.clientGameProfile().getName()), false);
        }
        if (nonNull(event.reason())) {
            embed = embed.addField("Reason", escape(event.reason()), false);
        }
        if (CONFIG.discord.mentionOnClientDisconnected) {
            sendEmbedMessage(notificationMention(), embed);
        } else {
            sendEmbedMessage(embed);
        }
    }

    public void handleVisualRangeEnterEvent(VisualRangeEnterEvent event) {
        var embedCreateSpec = Embed.builder()
            .title("Player In Visual Range")
            .color(event.isFriend() ? CONFIG.theme.success.color() : CONFIG.theme.error.color())
            .addField("Player Name", escape(event.playerEntry().getName()), true)
            .addField("Player UUID", ("[" + event.playerEntry().getProfileId() + "](https://namemc.com/profile/" + event.playerEntry().getProfileId() + ")"), true)
            .thumbnail(Proxy.getInstance().getPlayerBodyURL(event.playerEntry().getProfileId()).toString());

        if (CONFIG.discord.reportCoords) {
            embedCreateSpec.addField("Coordinates", "||["
                + (int) event.playerEntity().getX() + ", "
                + (int) event.playerEntity().getY() + ", "
                + (int) event.playerEntity().getZ()
                + "]||", false);
        }
        final String buttonId = "addFriend" + ThreadLocalRandom.current().nextInt(1000000);
        final List<Button> buttons = asList(Button.primary(buttonId, "Add Friend"));
        final Consumer<ButtonInteractionEvent> mapper = e -> {
            if (e.getComponentId().equals(buttonId)) {
                DISCORD_LOG.info("{} added friend: {} [{}]",
                    Optional.ofNullable(e.getInteraction().getMember())
                        .map(m -> m.getUser().getName())
                        .orElse("Unknown"),
                    event.playerEntry().getName(),
                    event.playerEntry().getProfileId());
                PLAYER_LISTS.getFriendsList().add(event.playerEntry().getName());
                e.replyEmbeds(Embed.builder()
                        .title("Friend Added")
                        .successColor()
                        .addField("Player Name", escape(event.playerEntry().getName()), true)
                        .addField("Player UUID", ("[" + event.playerEntry().getProfileId() + "](https://namemc.com/profile/" + event.playerEntry().getProfileId() + ")"), true)
                        .thumbnail(Proxy.getInstance().getPlayerBodyURL(event.playerEntry().getProfileId()).toString())
                        .toJDAEmbed())
                    .complete();
                saveConfigAsync();
            }
        };
        if (CONFIG.client.extra.visualRange.enterAlertMention)
            if (!event.isFriend())
                sendEmbedMessageWithButtons(notificationMention(), embedCreateSpec, buttons, mapper, Duration.ofHours(1));
            else
                sendEmbedMessage(embedCreateSpec);
        else
        if (!event.isFriend())
            sendEmbedMessageWithButtons(embedCreateSpec, buttons, mapper, Duration.ofHours(1));
        else
            sendEmbedMessage(embedCreateSpec);
    }

    public void handleVisualRangeLeaveEvent(final VisualRangeLeaveEvent event) {
        var embedCreateSpec = Embed.builder()
            .title("Player Left Visual Range")
            .color(event.isFriend() ? CONFIG.theme.success.color() : CONFIG.theme.error.color())
            .addField("Player Name", escape(event.playerEntry().getName()), true)
            .addField("Player UUID", ("[" + event.playerEntity().getUuid() + "](https://namemc.com/profile/" + event.playerEntry().getProfileId() + ")"), true)
            .thumbnail(Proxy.getInstance().getPlayerBodyURL(event.playerEntity().getUuid()).toString());

        if (CONFIG.discord.reportCoords) {
            embedCreateSpec.addField("Coordinates", "||["
                + (int) event.playerEntity().getX() + ", "
                + (int) event.playerEntity().getY() + ", "
                + (int) event.playerEntity().getZ()
                + "]||", false);
        }
        sendEmbedMessage(embedCreateSpec);
    }

    public void handleVisualRangeLogoutEvent(final VisualRangeLogoutEvent event) {
        var embedCreateSpec = Embed.builder()
            .title("Player Logout In Visual Range")
            .color(event.isFriend() ? CONFIG.theme.success.color() : CONFIG.theme.error.color())
            .addField("Player Name", escape(event.playerEntry().getName()), true)
            .addField("Player UUID", ("[" + event.playerEntity().getUuid() + "](https://namemc.com/profile/" + event.playerEntry().getProfileId() + ")"), true)
            .thumbnail(Proxy.getInstance().getPlayerBodyURL(event.playerEntity().getUuid()).toString());

        if (CONFIG.discord.reportCoords) {
            embedCreateSpec.addField("Coordinates", "||["
                + (int) event.playerEntity().getX() + ", "
                + (int) event.playerEntity().getY() + ", "
                + (int) event.playerEntity().getZ()
                + "]||", false);
        }
        sendEmbedMessage(embedCreateSpec);
    }

    public void handleNonWhitelistedPlayerConnectedEvent(NonWhitelistedPlayerConnectedEvent event) {
        var embed = Embed.builder()
            .title("Non-Whitelisted Login Blocked")
            .errorColor();
        if (nonNull(event.remoteAddress()) && CONFIG.discord.showNonWhitelistLoginIP) {
            embed = embed.addField("IP", escape(event.remoteAddress().toString()), false);
        }
        if (nonNull(event.gameProfile()) && nonNull(event.gameProfile().getId()) && nonNull(event.gameProfile().getName())) {
            embed
                .addField("Username", escape(event.gameProfile().getName()), false)
                .addField("Player UUID", ("[" + event.gameProfile().getId().toString() + "](https://namemc.com/profile/" + event.gameProfile().getId().toString() + ")"), true)
                .thumbnail(Proxy.getInstance().getPlayerBodyURL(event.gameProfile().getId()).toString());
            final String buttonId = "whitelist" + ThreadLocalRandom.current().nextInt(10000000);
            final List<Button> buttons = asList(Button.primary(buttonId, "Whitelist Player"));
            final Consumer<ButtonInteractionEvent> mapper = e -> {
                if (e.getComponentId().equals(buttonId)) {
                    if (validateButtonInteractionEventFromAccountOwner(e)) {
                        DISCORD_LOG.info("{} whitelisted {} [{}]",
                            Optional.ofNullable(e.getInteraction().getMember()).map(m -> m.getUser().getName()).orElse("Unknown"),
                            event.gameProfile().getName(),
                            event.gameProfile().getId().toString());
                        PLAYER_LISTS.getWhitelist().add(event.gameProfile().getName());
                        e.replyEmbeds(Embed.builder()
                            .title("Player Whitelisted")
                            .successColor()
                            .addField("Player Name", escape(event.gameProfile().getName()), true)
                            .addField("Player UUID", ("[" + event.gameProfile().getId().toString() + "](https://namemc.com/profile/" + event.gameProfile().getId().toString() + ")"), true)
                            .thumbnail(Proxy.getInstance().getPlayerBodyURL(event.gameProfile().getId()).toString())
                            .toJDAEmbed()).complete();
                        saveConfigAsync();
                    } else {
                        DISCORD_LOG.error("{} attempted to whitelist {} [{}] but was not authorized to do so!",
                            Optional.ofNullable(e.getInteraction().getMember()).map(m -> m.getUser().getName()).orElse("Unknown"),
                            event.gameProfile().getName(),
                            event.gameProfile().getId().toString());
                        e.replyEmbeds(Embed.builder()
                            .title("Not Authorized!")
                            .errorColor()
                            .addField("Error",
                                "User: " + Optional.ofNullable(e.getInteraction().getMember()).map(m -> m.getUser().getName()).orElse("Unknown")
                                    + " is not authorized to execute this command! Contact the account owner", true)
                            .toJDAEmbed()).complete();
                    }
                }
            };
            sendEmbedMessageWithButtons(embed, buttons, mapper, Duration.ofHours(1L));
        } else { // shouldn't be possible if verifyUsers is enabled
            if (nonNull(event.gameProfile())) {
                embed
                    .addField("Username", escape(event.gameProfile().getName()), false);
            }
            if (CONFIG.discord.mentionOnNonWhitelistedClientConnected) {
                sendEmbedMessage(notificationMention(), embed);
            } else {
                sendEmbedMessage(embed);
            }
        }
    }

    public void handleBlacklistedPlayerConnectedEvent(BlacklistedPlayerConnectedEvent event) {
        var embed = Embed.builder()
            .title("Blacklisted Player Disconnected")
            .errorColor();
        if (nonNull(event.remoteAddress()) && CONFIG.discord.showNonWhitelistLoginIP) {
            embed = embed.addField("IP", escape(event.remoteAddress().toString()), false);
        }
        if (nonNull(event.gameProfile()) && nonNull(event.gameProfile().getId()) && nonNull(event.gameProfile().getName())) {
            embed
                .addField("Username", escape(event.gameProfile().getName()), false)
                .addField("Player UUID", ("[" + event.gameProfile().getId().toString() + "](https://namemc.com/profile/" + event.gameProfile().getId().toString() + ")"), true)
                .thumbnail(Proxy.getInstance().getPlayerBodyURL(event.gameProfile().getId()).toString());
        }
        if (CONFIG.discord.mentionOnNonWhitelistedClientConnected) {
            sendEmbedMessage(notificationMention(), embed);
        } else {
            sendEmbedMessage(embed);
        }
    }

    public void handleProxySpectatorDisconnectedEvent(SpectatorDisconnectedEvent event) {
        if (!CONFIG.discord.clientConnectionMessages) return;
        var embed = Embed.builder()
            .title("Spectator Disconnected")
            .errorColor();
        if (nonNull(event.clientGameProfile())) {
            embed = embed.addField("Username", escape(event.clientGameProfile().getName()), false);
        }
        if (CONFIG.discord.mentionOnSpectatorDisconnected) {
            sendEmbedMessage(notificationMention(), embed);
        } else {
            sendEmbedMessage(embed);
        }
    }

    public void handleActiveHoursConnectEvent(ActiveHoursConnectEvent event) {
        var embed = Embed.builder()
            .title("Active Hours Connect Triggered")
            .primaryColor();
        sendEmbedMessage(embed);
    }

    public void handleProxyLoginFailedEvent(ClientLoginFailedEvent event) {
        var description = """
        [Help]
        Try waiting and connecting again.

        If that fails, log into the account with the vanilla MC launcher and join a server. Then try again with DotProxy.

        Another possible cause is your microsoft account needing to have a password (re)set. Usually only possible if you are using email codes to log in instead of passwords.
        """;
        if (event.exception() != null) {
            description = "Error: " + ChatUtil.constrainChatMessageSize(event.exception().getMessage(), true) + "\n\n" + description;
        }
        var embed = Embed.builder()
            .title("Login Failed")
            .description(description)
            .errorColor();
        if (CONFIG.discord.mentionRoleOnLoginFailed) {
            sendEmbedMessage(notificationMention(), embed);
        } else {
            sendEmbedMessage(embed);
        }
    }

    public void handleStartConnectEvent(ClientStartConnectEvent event) {
        sendEmbedMessage(Embed.builder()
            .title("Connecting...")
            .pendingColor());
    }

    public void handleAutoReconnectEvent(final AutoReconnectEvent event) {
        sendEmbedMessage(Embed.builder()
            .title("AutoReconnecting in " + event.delaySeconds() + "s")
            .pendingColor());
    }

    public void handleMsaDeviceCodeLoginEvent(final MsaDeviceCodeLoginEvent event) {
        final var embed = Embed.builder()
            .title("Microsoft Device Code Login")
            .primaryColor()
            .description("Login Here: " + event.deviceCode().getDirectVerificationUri() + " \nCode: " + event.deviceCode().getUserCode());
        if (CONFIG.discord.mentionRoleOnDeviceCodeAuth)
            sendEmbedMessage(notificationMention(), embed);
        else
            sendEmbedMessage(embed);
    }

    public void handlePluginLoadFailure(PluginLoadFailureEvent event) {
        String id = event.id() != null ? event.id() : "?";
        var embed = Embed.builder()
            .title("Plugin Load Failure")
            .errorColor()
            .description("Error: " + escape(event.message()))
            .addField("Plugin ID", escape(id), false)
            .addField("Plugin Jar", escape(event.jarPath().getFileName().toString()), false);
        sendEmbedMessage(embed);
    }

    public void handlePluginLoadedEvent(PluginLoadedEvent event) {
        var embed = Embed.builder()
            .title("Plugin Loaded")
            .successColor()
            .addField("ID", escape(event.pluginInfo().id()), false)
            .addField("Description", escape(event.pluginInfo().description()))
            .addField("Version", escape(event.pluginInfo().version().toString()), false)
            .addField("URL", escape(event.pluginInfo().url()), false)
            .addField("Author(s)", String.join(", ", event.pluginInfo().authors()), false)
            .addField("Mixins", event.pluginInfo().mixins().isEmpty() ? "no" : "yes");
        sendEmbedMessage(embed);
    }

    /**
     * Convenience proxy methods
     */
    public void sendEmbedMessage(Embed embed) {
        DISCORD.sendEmbedMessage(embed);
        EVENT_BUS.postAsync(new NotificationSentEvent(embed, null));
    }
    public void sendEmbedMessage(String message, Embed embed) {
        DISCORD.sendEmbedMessage(message, embed);
        EVENT_BUS.postAsync(new NotificationSentEvent(embed, message));
    }
    public void sendMessage(final String message) {
        DISCORD.sendMessage(message);
        EVENT_BUS.postAsync(new NotificationSentEvent(null, message));
    }
    public void sendEmbedMessageWithButtons(String message, Embed embed, List<Button> buttons, Consumer<ButtonInteractionEvent> mapper, Duration timeout) {
        DISCORD.sendEmbedMessageWithButtons(message, embed, buttons, mapper, timeout);
        EVENT_BUS.postAsync(new NotificationSentEvent(embed, message));
    }
    public void sendEmbedMessageWithButtons(Embed embed, List<Button> buttons, Consumer<ButtonInteractionEvent> mapper, Duration timeout) {
        DISCORD.sendEmbedMessageWithButtons(embed, buttons, mapper, timeout);
        EVENT_BUS.postAsync(new NotificationSentEvent(embed, null));
    }
    public void updatePresence(final OnlineStatus onlineStatus, final Activity activity) {
        DISCORD.setPresence(onlineStatus, activity);
    }
    public void updatePresence() {
        DISCORD.tickPresence();
    }
}
