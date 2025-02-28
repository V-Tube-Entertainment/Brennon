package com.gizmo.brennon.core.punishment.appeal.search;

import com.gizmo.brennon.core.punishment.appeal.AppealStatus;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public record AppealSearchOptions(
        String query,
        Set<AppealStatus> statuses,
        Instant startDate,
        Instant endDate,
        UUID handlerId,
        UUID appealerId,
        int page,
        int pageSize
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String query = "";
        private Set<AppealStatus> statuses = EnumSet.allOf(AppealStatus.class);
        private Instant startDate = null;
        private Instant endDate = null;
        private UUID handlerId = null;
        private UUID appealerId = null;
        private int page = 1;
        private int pageSize = 10;

        public Builder query(String query) {
            this.query = query != null ? query : "";
            return this;
        }

        public Builder statuses(Set<AppealStatus> statuses) {
            this.statuses = statuses != null ? EnumSet.copyOf(statuses) : EnumSet.allOf(AppealStatus.class);
            return this;
        }

        public Builder dateRange(Instant start, Instant end) {
            this.startDate = start;
            this.endDate = end;
            return this;
        }

        public Builder handler(UUID handlerId) {
            this.handlerId = handlerId;
            return this;
        }

        public Builder appealer(UUID appealerId) {
            this.appealerId = appealerId;
            return this;
        }

        public Builder pagination(int page, int pageSize) {
            this.page = Math.max(1, page);
            this.pageSize = Math.max(1, Math.min(pageSize, 100));
            return this;
        }

        public AppealSearchOptions build() {
            return new AppealSearchOptions(
                    query,
                    statuses,
                    startDate,
                    endDate,
                    handlerId,
                    appealerId,
                    page,
                    pageSize
            );
        }
    }
}
