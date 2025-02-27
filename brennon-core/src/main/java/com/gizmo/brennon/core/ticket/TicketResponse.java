package com.gizmo.brennon.core.ticket;

import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
        long id,
        long ticketId,
        UUID responderId,
        String responderName,
        String message,
        Instant createdAt,
        boolean isStaffResponse
) {}
