package com.gizmo.brennon.core.ticket;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record Ticket(
        long id,
        UUID creatorId,
        String creatorName,
        String title,
        TicketStatus status,
        Instant createdAt,
        List<TicketResponse> responses,
        UUID assignedToId,
        String assignedToName,
        String category
) {
    public Ticket {
        responses = new ArrayList<>(responses); // Make defensive copy
    }

    public boolean isAssigned() {
        return assignedToId != null;
    }
}
