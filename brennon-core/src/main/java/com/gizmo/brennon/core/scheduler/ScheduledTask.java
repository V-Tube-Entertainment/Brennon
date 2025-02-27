package com.gizmo.brennon.core.scheduler;

import java.util.concurrent.Future;

public record ScheduledTask(
        int id,
        Future<?> future
) {
    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean isDone() {
        return future.isDone();
    }

    public void cancel() {
        future.cancel(false);
    }
}
