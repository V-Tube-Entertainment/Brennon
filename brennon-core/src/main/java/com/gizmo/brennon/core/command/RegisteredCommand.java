package com.gizmo.brennon.core.command;

import java.lang.reflect.Method;

record RegisteredCommand(
        String name,
        String description,
        String permission,
        String usage,
        boolean async,
        Object handler,
        Method method
) {}
