package com.gizmo.brennon.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Utility class for text manipulation and formatting.
 */
public class TextUtil {
    private static final char COLOR_CHAR = '\u00A7'; // § character
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character(COLOR_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().build();

    /**
     * Converts a string with legacy color codes (&) to a Component.
     *
     * @param text The text to parse
     * @return The parsed Component
     */
    public static Component parseLegacy(String text) {
        if (text == null) return Component.empty();
        return LEGACY_SERIALIZER.deserialize(text.replace('&', COLOR_CHAR));
    }

    /**
     * Converts a string with MiniMessage format to a Component.
     *
     * @param text The text to parse
     * @return The parsed Component
     */
    public static Component parse(String text) {
        if (text == null) return Component.empty();
        return MINI_MESSAGE.deserialize(text);
    }

    /**
     * Converts a Component to a string with legacy color codes.
     *
     * @param component The component to serialize
     * @return The serialized string
     */
    public static String toLegacy(Component component) {
        if (component == null) return "";
        return LEGACY_SERIALIZER.serialize(component);
    }

    /**
     * Converts a Component to a string with MiniMessage format.
     *
     * @param component The component to serialize
     * @return The serialized string
     */
    public static String serialize(Component component) {
        if (component == null) return "";
        return MINI_MESSAGE.serialize(component);
    }

    /**
     * Strips all formatting codes from a string.
     *
     * @param text The text to strip
     * @return The stripped text
     */
    public static String stripColor(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)[&" + COLOR_CHAR + "][0-9A-FK-ORX]", "");
    }

    /**
     * Colorizes a string by replacing the & character with the § character.
     *
     * @param text The text to colorize
     * @return The colorized text
     */
    public static String colorize(String text) {
        if (text == null) return "";
        return text.replace('&', COLOR_CHAR);
    }

    /**
     * Replaces the § character with the & character.
     *
     * @param text The text to decolorize
     * @return The decolorized text
     */
    public static String decolorize(String text) {
        if (text == null) return "";
        return text.replace(COLOR_CHAR, '&');
    }

    /**
     * Checks if a string contains color codes.
     *
     * @param text The text to check
     * @return True if the text contains color codes
     */
    public static boolean hasColor(String text) {
        if (text == null) return false;
        return text.contains(String.valueOf(COLOR_CHAR)) || text.contains("&");
    }
}
