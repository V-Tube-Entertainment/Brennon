package com.gizmo.brennon.core.logging;

import java.time.Instant;
import java.util.UUID;

public record LogQueryFilter(
        Instant from,
        Instant to,
        LogLevel level,
        String category,
        String serverId,
        UUID userId,
        int limit
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Instant from;
        private Instant to;
        private LogLevel level;
        private String category;
        private String serverId;
        private UUID userId;
        private int limit = 100;

        public Builder from(Instant from) {
            this.from = from;
            return this;
        }

        public Builder to(Instant to) {
            this.to = to;
            return this;
        }

        public Builder level(LogLevel level) {
            this.level = level;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder serverId(String serverId) {
            this.serverId = serverId;
            return this;
        }

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public LogQueryFilter build() {
            return new LogQueryFilter(from, to, level, category, serverId, userId, limit);
        }
    }
}
