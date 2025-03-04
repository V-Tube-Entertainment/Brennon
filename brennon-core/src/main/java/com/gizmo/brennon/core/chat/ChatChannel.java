package com.gizmo.brennon.core.chat;

public class ChatChannel {
    private final String id;
    private final String name;
    private final String permission;
    private final String color;
    private final String prefix;
    private final boolean crossServer;
    private final double radius;

    public ChatChannel(String id, String name, String permission, String color,
                       String prefix, boolean crossServer, double radius) {
        this.id = id;
        this.name = name;
        this.permission = permission;
        this.color = color;
        this.prefix = prefix;
        this.crossServer = crossServer;
        this.radius = radius;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPermission() { return permission; }
    public String getColor() { return color; }
    public String getPrefix() { return prefix; }
    public boolean isCrossServer() { return crossServer; }
    public double getRadius() { return radius; }
}
