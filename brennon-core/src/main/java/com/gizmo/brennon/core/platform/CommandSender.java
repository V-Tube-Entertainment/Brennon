package com.gizmo.brennon.core.platform;

import net.kyori.adventure.text.Component;
import java.util.UUID;

public interface CommandSender {
    String getName();
    UUID getUniqueId();
    void sendMessage(String message);
    void sendMessage(Component message);
    boolean hasPermission(String permission);
    boolean isConsole();
}
