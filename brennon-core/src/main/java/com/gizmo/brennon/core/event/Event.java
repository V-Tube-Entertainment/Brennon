package com.gizmo.brennon.core.event;

public interface Event {
    /**
     * Gets the name of the event.
     * @return The event name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
