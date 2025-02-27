package com.gizmo.brennon.core.command;

import com.google.inject.Inject;
import com.gizmo.brennon.core.permission.PermissionService;
import com.gizmo.brennon.core.service.Service;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CommandManager implements Service {
    private final Logger logger;
    private final PermissionService permissionService;
    private final Map<String, CommandRegistration> commands;
    private final Executor asyncExecutor;

    @Inject
    public CommandManager(Logger logger, PermissionService permissionService) {
        this.logger = logger;
        this.permissionService = permissionService;
        this.commands = new ConcurrentHashMap<>();
        this.asyncExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public void enable() throws Exception {
        commands.clear();
    }

    @Override
    public void disable() throws Exception {
        commands.clear();
    }

    public void registerCommands(Object instance) {
        for (Method method : instance.getClass().getDeclaredMethods()) {
            Command command = method.getAnnotation(Command.class);
            if (command == null) continue;

            CommandRegistration registration = new CommandRegistration(
                    command,
                    instance,
                    method
            );

            commands.put(command.name().toLowerCase(), registration);
            for (String alias : command.aliases()) {
                commands.put(alias.toLowerCase(), registration);
            }
        }
    }

    public void unregisterCommands(Object instance) {
        commands.values().removeIf(reg -> reg.instance() == instance);
    }

    public Optional<CommandRegistration> getCommand(String name) {
        return Optional.ofNullable(commands.get(name.toLowerCase()));
    }

    public Collection<CommandRegistration> getCommands() {
        return new HashSet<>(commands.values());
    }

    public CompletableFuture<Boolean> executeCommand(CommandContext context) {
        String commandName = context.getLabel().toLowerCase();
        Optional<CommandRegistration> registration = getCommand(commandName);

        if (registration.isEmpty()) {
            context.replyError(Component.text("Unknown command."));
            return CompletableFuture.completedFuture(false);
        }

        CommandRegistration reg = registration.get();
        Command command = reg.command();

        // Check if player is required
        if (command.requiresPlayer() && !context.isPlayer()) {
            context.replyError(Component.text("This command can only be used by players."));
            return CompletableFuture.completedFuture(false);
        }

        // Check permission
        if (!command.permission().isEmpty() &&
                !permissionService.hasPermission(context.getSender(), command.permission())) {
            context.replyError(Component.text("You don't have permission to use this command."));
            return CompletableFuture.completedFuture(false);
        }

        // Execute command
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Runnable task = () -> {
            try {
                reg.method().invoke(reg.instance(), context);
                future.complete(true);
            } catch (Exception e) {
                logger.error("Error executing command: " + command.name(), e);
                context.replyError(Component.text("An error occurred while executing this command."));
                future.complete(false);
            }
        };

        if (command.async()) {
            asyncExecutor.execute(task);
        } else {
            task.run();
        }

        return future;
    }

    public List<String> tabComplete(CommandContext context) {
        String commandName = context.getLabel().toLowerCase();
        Optional<CommandRegistration> registration = getCommand(commandName);

        if (registration.isEmpty()) {
            return List.of();
        }

        CommandRegistration reg = registration.get();
        Command command = reg.command();

        // Check permission for tab completion
        if (!command.permission().isEmpty() &&
                !permissionService.hasPermission(context.getSender(), command.permission())) {
            return List.of();
        }

        try {
            Method tabComplete = reg.instance().getClass().getMethod("tabComplete", CommandContext.class);
            @SuppressWarnings("unchecked")
            List<String> completions = (List<String>) tabComplete.invoke(reg.instance(), context);
            return completions != null ? completions : List.of();
        } catch (NoSuchMethodException e) {
            return List.of(); // No tab completions defined
        } catch (Exception e) {
            logger.error("Error getting tab completions for command: " + command.name(), e);
            return List.of();
        }
    }
}
