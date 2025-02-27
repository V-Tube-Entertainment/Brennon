package com.gizmo.brennon.core.punishment.appeal;

import com.gizmo.brennon.core.database.DatabaseManager;
import com.gizmo.brennon.core.punishment.AppealStatus;
import com.gizmo.brennon.core.redis.RedisManager;
import org.slf4j.Logger;

import java.util.UUID;

public class AppealManager {
    private final DatabaseManager databaseManager;
    private final RedisManager redisManager;
    private final Logger logger;

    public Appeal createAppeal(long punishmentId, UUID appealerId,
                               String appealerName, String reason) {
        // TODO: Implement appeal creation
    }

    public void handleAppeal(long appealId, UUID handlerId,
                             String handlerName, AppealStatus status, String response) {
        // TODO: Implement appeal handling
    }

    public List<Appeal> getPendingAppeals() {
        // TODO: Implement pending appeals lookup
    }
}
