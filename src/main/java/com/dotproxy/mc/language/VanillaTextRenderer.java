package com.dotproxy.mc.language;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.KeybindComponent;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.dotproxy.Globals.CONFIG;

/**
 * Renders a component to a plain string exactly like a vanilla client's {@code Component.getString()}.
 *
 * Used to answer translation-key probes: servers send translatable / keybind text and read back what the client
 * resolved it to. A vanilla client resolves vanilla keys and leaves anything it doesn't know (e.g. mod keys) raw.
 *
 * Mirrors (verified against the 26.2 client):
 * <ul>
 *   <li>TranslatableContents.decompose: key -> lang, else fallback, else the key itself; java-style %s / %n$s / %%;
 *       any malformed template or bad argument index renders the template literally</li>
 *   <li>KeybindContents: a known key mapping renders its bound key's name, an unknown one renders as translatable(name)</li>
 *   <li>score / selector / nbt / object contents are not resolved client-side and render as nothing</li>
 * </ul>
 */
public final class VanillaTextRenderer {
    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");
    private static final int MAX_DEPTH = 256;

    private final Map<String, String> lang;
    private final Map<String, String> keybinds;
    private final Map<String, String> translationOverrides;
    private final Map<String, String> keybindOverrides;

    public VanillaTextRenderer(
        final Map<String, String> lang,
        final Map<String, String> keybinds,
        final Map<String, String> translationOverrides,
        final Map<String, String> keybindOverrides
    ) {
        this.lang = lang;
        this.keybinds = keybinds;
        this.translationOverrides = translationOverrides;
        this.keybindOverrides = keybindOverrides;
    }

    public VanillaTextRenderer(final VanillaLanguage.Snapshot snapshot) {
        this(snapshot.lang(), snapshot.keybinds(), Map.of(), Map.of());
    }

    /**
     * Renderer matching the vanilla client version we're presenting to the server, plus configured overrides.
     */
    public static VanillaTextRenderer forProtocol(final int protocol) {
        final var snapshot = VanillaLanguage.forProtocol(protocol);
        final var probes = CONFIG.client.extra.translationProbes;
        return new VanillaTextRenderer(snapshot.lang(), snapshot.keybinds(),
                                       Map.copyOf(probes.translationOverrides), Map.copyOf(probes.keybindOverrides));
    }

    public String render(final Component component) {
        final var sb = new StringBuilder();
        append(component, sb, 0);
        return sb.toString();
    }

    private void append(final Component component, final StringBuilder sb, final int depth) {
        if (depth > MAX_DEPTH) return;
        if (component instanceof TextComponent text) {
            sb.append(text.content());
        } else if (component instanceof TranslatableComponent translatable) {
            final String template = translate(translatable.key(), translatable.fallback());
            appendTemplate(template, translatable.arguments(), sb, depth);
        } else if (component instanceof KeybindComponent keybind) {
            final String name = keybind.keybind();
            final String bound = boundKeyName(name);
            if (bound != null) {
                sb.append(bound);
            } else {
                // unknown key mapping -> Component.translatable(name)
                appendTemplate(translate(name, null), List.of(), sb, depth);
            }
        }
        // score, selector, nbt, object: unresolved client-side, contribute nothing
        for (final Component child : component.children()) {
            append(child, sb, depth + 1);
        }
    }

    private String translate(final String key, final @Nullable String fallback) {
        final String override = translationOverrides.get(key);
        if (override != null) return override;
        final String value = lang.get(key);
        if (value != null) return value;
        return fallback != null ? fallback : key;
    }

    private @Nullable String boundKeyName(final String name) {
        final String override = keybindOverrides.get(name);
        if (override != null) return override;
        return keybinds.get(name);
    }

    private void appendTemplate(final String template, final List<TranslationArgument> args, final StringBuilder sb, final int depth) {
        final String decomposed = decompose(template, args, depth);
        sb.append(decomposed != null ? decomposed : template);
    }

    /**
     * @return the formatted text, or null if vanilla would reject the template and show it literally
     */
    private @Nullable String decompose(final String template, final List<TranslationArgument> args, final int depth) {
        final StringBuilder out = new StringBuilder();
        final Matcher matcher = FORMAT_PATTERN.matcher(template);
        int argIndex = 0;
        int pos = 0;
        try {
            while (matcher.find(pos)) {
                final int start = matcher.start();
                final int end = matcher.end();
                if (start > pos) {
                    final String text = template.substring(pos, start);
                    if (text.indexOf('%') != -1) return null;
                    out.append(text);
                }
                final String type = matcher.group(2);
                final String full = template.substring(start, end);
                if ("%".equals(type) && "%%".equals(full)) {
                    out.append('%');
                } else if ("s".equals(type)) {
                    final String explicit = matcher.group(1);
                    final int index = explicit != null ? Integer.parseInt(explicit) - 1 : argIndex++;
                    if (index < 0 || index >= args.size()) return null;
                    appendArgument(args.get(index), out, depth);
                } else {
                    return null;
                }
                pos = end;
            }
            if (pos < template.length()) {
                final String tail = template.substring(pos);
                if (tail.indexOf('%') != -1) return null;
                out.append(tail);
            }
        } catch (final IllegalArgumentException e) { // includes NumberFormatException, as vanilla catches
            return null;
        }
        return out.toString();
    }

    private void appendArgument(final TranslationArgument argument, final StringBuilder out, final int depth) {
        final Object value = argument.value();
        if (value instanceof Component c) {
            append(c, out, depth + 1);
        } else {
            out.append(value);
        }
    }

    /**
     * {@code StringUtil.filterText}: drops section signs, control chars and DEL.
     */
    public static String filterText(final String text) {
        final StringBuilder sb = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            final char c = text.charAt(i);
            if (c != 167 && c >= 32 && c != 127) sb.append(c);
        }
        return sb.toString();
    }
}
