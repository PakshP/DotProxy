package com.dotproxy;

import com.dotproxy.mc.language.VanillaLanguage;
import com.dotproxy.mc.language.VanillaTextRenderer;
import com.dotproxy.module.impl.TranslationProbeResponder;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import com.viaversion.nbt.tag.StringTag;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VanillaTextRendererTest {
    private static final int PROTOCOL_1_21_4 = 769;
    private static final int PROTOCOL_26_2 = 776;

    private static final VanillaTextRenderer R_26_2 = new VanillaTextRenderer(VanillaLanguage.forProtocol(PROTOCOL_26_2));
    private static final VanillaTextRenderer R_1_21_4 = new VanillaTextRenderer(VanillaLanguage.forProtocol(PROTOCOL_1_21_4));

    private static String render(VanillaTextRenderer r, String json) {
        return r.render(gson().deserialize(json));
    }

    @Test
    public void versionSelection() {
        assertEquals("1.21.4", VanillaLanguage.forProtocol(PROTOCOL_1_21_4).versionId());
        assertEquals("26.2", VanillaLanguage.forProtocol(PROTOCOL_26_2).versionId());
        assertEquals("1.21.4", VanillaLanguage.forProtocol(47).versionId()); // older than our data -> oldest
        assertEquals("1.21.8", VanillaLanguage.forProtocol(772).versionId()); // shared protocol -> newest of them
    }

    @Test
    public void translatesVanillaKeys() {
        assertEquals("Stone", render(R_26_2, "{\"translate\":\"block.minecraft.stone\"}"));
        assertEquals("<Steve> hi", render(R_26_2, "{\"translate\":\"chat.type.text\",\"with\":[\"Steve\",\"hi\"]}"));
    }

    @Test
    public void unknownKeysRenderRaw() {
        assertEquals("key.meteor-client.open-gui", render(R_26_2, "{\"translate\":\"key.meteor-client.open-gui\"}"));
        assertEquals("fb", render(R_26_2, "{\"translate\":\"some.mod.key\",\"fallback\":\"fb\"}"));
        // vanilla keys win over the fallback
        assertEquals("Stone", render(R_26_2, "{\"translate\":\"block.minecraft.stone\",\"fallback\":\"fb\"}"));
    }

    @Test
    public void formatting() {
        assertEquals("b a", render(R_26_2, "{\"translate\":\"%2$s %1$s\",\"with\":[\"a\",\"b\"]}"));
        assertEquals("100%", render(R_26_2, "{\"translate\":\"%s%%\",\"with\":[\"100\"]}"));
        // bad index / stray percent / unknown conversion -> the template literally
        assertEquals("%3$s", render(R_26_2, "{\"translate\":\"%3$s\",\"with\":[\"a\"]}"));
        assertEquals("%s", render(R_26_2, "{\"translate\":\"%s\"}"));
        assertEquals("50% off", render(R_26_2, "{\"translate\":\"50% off\"}"));
        assertEquals("%d", render(R_26_2, "{\"translate\":\"%d\",\"with\":[\"1\"]}"));
        // nested args
        assertEquals("<Stone> x", render(R_26_2,
            "{\"translate\":\"chat.type.text\",\"with\":[{\"translate\":\"block.minecraft.stone\"},\"x\"]}"));
    }

    @Test
    public void keybinds() {
        assertEquals("Space", render(R_26_2, "{\"keybind\":\"key.jump\"}"));
        assertEquals("W", render(R_26_2, "{\"keybind\":\"key.forward\"}"));
        assertEquals("Left Button", render(R_26_2, "{\"keybind\":\"key.attack\"}"));
        assertEquals("Not Bound", render(R_26_2, "{\"keybind\":\"key.smoothCamera\"}"));
        // mod key mapping -> translatable(name) -> raw
        assertEquals("key.meteor-client.open-gui", render(R_26_2, "{\"keybind\":\"key.meteor-client.open-gui\"}"));
    }

    @Test
    public void versionGating() {
        // the quick actions key mapping was added in 1.21.6
        assertEquals("G", render(R_26_2, "{\"keybind\":\"key.quickActions\"}"));
        assertEquals("key.quickActions", render(R_1_21_4, "{\"keybind\":\"key.quickActions\"}"));
    }

    @Test
    public void overrides() {
        var snapshot = VanillaLanguage.forProtocol(PROTOCOL_26_2);
        var r = new VanillaTextRenderer(snapshot.lang(), snapshot.keybinds(),
            Map.of("key.meteor-client.open-gui", "Open GUI"),
            Map.of("key.jump", "J", "key.meteor-client.open-gui", "Right Shift"));
        assertEquals("Open GUI", r.render(Component.translatable("key.meteor-client.open-gui")));
        assertEquals("J", r.render(Component.keybind("key.jump")));
        assertEquals("Right Shift", r.render(Component.keybind("key.meteor-client.open-gui")));
    }

    @Test
    public void childrenAndUnresolvedContents() {
        assertEquals("a Stone b", render(R_26_2,
            "{\"text\":\"a \",\"extra\":[{\"translate\":\"block.minecraft.stone\"},\" b\"]}"));
        assertEquals("", render(R_26_2, "{\"score\":{\"name\":\"@p\",\"objective\":\"x\"}}"));
        assertEquals("", render(R_26_2, "{\"selector\":\"@p\"}"));
    }

    @Test
    public void signLines() {
        var messages = new ListTag<>(StringTag.class);
        messages.add(new StringTag("{\"translate\":\"key.meteor-client.open-gui\"}"));
        messages.add(new StringTag("{\"keybind\":\"key.jump\"}"));
        messages.add(new StringTag("{\"translate\":\"block.minecraft.stone\"}"));
        messages.add(new StringTag("\"\""));
        var front = new CompoundTag();
        front.put("messages", messages);
        var root = new CompoundTag();
        root.put("front_text", front);

        assertArrayEquals(new String[]{"key.meteor-client.open-gui", "Space", "Stone", ""},
            TranslationProbeResponder.renderSignLines(root, true, R_26_2));
        // back side not present -> empty sign text
        assertArrayEquals(new String[]{"", "", "", ""}, TranslationProbeResponder.renderSignLines(root, false, R_26_2));
        // no nbt at all (freshly placed sign)
        assertArrayEquals(new String[]{"", "", "", ""}, TranslationProbeResponder.renderSignLines(null, true, R_26_2));
    }

    @Test
    public void nbtComponentSignLines() {
        var line = new CompoundTag();
        line.put("translate", new StringTag("block.minecraft.stone"));
        var messages = new ListTag<>(CompoundTag.class);
        messages.add(line);
        var front = new CompoundTag();
        front.put("messages", messages);
        var root = new CompoundTag();
        root.put("front_text", front);
        assertEquals("Stone", TranslationProbeResponder.renderSignLines(root, true, R_26_2)[0]);
    }

    @Test
    public void filterText() {
        assertEquals("abc", VanillaTextRenderer.filterText("a§b\nc\u007f"));
    }
}
