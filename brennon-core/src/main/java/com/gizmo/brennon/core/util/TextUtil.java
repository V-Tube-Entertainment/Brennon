package com.gizmo.brennon.core.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class TextUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final GsonComponentSerializer GSON_SERIALIZER = GsonComponentSerializer.gson();

    public static Component parse(String input) {
        return MINI_MESSAGE.deserialize(input);
    }

    public static String serialize(Component component) {
        return MINI_MESSAGE.serialize(component);
    }

    public static String toJson(Component component) {
        return GSON_SERIALIZER.serialize(component);
    }

    public static Component fromJson(String json) {
        return GSON_SERIALIZER.deserialize(json);
    }

    public static Component colorize(String text) {
        return parse(text.replace('&', '§'));
    }
}
