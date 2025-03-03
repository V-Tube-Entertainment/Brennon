package com.gizmo.brennon.core.player.rank;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import java.util.HashMap;
import java.util.Map;

public enum NetworkRank {
    // Management ranks
    BOSSMAN("bossman", "bossman", "#FF0000", 12000, true),
    IT_GUY("it-guy", "IT Guy", "#FF5555", 12000, true),
    SERVERMANAGER("server-manager", "Server Manager", "#55FFFF", 11000, true),
    STAFFMANAGER("staff-manager", "Staff Manager", "#5555FF", 11000, true),
    MEDIAMANAGER("media-manager", "Media Manager", "#55FF55", 10500, true),
    ADMIN("admin", "Admin", "#00FF00", 9000, true),
    DEV("dev", "Dev", "#FF55FF", 9000, true),
    HEADBUILDER("head-builder", "HeadBuilder", "#FF5555", 9000, true),
    JRADMIN("jr-admin", "JR. Admin", "#55FFFF", 8500, true),
    MOD("mod", "Mod", "#5555FF", 8000, true),
    JRMOD("jr-mod", "JR. Mod", "#00FF00", 7500, true),
    BUILDER("Builder", "Builder", "#FFFF55", 6000, true),
    BETATESTER("beta-tester", "Beta Tester", "#FFAA00", 5500, false),
    VIPPLUS("vip+", "VIP+", "#FF55FF", 1100, false),
    VIP("vip", "VIP", "#FF55FF", 1000, false),
    DEFAULT("default", "Default", "#AAAAAA", 0, false);

    private static final Map<String, NetworkRank> BY_GROUP_NAME = new HashMap<>();

    static {
        for (NetworkRank rank : values()) {
            BY_GROUP_NAME.put(rank.groupName.toLowerCase(), rank);
        }
    }

    private final String groupName;
    private final String displayName;
    private final String colorHex;
    private final TextColor textColor;
    private final int weight;
    private final boolean staff;

    NetworkRank(String groupName, String displayName, String colorHex, int weight, boolean staff) {
        this.groupName = groupName;
        this.displayName = displayName;
        this.colorHex = colorHex;
        this.textColor = TextColor.fromHexString(colorHex);
        this.weight = weight;
        this.staff = staff;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return colorHex;
    }

    public TextColor getTextColor() {
        return textColor;
    }

    public int getWeight() {
        return weight;
    }

    public boolean isStaff() {
        return staff;
    }

    public TextComponent getPrefix() {
        return Component.text(displayName).color(textColor);
    }

    public TextComponent getPrefixWithBrackets() {
        return Component.text()
                .content("[")
                .color(textColor)
                .append(Component.text(displayName).color(textColor))
                .append(Component.text("]").color(textColor))
                .build();
    }

    public TextComponent getStyledPrefix() {
        TextComponent prefix = Component.text()
                .content("[")
                .color(textColor)
                .build();

        TextComponent name = weight >= ADMIN.weight ?
                Component.text(displayName)
                        .color(textColor)
                        .decorate(TextDecoration.BOLD) :
                Component.text(displayName)
                        .color(textColor);

        return Component.text()
                .append(prefix)
                .append(name)
                .append(Component.text("]").color(textColor))
                .build();
    }

    public static NetworkRank fromGroupName(String name) {
        if (name == null) return DEFAULT;
        return BY_GROUP_NAME.getOrDefault(name.toLowerCase(), DEFAULT);
    }

    public boolean isAdmin() {
        return this.weight >= ADMIN.weight;
    }

    public boolean isManagement() {
        return this.weight >= STAFFMANAGER.weight;
    }

    public boolean isDeveloper() {
        return this == DEV || this == IT_GUY;
    }

    public boolean isBuilder() {
        return this == BUILDER || this == HEADBUILDER;
    }

    public boolean isModerator() {
        return this == MOD || this == JRMOD;
    }

    public boolean isVIP() {
        return this == VIP || this == VIPPLUS;
    }

    public boolean canManageStaff() {
        return this.weight >= STAFFMANAGER.weight;
    }

    public boolean canManageServer() {
        return this.weight >= SERVERMANAGER.weight;
    }

    public TextComponent colorize(String text) {
        return Component.text(text).color(textColor);
    }

    public TextComponent getTabListName(String username) {
        return Component.text()
                .append(getStyledPrefix())
                .append(Component.text(" "))
                .append(Component.text(username).color(textColor))
                .build();
    }

    public TextComponent getChatFormat(String username, String message) {
        return Component.text()
                .append(getStyledPrefix())
                .append(Component.text(" "))
                .append(Component.text(username).color(textColor))
                .append(Component.text(" » ").color(TextColor.color(0xAAAAAA)))
                .append(Component.text(message).color(TextColor.color(0xFFFFFF)))
                .build();
    }

    public boolean outranks(NetworkRank other) {
        return this.weight > other.weight;
    }

    public TextComponent getGradientText(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        TextColor darker = TextColor.color(
                Math.max((int)(textColor.red() * 0.8), 0),
                Math.max((int)(textColor.green() * 0.8), 0),
                Math.max((int)(textColor.blue() * 0.8), 0)
        );

        TextComponent.Builder builder = Component.text();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);
            TextColor interpolated = TextColor.color(
                    interpolate(textColor.red(), darker.red(), ratio),
                    interpolate(textColor.green(), darker.green(), ratio),
                    interpolate(textColor.blue(), darker.blue(), ratio)
            );
            builder.append(Component.text(text.charAt(i)).color(interpolated));
        }

        return builder.build();
    }

    private int interpolate(int start, int end, float ratio) {
        return (int) (start * (1 - ratio) + end * ratio);
    }
}
