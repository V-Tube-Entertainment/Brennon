package com.gizmo.brennon.core.command;

import com.google.inject.Inject;
import com.gizmo.brennon.core.platform.CommandSender;
import com.gizmo.brennon.core.scheduler.TaskScheduler;
import com.gizmo.brennon.core.service.Service;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandManager implements Service {
    private final Logger logger;
    private final TaskScheduler scheduler;
    private final Map<String, RegisteredCommand> commands;

    @Inject
    public CommandManager(Logger logger, TaskScheduler scheduler) {
        this.logger = logger;
        this.scheduler = scheduler;
        this.commands = new HashMap<>();
    }

    public void registerCommand(Object handler) {
        for (Method method : handler.getClass().getDeclaredMethods()) {
            Command command = method.getAnnotation(Command.class);
            if (command != null) {
                if (method.getParameterCount() != 3 ||
                        !CommandSender.class.isAssignableFrom(method.getParameterTypes()[0]) ||
                        !String.class.equals(method.getParameterTypes()[1]) ||
                        !String[].class.equals(method.getParameterTypes()[2])) {
                    logger.error("Invalid command method signature: {}", method);
                    continue;
                }

                RegisteredCommand cmd = new RegisteredCommand(
                        command.name(),
                        command.description(),
                        command.permission(),
                        command.usage(),
                        command.async(),
                        handler,
                        method
                );

                commands.put(command.name().toLowerCase(), cmd);
                for (String alias : command.aliases()) {
                    commands.put(alias.toLowerCase(), cmd);
                }
            }
        }
    }

    public boolean executeCommand(CommandSender sender, String commandLine) {
        String[] split = commandLine.split(" ");
        String label = split[0].toLowerCase();
        String[] args = Arrays.copyOfRange(split, 1, split.length);

        RegisteredCommand command = commands.get(label);
        if (command == null) {
            return false;
        }

        if (!command.permission().isEmpty() && !sender.hasPermission(command.permission())) {
            sender.sendMessage("§cYou don't have permission to use this command.");
            return true;
        }

        if (command.async()) {
            scheduler.scheduleAsync(() -> executeCommand(command, sender, label, args));
        } else {
            executeCommand(command, sender, label, args);
        }

        return true;
    }

    private void executeCommand(RegisteredCommand command, CommandSender sender, String label, String[] args) {
        try {
            command.method().invoke(command.handler(), sender, label, args);
        } catch (Exception e) {
            logger.error("Error executing command: " + command.name(), e);
            sender.sendMessage("§cAn error occurred while executing this command.");
        }
    }

    @Override
    public void disable() throws Exception {
        // Cleanup if needed
    }
}
