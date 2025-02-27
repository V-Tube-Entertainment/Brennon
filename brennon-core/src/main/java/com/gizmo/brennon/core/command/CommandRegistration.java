package com.gizmo.brennon.core.command;

import java.lang.reflect.Method;

public record CommandRegistration(
        Command command,
        Object instance,
        Method method
) {
    public String getName() {
        return command.name();
    }

    public String[] getAliases() {
        return command.aliases();
    }

    public String getDescription() {
        return command.description();
    }

    public String getUsage() {
        return command.usage();
    }

    public String getPermission() {
        return command.permission();
    }
}