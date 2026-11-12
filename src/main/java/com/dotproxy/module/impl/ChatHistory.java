package com.dotproxy.module.impl;

import com.github.rfresh2.EventConsumer;
import com.dotproxy.event.chat.PublicChatEvent;
import com.dotproxy.event.chat.SystemChatEvent;
import com.dotproxy.event.chat.WhisperChatEvent;
import com.dotproxy.event.client.ClientDisconnectEvent;
import com.dotproxy.event.player.PlayerLoginEvent;
import com.dotproxy.event.player.SpectatorLoggedInEvent;
import com.dotproxy.module.api.Module;
import com.dotproxy.util.struct.CircularFifoQueue;
import lombok.Locked;
import net.kyori.adventure.text.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Queue;

import static com.github.rfresh2.EventConsumer.of;
import static com.dotproxy.Globals.CONFIG;

public class ChatHistory extends Module {
    private Queue<StoredChat> chatHistory = new CircularFifoQueue<>(CONFIG.server.extra.chatHistory.maxCount);

    @Override
    public List<EventConsumer<?>> registerEvents() {
        return List.of(
            of(PublicChatEvent.class, this::handlePublicChat),
            of(WhisperChatEvent.class, this::handleWhisperChat),
            of(SystemChatEvent.class, this::handleSystemChat),
            of(PlayerLoginEvent.Post.class, this::handleClientLoggedIn),
            of(SpectatorLoggedInEvent.class, this::handleSpectatorLoggedIn),
            of(ClientDisconnectEvent.class, this::handleDisconnect)
        );
    }

    @Locked
    @Override
    public void onDisable() {
        chatHistory.clear();
    }

    @Override
    public boolean enabledSetting() {
        return CONFIG.server.extra.chatHistory.enable;
    }


    @Locked
    private void handleSystemChat(SystemChatEvent event) {
        chatHistory.add(new StoredChat(event.component(), Instant.now()));
    }

    @Locked
    private void handleWhisperChat(WhisperChatEvent event) {
        chatHistory.add(new StoredChat(event.component(), Instant.now()));
    }

    @Locked
    private void handlePublicChat(PublicChatEvent event) {
        chatHistory.add(new StoredChat(event.component(), Instant.now()));
    }

    @Locked
    private void handleClientLoggedIn(PlayerLoginEvent.Post event) {
        removeOldChats();
        var session = event.session();
        chatHistory.forEach(chat -> session.sendAsyncMessage(chat.message()));
    }

    @Locked
    private void handleSpectatorLoggedIn(SpectatorLoggedInEvent event) {
        if (!CONFIG.server.extra.chatHistory.spectators) return;
        removeOldChats();
        var session = event.session();
        chatHistory.forEach(chat -> session.sendAsyncMessage(chat.message()));
    }

    @Locked
    private void handleDisconnect(ClientDisconnectEvent event) {
        chatHistory.clear();
    }

    @Locked
    private void removeOldChats() {
        while (checkChatTime(chatHistory.peek())) chatHistory.poll();
    }

    // true if the chat is older than the configured time, and should be removed
    private boolean checkChatTime(StoredChat storedChat) {
        if (storedChat == null) return false;
        return storedChat.time().isBefore(Instant.now().minus(CONFIG.server.extra.chatHistory.seconds, ChronoUnit.SECONDS));
    }

    @Locked
    public void syncMaxCountFromConfig() {
        final Queue<StoredChat> newChatHistory = new CircularFifoQueue<>(CONFIG.server.extra.chatHistory.maxCount);
        while (chatHistory.peek() != null) {
            newChatHistory.add(chatHistory.poll());
        }
        chatHistory = newChatHistory;
    }

    private record StoredChat(Component message, Instant time) { }

}
