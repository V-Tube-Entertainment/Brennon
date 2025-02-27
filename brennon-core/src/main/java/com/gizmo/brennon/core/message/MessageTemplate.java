package com.gizmo.brennon.core.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;

public class MessageTemplate {
    private final String template;
    private final MiniMessage miniMessage;

    public MessageTemplate(String template) {
        this.template = template;
        this.miniMessage = MiniMessage.miniMessage();
    }

    public Component format(Map<String, String> placeholders) {
        String processed = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            processed = processed.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return miniMessage.deserialize(processed);
    }

    public Component format(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("Must provide key-value pairs");
        }

        Map<String, String> placeholders = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            placeholders.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }

        return format(placeholders);
    }

    public static class Builder {
        private StringBuilder template;

        public Builder() {
            this.template = new StringBuilder();
        }

        public Builder line(String text) {
            if (template.length() > 0) {
                template.append("\n");
            }
            template.append(text);
            return this;
        }

        public MessageTemplate build() {
            return new MessageTemplate(template.toString());
        }
    }
}
