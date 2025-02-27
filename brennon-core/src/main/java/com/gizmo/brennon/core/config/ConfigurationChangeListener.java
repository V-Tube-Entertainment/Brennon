package com.gizmo.brennon.core.config;

@FunctionalInterface
public interface ConfigurationChangeListener {
    void onConfigurationChanged(Object newConfig);
}
