package com.gizmo.brennon.core.command;

import com.gizmo.brennon.core.user.UserInfo;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CommandContext {
    private final String label;
    private final List<String> args;
    private final UUID sender;
    private final UserInfo senderInfo;
    private final boolean isPlayer;

    public CommandContext(String label, List<String> args, UUID sender, UserInfo senderInfo) {
        this.label = label;
        this.args = args;
        this.sender = sender;
        this.senderInfo = senderInfo;
        this.isPlayer = senderInfo != null;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getArgs() {
        return args;
    }

    public String getArg(int index) {
        return index < args.size() ? args.get(index) : "";
    }

    public Optional<String> getOptionalArg(int index) {
        return index < args.size() ? Optional.of(args.get(index)) : Optional.empty();
    }

    public UUID getSender() {
        return sender;
    }

    public Optional<UserInfo> getSenderInfo() {
        return Optional.ofNullable(senderInfo);
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public void reply(Component message) {
        // Implementation will be handled by the platform-specific code
    }

    public void replyError(Component message) {
        // Implementation will be handled by the platform-specific code
    }
}
