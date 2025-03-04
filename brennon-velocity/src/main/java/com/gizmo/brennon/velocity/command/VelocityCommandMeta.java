package com.gizmo.brennon.velocity.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Holds metadata for Velocity commands
 *
 * @author Gizmo0320
 * @since 2025-03-04 02:10:30
 */
public class VelocityCommandMeta {
    private final String name;
    private final String permission;
    private final String description;
    private final List<String> aliases;
    private final boolean isStaffCommand;

    private VelocityCommandMeta(Builder builder) {
        this.name = builder.name;
        this.permission = builder.permission;
        this.description = builder.description;
        this.aliases = Collections.unmodifiableList(new ArrayList<>(builder.aliases));
        this.isStaffCommand = builder.isStaffCommand;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean isStaffCommand() {
        return isStaffCommand;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private String permission;
        private String description;
        private final List<String> aliases = new ArrayList<>();
        private boolean isStaffCommand;

        private Builder(String name) {
            this.name = name;
        }

        public Builder permission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder aliases(String... aliases) {
            this.aliases.addAll(Arrays.asList(aliases));
            return this;
        }

        public Builder staffCommand() {
            this.isStaffCommand = true;
            return this;
        }

        public VelocityCommandMeta build() {
            return new VelocityCommandMeta(this);
        }
    }
}
