package com.gizmo.brennon.velocity.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.gizmo.brennon.velocity.BrennonVelocity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class ConfigManager {
    private final BrennonVelocity plugin;
    private final File configFile;
    private final Gson gson;
    private Config config;

    public ConfigManager(BrennonVelocity plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataDirectory().toFile(), "config.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadConfig();
    }

    public void loadConfig() {
        try {
            if (!configFile.exists()) {
                config = new Config();
                saveConfig();
                return;
            }

            try (FileReader reader = new FileReader(configFile)) {
                config = gson.fromJson(reader, Config.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
            config = new Config();
        }
    }

    public void saveConfig() {
        try {
            if (!configFile.exists()) {
                Files.createDirectories(configFile.getParentFile().toPath());
                Files.createFile(configFile.toPath());
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(config, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Config getConfig() {
        return config;
    }
}
