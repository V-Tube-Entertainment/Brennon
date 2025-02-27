package com.gizmo.brennon.core.player.rank;

import com.google.inject.Inject;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class LuckPermsGroupChecker {
    private final Logger logger;
    private final LuckPerms luckPerms;
    private final Map<String, NetworkRank> rankCache;

    @Inject
    public LuckPermsGroupChecker(Logger logger, LuckPerms luckPerms) {
        this.logger = logger;
        this.luckPerms = luckPerms;
        this.rankCache = new HashMap<>();
    }

    public void validateGroups() {
        Set<String> existingGroups = luckPerms.getGroupManager().getLoadedGroups()
                .stream()
                .map(Group::getName)
                .collect(Collectors.toSet());

        logger.info("Found LuckPerms groups: {}", existingGroups);

        // Check Network ranks against LuckPerms groups
        for (NetworkRank rank : NetworkRank.values()) {
            String groupName = rank.getGroupName();
            if (!existingGroups.contains(groupName)) {
                logger.error("Missing LuckPerms group: {} for rank {}", groupName, rank.name());
                logger.error("Weight should be: {}", rank.getWeight());
                logger.error("Please create this group in LuckPerms with command:");
                logger.error("/lp creategroup {} weight {}", groupName, rank.getWeight());
            } else {
                Group group = luckPerms.getGroupManager().getGroup(groupName);
                if (group != null && (!group.getWeight().isPresent() ||
                        group.getWeight().getAsInt() != rank.getWeight())) {
                    logger.warn("Group {} has incorrect weight. Expected: {}, Actual: {}",
                            groupName, rank.getWeight(),
                            group.getWeight().isPresent() ? group.getWeight().getAsInt() : "none");
                    logger.warn("Fix with command: /lp group {} setweight {}",
                            groupName, rank.getWeight());
                }
            }
        }

        // Check for extra LuckPerms groups
        for (String groupName : existingGroups) {
            if (NetworkRank.fromGroupName(groupName) == NetworkRank.DEFAULT &&
                    !groupName.equals("default")) {
                logger.warn("Found extra LuckPerms group not defined in NetworkRank: {}",
                        groupName);
            }
        }
    }

    public NetworkRank getRankFromLuckPerms(String groupName) {
        return rankCache.computeIfAbsent(groupName, this::resolveRank);
    }

    private NetworkRank resolveRank(String groupName) {
        Group group = luckPerms.getGroupManager().getGroup(groupName);
        if (group == null) {
            logger.warn("Unknown LuckPerms group: {}", groupName);
            return NetworkRank.DEFAULT;
        }

        NetworkRank rank = NetworkRank.fromGroupName(groupName);
        if (rank != NetworkRank.DEFAULT) {
            return rank;
        }

        // Try to match by weight
        OptionalInt weightOpt = group.getWeight();
        if (weightOpt.isPresent()) {
            int weight = weightOpt.getAsInt();
            for (NetworkRank r : NetworkRank.values()) {
                if (r.getWeight() == weight) {
                    return r;
                }
            }
        }

        // Check inherited groups
        Set<String> inheritedGroups = group.getNodes().stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .map(InheritanceNode::getGroupName)
                .collect(Collectors.toSet());

        for (String inherited : inheritedGroups) {
            NetworkRank inheritedRank = NetworkRank.fromGroupName(inherited);
            if (inheritedRank != NetworkRank.DEFAULT) {
                return inheritedRank;
            }
        }

        return NetworkRank.DEFAULT;
    }

    public void clearCache() {
        rankCache.clear();
    }

    public boolean isHigherRank(String group1, String group2) {
        NetworkRank rank1 = getRankFromLuckPerms(group1);
        NetworkRank rank2 = getRankFromLuckPerms(group2);
        return rank1.getWeight() > rank2.getWeight();
    }

    public List<String> getSuggestedFixCommands() {
        List<String> commands = new ArrayList<>();
        Set<String> existingGroups = luckPerms.getGroupManager().getLoadedGroups()
                .stream()
                .map(Group::getName)
                .collect(Collectors.toSet());

        for (NetworkRank rank : NetworkRank.values()) {
            String groupName = rank.getGroupName();
            if (!existingGroups.contains(groupName)) {
                commands.add(String.format("/lp creategroup %s", groupName));
                commands.add(String.format("/lp group %s setweight %d", groupName, rank.getWeight()));
                commands.add(String.format("/lp group %s meta setprefix \"%s\"",
                        groupName, rank.getColor() + rank.getDisplayName()));
            }
        }
        return commands;
    }
}