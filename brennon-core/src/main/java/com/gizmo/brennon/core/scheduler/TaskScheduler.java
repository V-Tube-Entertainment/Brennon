package com.gizmo.brennon.core.scheduler;

import com.google.inject.Inject;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskScheduler implements Service {
    private final Logger logger;
    private ScheduledExecutorService scheduledExecutor;
    private ExecutorService asyncExecutor;
    private final AtomicInteger taskIdCounter;

    @Inject
    public TaskScheduler(Logger logger) {
        this.logger = logger;
        this.taskIdCounter = new AtomicInteger(0);
    }

    @Override
    public void enable() {
        scheduledExecutor = Executors.newScheduledThreadPool(4, new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("Brennon-Scheduled-" + counter.incrementAndGet());
                return thread;
            }
        });

        asyncExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
            private final AtomicInteger counter = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("Brennon-Async-" + counter.incrementAndGet());
                return thread;
            }
        });
    }

    @Override
    public void disable() {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            try {
                if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduledExecutor.shutdownNow();
            }
        }

        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
            try {
                if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
            }
        }
    }

    public ScheduledTask scheduleAsync(Runnable task) {
        int taskId = taskIdCounter.incrementAndGet();
        Future<?> future = asyncExecutor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Error in async task #" + taskId, e);
            }
        });
        return new ScheduledTask(taskId, future);
    }

    public ScheduledTask scheduleDelayed(Runnable task, long delay, TimeUnit unit) {
        int taskId = taskIdCounter.incrementAndGet();
        ScheduledFuture<?> future = scheduledExecutor.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Error in delayed task #" + taskId, e);
            }
        }, delay, unit);
        return new ScheduledTask(taskId, future);
    }

    public ScheduledTask scheduleRepeating(Runnable task, long initialDelay, long period, TimeUnit unit) {
        int taskId = taskIdCounter.incrementAndGet();
        ScheduledFuture<?> future = scheduledExecutor.scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                logger.error("Error in repeating task #" + taskId, e);
            }
        }, initialDelay, period, unit);
        return new ScheduledTask(taskId, future);
    }

    public void cancelTask(ScheduledTask task) {
        if (task != null && !task.future().isDone()) {
            task.future().cancel(false);
        }
    }

    public void cancelAllTasks() {
        scheduledExecutor.shutdown();
        asyncExecutor.shutdown();
        enable(); // Recreate the executors
    }
}
