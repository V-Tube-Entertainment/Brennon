package com.gizmo.brennon.core.event;

import com.google.inject.Inject;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private final Logger logger;
    private final Map<Class<? extends Event>, List<RegisteredListener>> listeners;

    @Inject
    public EventBus(Logger logger) {
        this.logger = logger;
        this.listeners = new ConcurrentHashMap<>();
    }

    public void registerListener(Object listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            Subscribe subscribe = method.getAnnotation(Subscribe.class);
            if (subscribe != null) {
                if (method.getParameterCount() != 1) {
                    logger.warn("Invalid event listener method {}: must have exactly one parameter",
                            method.getName());
                    continue;
                }

                Class<?> paramType = method.getParameterTypes()[0];
                if (!Event.class.isAssignableFrom(paramType)) {
                    logger.warn("Invalid event listener method {}: parameter must be an Event",
                            method.getName());
                    continue;
                }

                @SuppressWarnings("unchecked")
                Class<? extends Event> eventType = (Class<? extends Event>) paramType;
                RegisteredListener registeredListener = new RegisteredListener(
                        listener,
                        method,
                        subscribe.priority()
                );

                listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                        .add(registeredListener);

                // Sort listeners by priority
                listeners.get(eventType).sort((l1, l2) ->
                        Integer.compare(l2.getPriority(), l1.getPriority()));
            }
        }
    }

    public void unregisterListener(Object listener) {
        listeners.values().forEach(listenerList ->
                listenerList.removeIf(registeredListener ->
                        registeredListener.getListener() == listener));
    }

    public void post(Event event) {
        List<RegisteredListener> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (RegisteredListener listener : eventListeners) {
                try {
                    listener.call(event);
                } catch (Exception e) {
                    logger.error("Error dispatching event {} to listener {}",
                            event.getName(), listener.getListener().getClass().getName(), e);
                }
            }
        }
    }
}
