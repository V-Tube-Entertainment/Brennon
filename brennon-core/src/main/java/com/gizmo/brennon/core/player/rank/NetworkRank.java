package com.gizmo.brennon.core.player.rank;


import net.luckperms.api.model.group.Group;

public enum NetworkRank {
    OWNER("owner", "Owner", "§4", 1000),
    ADMIN("admin", "Admin", "§c", 900),
    SR_MOD("sr_mod", "Sr.Mod", "§3", 800),
    MOD("mod", "Mod", "§2", 700),
    JR_MOD("jr_mod", "Jr.Mod", "§a", 600),
    BUILDER("builder", "Builder", "§d", 500),
    YOUTUBE("youtube", "YouTube", "§c", 400),
    MVP_PLUS("mvp_plus", "MVP+", "§b", 300),
    MVP("mvp", "MVP", "§9", 200),
    VIP_PLUS("vip_plus", "VIP+", "§a", 100),
    VIP("vip", "VIP", "§e", 50),
    DEFAULT("default", "Default", "§7", 0);

    private final String groupName;
    private final String displayName;
    private final String color;
    private final int weight;

    NetworkRank(String groupName, String displayName, String color, int weight) {
        this.groupName = groupName;
        this.displayName = displayName;
        this.color = color;
        this.weight = weight;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public int getWeight() {
        return weight;
    }

    public String getPrefix() {
        return color + displayName;
    }

    public static NetworkRank fromGroup(Group group) {
        if (group == null) return DEFAULT;
        return fromGroupName(group.getName());
    }

    public static NetworkRank fromGroupName(String name) {
        for (NetworkRank rank : values()) {
            if (rank.groupName.equalsIgnoreCase(name)) {
                return rank;
            }
        }
        return DEFAULT;
    }

    public boolean isStaff() {
        return this.weight >= MOD.weight;
    }

    public boolean isAdmin() {
        return this.weight >= ADMIN.weight;
    }
}
