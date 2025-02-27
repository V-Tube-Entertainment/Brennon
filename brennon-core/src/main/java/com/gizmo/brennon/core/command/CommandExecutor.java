package com.gizmo.brennon.core.command;

import com.gizmo.brennon.core.platform.CommandSender;

public interface CommandExecutor {
    boolean execute(CommandSender sender, String label, String[] args);
}
