package com.dotproxy;

import com.dotproxy.util.ComponentSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class TranslatableTextParserTest {

    @Test
    public void translatableTextComponentParseTest() {
        Logger blah = Globals.CLIENT_LOG; // init in shared static block
        final String chatText = "{\"translate\":\"chat.type.text\",\"with\":[{\"text\":\"bonk_player\"},{\"text\":\"you should never talk about that with them\"}]}";
        Component deserialize = ComponentSerializer.deserialize(chatText);
        assertInstanceOf(TranslatableComponent.class, deserialize);
        String serialize = ComponentSerializer.serializePlain(deserialize);
        assertEquals("<bonk_player> you should never talk about that with them", serialize);
    }

    @Test
    public void translatableTextRenderingTest() {
        var json = "{\"translate\":\"%s\",\"with\":[{\"extra\":[\"<\",{\"color\":\"dark_red\",\"text\":\"Steve\"},\"> hello\"],\"text\":\"\"}]}";
        String expectedText = "<Steve> hello";
        var component = ComponentSerializer.deserialize(json);

        String rendered = ComponentSerializer.serializePlain(component);

        assertEquals(expectedText, rendered);
    }
}
