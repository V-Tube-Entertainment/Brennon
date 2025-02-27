package com.gizmo.brennon.core.config;

import java.util.*;

public class Configuration {
    private final Map<String, Object> data;

    public Configuration(Map<String, Object> data) {
        this.data = new HashMap<>(data);
    }

    /**
     * Gets a value from the configuration
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String path, T defaultValue) {
        Object value = get(path);
        if (value != null && defaultValue.getClass().isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }

    /**
     * Gets a raw value from the configuration
     */
    public Object get(String path) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = data;

        for (int i = 0; i < parts.length - 1; i++) {
            Object value = current.get(parts[i]);
            if (!(value instanceof Map)) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            current = map;
        }

        return current.get(parts[parts.length - 1]);
    }

    /**
     * Sets a value in the configuration
     */
    public void set(String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = data;

        for (int i = 0; i < parts.length - 1; i++) {
            current.computeIfAbsent(parts[i], k -> new HashMap<String, Object>());
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) current.get(parts[i]);
            current = map;
        }

        if (value == null) {
            current.remove(parts[parts.length - 1]);
        } else {
            current.put(parts[parts.length - 1], value);
        }
    }

    /**
     * Gets all configuration values
     */
    public Map<String, Object> getAll() {
        return new HashMap<>(data);
    }

    /**
     * Merges default values into the configuration
     */
    public void merge(Map<String, Object> defaults) {
        mergeMap(data, defaults);
    }

    @SuppressWarnings("unchecked")
    private void mergeMap(Map<String, Object> target, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                if (!target.containsKey(key)) {
                    target.put(key, new HashMap<>());
                }
                mergeMap(
                        (Map<String, Object>) target.get(key),
                        (Map<String, Object>) value
                );
            } else if (!target.containsKey(key)) {
                target.put(key, value);
            }
        }
    }
}
