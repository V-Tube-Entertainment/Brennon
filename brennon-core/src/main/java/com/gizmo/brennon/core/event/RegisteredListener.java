package com.gizmo.brennon.core.event;

import java.lang.reflect.Method;

class RegisteredListener {
    private final Object listener;
    private final Method method;
    private final int priority;

    public RegisteredListener(Object listener, Method method, int priority) {
        this.listener = listener;
        this.method = method;
        this.priority = priority;
        this.method.setAccessible(true);
    }

    public void call(Event event) throws Exception {
        method.invoke(listener, event);
    }

    public Object getListener() {
        return listener;
    }

    public int getPriority() {
        return priority;
    }
}
