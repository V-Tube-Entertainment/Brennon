package com.gizmo.brennon.velocity.config;

import java.util.List;
import java.util.ArrayList;

/**
 * Configuration class for the Velocity platform
 *
 * @author Gizmo0320
 * @since 2025-03-01 03:09:19
 */
public class Config {
    private String serverName = "Brennon Network";
    private String motd = "Welcome to Brennon Network!";
    private String maintenanceMotd = "Server is currently under maintenance";
    private boolean maintenance = false;
    private List<String> maintenanceWhitelist = new ArrayList<>();
    private int maxPlayers = 1000;
    private int maxPlayersPerServer = 100;
    private boolean filterEnabled = true;
    private List<String> filteredWords = new ArrayList<>();
    private String globalChatFormat = "[%server%] %player%: %message%";

    // Getters and setters
    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public String getMaintenanceMotd() {
        return maintenanceMotd;
    }

    public void setMaintenanceMotd(String maintenanceMotd) {
        this.maintenanceMotd = maintenanceMotd;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    public List<String> getMaintenanceWhitelist() {
        return maintenanceWhitelist;
    }

    public void setMaintenanceWhitelist(List<String> maintenanceWhitelist) {
        this.maintenanceWhitelist = maintenanceWhitelist;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public int getMaxPlayersPerServer() {
        return maxPlayersPerServer;
    }

    public void setMaxPlayersPerServer(int maxPlayersPerServer) {
        this.maxPlayersPerServer = maxPlayersPerServer;
    }

    public boolean isFilterEnabled() {
        return filterEnabled;
    }

    public void setFilterEnabled(boolean filterEnabled) {
        this.filterEnabled = filterEnabled;
    }

    public List<String> getFilteredWords() {
        return filteredWords;
    }

    public void setFilteredWords(List<String> filteredWords) {
        this.filteredWords = filteredWords;
    }

    public String getGlobalChatFormat() {
        return globalChatFormat;
    }

    public void setGlobalChatFormat(String globalChatFormat) {
        this.globalChatFormat = globalChatFormat;
    }
}