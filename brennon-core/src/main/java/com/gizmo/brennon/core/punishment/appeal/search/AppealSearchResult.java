package com.gizmo.brennon.core.punishment.appeal.search;

import com.gizmo.brennon.core.punishment.appeal.Appeal;

import java.util.List;

public record AppealSearchResult(
        List<Appeal> appeals,
        int totalResults,
        int currentPage,
        int totalPages
) {
    public boolean hasNextPage() {
        return currentPage < totalPages;
    }

    public boolean hasPreviousPage() {
        return currentPage > 1;
    }
}
