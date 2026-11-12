package com.dotproxy.network.client.handler.incoming;

import com.dotproxy.event.chat.PublicChatEvent;
import com.dotproxy.event.chat.SystemChatEvent;
import com.dotproxy.event.chat.WhisperChatEvent;
import com.dotproxy.feature.chatschema.ChatSchemaParser;
import com.dotproxy.network.client.ClientSession;
import com.dotproxy.network.codec.ClientEventLoopPacketHandler;
import com.dotproxy.util.ComponentSerializer;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import static com.dotproxy.Globals.*;

public class SystemChatHandler implements ClientEventLoopPacketHandler<ClientboundSystemChatPacket, ClientSession> {
    @Override
    public boolean applyAsync(@NonNull ClientboundSystemChatPacket packet, @NonNull ClientSession session) {
        try {
            if (packet.isOverlay()) {
                return true; // skip action bar text
            }
            logSystemChat(packet);
            final Component component = packet.getContent();
            String messageString = ComponentSerializer.serializePlain(component);

            var chatParseResult = ChatSchemaParser.parse(messageString);
            if (chatParseResult != null) {
                switch (chatParseResult.type()) {
                    case PUBLIC_CHAT -> {
                        EVENT_BUS.postAsync(new PublicChatEvent(chatParseResult.sender(), component, chatParseResult.messageContent()));
                    }
                    case WHISPER_INBOUND -> {
                        EVENT_BUS.postAsync(new WhisperChatEvent(
                            false,
                            chatParseResult.sender(),
                            chatParseResult.receiver(),
                            component,
                            chatParseResult.messageContent()
                        ));
                    }
                    case WHISPER_OUTBOUND -> {
                        EVENT_BUS.postAsync(new WhisperChatEvent(
                            true,
                            chatParseResult.sender(),
                            chatParseResult.receiver(),
                            component,
                            chatParseResult.messageContent()
                        ));
                    }
                }
            } else {
                EVENT_BUS.postAsync(new SystemChatEvent(component, messageString));
            }
        } catch (final Exception e) {
            CLIENT_LOG.error("Caught exception in ChatHandler. Packet: {}", packet, e);
        }
        return true;
    }

    private static void logSystemChat(final @NotNull ClientboundSystemChatPacket packet) {
        if (!CONFIG.client.extra.logChatMessages) return;
        CHAT_LOG.info(packet.getContent());
    }
}
