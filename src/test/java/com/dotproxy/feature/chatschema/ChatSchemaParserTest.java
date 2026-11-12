package com.dotproxy.feature.chatschema;

import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.dotproxy.Globals.CACHE;
import static org.junit.jupiter.api.Assertions.*;

public class ChatSchemaParserTest {
    PlayerListEntry SteveEntry = new PlayerListEntry(
        "Steve", UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7")
    );

    PlayerListEntry SteveBedrockEntry = new PlayerListEntry(
        ".Steve", UUID.fromString("00000000-0000-0000-0009-01f6c03c69e5")
    );

    PlayerListEntry arrfreshEntry = new PlayerListEntry(
        "aRRfresh", UUID.fromString("7cb31e32-65dc-494e-b812-3391670374c3")
    );

    ChatSchema schemaSimple = new ChatSchema(
        "<$s> $m",
        "$s whispers: $m",
        "to $r: $m"
    );
    ChatSchema schemaEssentials = new ChatSchema(
        "<$s> $m",
        "[$s -> me] $m",
        "[me -> $r] $m"
    );

    @BeforeEach
    public void setUp() {
        CACHE.getTabListCache().add(SteveEntry);
        CACHE.getTabListCache().add(arrfreshEntry);
        CACHE.getTabListCache().add(SteveBedrockEntry);
        CACHE.getProfileCache().setProfile(new GameProfile(SteveEntry.getProfileId(), SteveEntry.getName()));
    }

    @AfterEach
    public void tearDown() {
        CACHE.getTabListCache().remove(SteveEntry);
        CACHE.getTabListCache().remove(arrfreshEntry);
        CACHE.getProfileCache().setProfile(null);
    }

    @Test
    public void testSimplePublicChat() {
        String input = "<Steve> what's up?";
        var result = ChatSchemaParser.parse(input, schemaSimple);
        assertNotNull(result);
        assertEquals(ChatType.PUBLIC_CHAT, result.type());
        assertEquals(SteveEntry, result.sender());
        assertNull(result.receiver());
        assertEquals("what's up?", result.messageContent());
    }

    @Test
    public void testSimpleBedrockPublicChat() {
        String input = "<.Steve> what's up?";
        var result = ChatSchemaParser.parse(input, schemaSimple);
        assertNotNull(result);
        assertEquals(ChatType.PUBLIC_CHAT, result.type());
        assertEquals(SteveBedrockEntry, result.sender());
        assertNull(result.receiver());
        assertEquals("what's up?", result.messageContent());
    }


    @Test
    public void testSimpleWhisperOutgoing() {
        String input = "to aRRfresh: what's up?";
        var result = ChatSchemaParser.parse(input, schemaSimple);
        assertNotNull(result);
        assertEquals(ChatType.WHISPER_OUTBOUND, result.type());
        assertEquals(SteveEntry, result.sender());
        assertEquals(arrfreshEntry, result.receiver());
        assertEquals("what's up?", result.messageContent());
    }

    @Test
    public void testSimpleWhisperInbound() {
        String input = "aRRfresh whispers: what's up?";
        var result = ChatSchemaParser.parse(input, schemaSimple);
        assertNotNull(result);
        assertEquals(ChatType.WHISPER_INBOUND, result.type());
        assertEquals(arrfreshEntry, result.sender());
        assertEquals(SteveEntry, result.receiver());
        assertEquals("what's up?", result.messageContent());
    }

    @Test
    public void testEssentialsWhisperOutgoing() {
        String input = "[me -> aRRfresh] what's up?";
        var result = ChatSchemaParser.parse(input, schemaEssentials);
        assertNotNull(result);
        assertEquals(ChatType.WHISPER_OUTBOUND, result.type());
        assertEquals(SteveEntry, result.sender());
        assertEquals(arrfreshEntry, result.receiver());
        assertEquals("what's up?", result.messageContent());
    }

    @Test
    public void testEssentialsWhisperInbound() {
        String input = "[aRRfresh -> me] what's up?";
        var result = ChatSchemaParser.parse(input, schemaEssentials);
        assertNotNull(result);
        assertEquals(ChatType.WHISPER_INBOUND, result.type());
        assertEquals(arrfreshEntry, result.sender());
        assertEquals(SteveEntry, result.receiver());
        assertEquals("what's up?", result.messageContent());
    }

    @Test
    public void testEssentialsWithGroupPublicChat() {
        String input = "[default] Steve: what's up?";
        var schema = new ChatSchema(
            "[$w] $s: $m",
            schemaEssentials.whisperInbound(),
            schemaEssentials.whisperOutbound()
        );
        var result = ChatSchemaParser.parse(input, schema);
        assertNotNull(result);
        assertEquals(ChatType.PUBLIC_CHAT, result.type());
        assertEquals(SteveEntry, result.sender());
        assertNull(result.receiver());
        assertEquals("what's up?", result.messageContent());
    }

    @Test
    public void testDoubleWordWildcardMatch() {
        String input = ">> [BOSS] [ADMIN] Steve : what's up?";
        var schema = new ChatSchema(
            ">> [$w] $s : $m",
            schemaEssentials.whisperInbound(),
            schemaEssentials.whisperOutbound()
        );
        var result = ChatSchemaParser.parse(input, schema);
        assertNotNull(result);
        assertEquals(ChatType.PUBLIC_CHAT, result.type());
        assertEquals(SteveEntry, result.sender());
        assertNull(result.receiver());
        assertEquals("what's up?", result.messageContent());
    }
}
