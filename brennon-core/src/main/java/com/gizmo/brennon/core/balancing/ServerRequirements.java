package com.gizmo.brennon.core.balancing;

/**
 * Represents requirements for server selection
 *
 * @author Gizmo0320
 * @since 2025-03-04 00:30:27
 */
public class ServerRequirements {
    private Double minTps;
    private Integer maxPlayers;
    private Double maxMemory;
    private Long maxResponseTime;

    private ServerRequirements() {}

    public static Builder builder() {
        return new Builder();
    }

    public boolean hasMinTps() { return minTps != null; }
    public boolean hasMaxPlayers() { return maxPlayers != null; }
    public boolean hasMaxMemory() { return maxMemory != null; }
    public boolean hasMaxResponseTime() { return maxResponseTime != null; }

    public Double getMinTps() { return minTps; }
    public Integer getMaxPlayers() { return maxPlayers; }
    public Double getMaxMemory() { return maxMemory; }
    public Long getMaxResponseTime() { return maxResponseTime; }

    public static class Builder {
        private final ServerRequirements requirements;

        private Builder() {
            this.requirements = new ServerRequirements();
        }

        public Builder minTps(double tps) {
            requirements.minTps = tps;
            return this;
        }

        public Builder maxPlayers(int players) {
            requirements.maxPlayers = players;
            return this;
        }

        public Builder maxMemory(double memory) {
            requirements.maxMemory = memory;
            return this;
        }

        public Builder maxResponseTime(long time) {
            requirements.maxResponseTime = time;
            return this;
        }

        public ServerRequirements build() {
            return requirements;
        }
    }
}
