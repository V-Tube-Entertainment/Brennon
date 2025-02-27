package com.gizmo.brennon.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.regex.Pattern;

public class ColorUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static Component colorize(String message) {
        if (message == null || message.isEmpty()) return Component.empty();
        return MINI_MESSAGE.deserialize(message);
    }

    public static TextComponent gradient(String message, String fromHex, String toHex) {
        if (message == null || message.isEmpty()) return Component.empty();

        TextColor from = TextColor.fromHexString(fromHex);
        TextColor to = TextColor.fromHexString(toHex);

        TextComponent.Builder builder = Component.text();
        int length = message.length();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);
            TextColor color = interpolateColor(from, to, ratio);
            builder.append(Component.text(message.charAt(i)).color(color));
        }

        return builder.build();
    }

    public static TextComponent rainbow(String message, float saturation, float brightness) {
        if (message == null || message.isEmpty()) return Component.empty();

        TextComponent.Builder builder = Component.text();
        int length = message.length();

        for (int i = 0; i < length; i++) {
            float hue = (float) i / length;
            int rgb = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
            TextColor color = TextColor.color(rgb);
            builder.append(Component.text(message.charAt(i)).color(color));
        }

        return builder.build();
    }

    private static TextColor interpolateColor(TextColor color1, TextColor color2, float ratio) {
        int r = interpolate(color1.red(), color2.red(), ratio);
        int g = interpolate(color1.green(), color2.green(), ratio);
        int b = interpolate(color1.blue(), color2.blue(), ratio);
        return TextColor.color(r, g, b);
    }

    private static int interpolate(int start, int end, float ratio) {
        return (int) (start * (1 - ratio) + end * ratio);
    }

    public static TextColor fromHex(String hex) {
        if (!hex.startsWith("#")) {
            hex = "#" + hex;
        }
        return TextColor.fromHexString(hex);
    }

    public static TextColor darken(TextColor color, float factor) {
        return TextColor.color(
                Math.max((int)(color.red() * factor), 0),
                Math.max((int)(color.green() * factor), 0),
                Math.max((int)(color.blue() * factor), 0)
        );
    }

    public static TextColor lighten(TextColor color, float factor) {
        return TextColor.color(
                Math.min((int)(color.red() + (255 - color.red()) * factor), 255),
                Math.min((int)(color.green() + (255 - color.green()) * factor), 255),
                Math.min((int)(color.blue() + (255 - color.blue()) * factor), 255)
        );
    }
}
