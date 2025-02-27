package com.gizmo.brennon.core.monitoring;

import com.google.gson.JsonObject;
import java.time.Instant;

public record MonitoringAlert(
        String serverId,
        AlertType type,
        AlertSeverity severity,
        String message,
        Instant timestamp
) {
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("serverId", serverId);
        json.addProperty("type", type.name());
        json.addProperty("severity", severity.name());
        json.addProperty("message", message);
        json.addProperty("timestamp", timestamp.toString());
        return json;
    }
}
