package com.gizmo.brennon.core.event;

public interface Cancellable {
    boolean isCancelled();
    void setCancelled(boolean cancelled);
}
