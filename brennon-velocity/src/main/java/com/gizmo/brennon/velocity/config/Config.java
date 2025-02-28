package com.gizmo.brennon.velocity.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {
    private String motd;
    private boolean maintenance;
    private String maintenanceMotd;
    private List<String> maintenanceWhitelist;
    private Map<String, ServerConfig> servers;
    private ChatConfig chat;
    private boolean debugMode;

    public Config() {
        this.motd = "§6Brennon Network";
        this.maintenance = false;
        this.maintenanceMotd = "§cServer is currently under maintenance";
        this.maintenanceWhitelist = new ArrayList<>();
        this.servers = new HashMap<>();
        this.chat = new ChatConfig();
        this.debugMode = false;
    }

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    public String getMaintenanceMotd() {
        return maintenanceMotd;
    }

    public void setMaintenanceMotd(String maintenanceMotd) {
        this.maintenanceMotd = maintenanceMotd;
    }

    public List<String> getMaintenanceWhitelist() {
        return maintenanceWhitelist;
    }

    public void setMaintenanceWhitelist(List<String> maintenanceWhitelist) {
        this.maintenanceWhitelist = maintenanceWhitelist;
    }

    public Map<String, ServerConfig> getServers() {
        return servers;
    }

    public void setServers(Map<String, ServerConfig> servers) {
        this.servers = servers;
    }

    public ChatConfig getChat() {
        return chat;
    }

    public void setChat(ChatConfig chat) {
        this.chat = chat;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public static class ServerConfig {
        private String address;
        private int port;
        private boolean restricted;
        private List<String> allowedGroups;

        public ServerConfig() {
            this.address = "localhost";
            this.port = 25565;
            this.restricted = false;
            this.allowedGroups = new ArrayList<>();
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isRestricted() {
            return restricted;
        }

        public void setRestricted(boolean restricted) {
            this.restricted = restricted;
        }

        public List<String> getAllowedGroups() {
            return allowedGroups;
        }

        public void setAllowedGroups(List<String> allowedGroups) {
            this.allowedGroups = allowedGroups;
        }
    }

    public static class ChatConfig {
        private boolean globalChat;
        private String globalChatFormat;
        private String staffChatFormat;
        private List<String> filteredWords;
        private boolean filterEnabled;

        public ChatConfig() {
            this.globalChat = true;
            this.globalChatFormat = "§7[%server%] %prefix%%player%%suffix%: %message%";
            this.staffChatFormat = "§c[STAFF] %player%: %message%";
            this.filteredWords = new ArrayList<>();
            this.filterEnabled = true;
        }

        public boolean isGlobalChat() {
            return globalChat;
        }

        public void setGlobalChat(boolean globalChat) {
            this.globalChat = globalChat;
        }

        public String getGlobalChatFormat() {
            return globalChatFormat;
        }

        public void setGlobalChatFormat(String globalChatFormat) {
            this.globalChatFormat = globalChatFormat;
        }

        public String getStaffChatFormat() {
            return staffChatFormat;
        }

        public void setStaffChatFormat(String staffChatFormat) {
            this.staffChatFormat = staffChatFormat;
        }

        public List<String> getFilteredWords() {
            return filteredWords;
        }

        public void setFilteredWords(List<String> filteredWords) {
            this.filteredWords = filteredWords;
        }

        public boolean isFilterEnabled() {
            return filterEnabled;
        }

        public void setFilterEnabled(boolean filterEnabled) {
            this.filterEnabled = filterEnabled;
        }
    }
}