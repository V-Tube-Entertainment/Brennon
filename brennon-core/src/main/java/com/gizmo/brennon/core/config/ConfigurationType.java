package com.gizmo.brennon.core.config;

public enum ConfigurationType {
    YAML(".yml"),
    JSON(".json");

    private final String extension;

    ConfigurationType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
