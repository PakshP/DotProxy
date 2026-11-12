package com.zenith.feature.extrachat;

import com.zenith.feature.chatschema.ChatSchemaParser;
import com.zenith.module.impl.ExtraChat;
import com.zenith.network.codec.PacketHandler;
import com.zenith.network.server.ServerSession;
import com.zenith.util.ComponentSerializer;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;

import static com.zenith.Globals.*;

public class ECSystemChatOutgoingHandler implements PacketHandler<ClientboundSystemChatPacket, ServerSession> {

    @Override
    public ClientboundSystemChatPacket apply(ClientboundSystemChatPacket packet, ServerSession session) {
        if (packet.isOverlay()) return packet;
        try {
            final Component component = packet.getContent();
            final String message = ComponentSerializer.serializePlain(component);

            var chatParseResult = ChatSchemaParser.parse(message);
            if (chatParseResult != null) {
                switch (chatParseResult.type()) {
                    case PUBLIC_CHAT -> {
                        if (CONFIG.client.extra.chat.hideChat) {
                            return null;
                        }
                        if (PLAYER_LISTS.getIgnoreList().contains(chatParseResult.sender().getProfileId())) {
                            return null;
                        }
                    }
                    case WHISPER_INBOUND -> {
                        if (CONFIG.client.extra.chat.hideWhispers) {
                            return null;
                        }
                        if (PLAYER_LISTS.getIgnoreList().contains(chatParseResult.sender().getProfileId())) {
                            return null;
                        }
                    }
                    // less confusing for users if we show outbound whispers imo
//                    case WHISPER_OUTBOUND -> {
//                        if (CONFIG.client.extra.chat.hideWhispers) {
//                            return null;
//                        }
//                    }
                }
            }
            if (CONFIG.client.extra.chat.insertClickableLinks && !packet.isOverlay()) {
                return new ClientboundSystemChatPacket(MODULE.get(ExtraChat.class).insertClickableLinks(packet.getContent()), packet.isOverlay());
            }
        } catch (final Exception e) {
            SERVER_LOG.error("Failed to parse chat message in ExtraChatSystemChatOutgoingHandler: {}",
                             ComponentSerializer.serializePlain(packet.getContent()),
                             e);
        }
        return packet;
    }
}
