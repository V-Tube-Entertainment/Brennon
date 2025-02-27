package com.gizmo.brennon.core.server;

import com.gizmo.brennon.core.message.MessageTemplate;
import com.google.inject.Inject;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.UUID;

public class KickService {
    private static final String KICK_CHANNEL = "brennon:kick";

    private final Logger logger;
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;
    private MessageTemplate kickTemplate;

    @Inject
    public KickService(Logger logger, RedisClient redisClient) {
        this.logger = logger;
        this.redisClient = redisClient;
        this.connection = redisClient.connect();

        // Create default kick template
        this.kickTemplate = new MessageTemplate.Builder()
                .line("<red>You have been kicked!")
                .line("<gray>By: <white>{issuer}")
                .line("<gray>Reason: <white>{reason}")
                .build();
    }

    /**
     * Kicks a player from the network.
     *
     * @param targetId The UUID of the player to kick
     * @param reason The reason for the kick
     * @param issuerName The name of the person who issued the kick
     * @return true if the kick message was sent successfully, false otherwise
     */
    public boolean kickPlayer(UUID targetId, String reason, String issuerName) {
        try {
            // Create kick message using template
            String kickMessage = formatKickMessage(reason, issuerName);

            // Publish kick message
            String message = String.format("%s:%s", targetId.toString(), kickMessage);
            Long receivers = connection.sync().publish(KICK_CHANNEL, message);

            // Return true if at least one server received the message
            return receivers != null && receivers > 0;
        } catch (Exception e) {
            logger.error("Failed to kick player {} ({})", targetId, reason, e);
            return false;
        }
    }

    /**
     * Creates a formatted kick message using the template.
     */
    private String formatKickMessage(String reason, String issuerName) {
        return kickTemplate.format(
                "issuer", issuerName,
                "reason", reason
        ).toString();
    }

    /**
     * Sets a custom kick template.
     *
     * @param template The new template to use for kick messages
     */
    public void setKickTemplate(MessageTemplate template) {
        if (template != null) {
            this.kickTemplate = template;
        }
    }

    /**
     * Clean up resources when the service is shutting down.
     */
    public void shutdown() {
        if (connection != null) {
            connection.close();
        }
    }
}