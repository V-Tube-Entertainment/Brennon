module brennon.core {
    // Required dependencies
    requires transitive com.google.guice;
    requires transitive org.slf4j;
    requires transitive com.zaxxer.hikari;
    requires transitive lettuce.core;
    requires transitive net.kyori.adventure;
    requires transitive net.kyori.adventure.text.minimessage;
    requires transitive net.kyori.adventure.text.serializer.gson;
    requires transitive org.spongepowered.configurate.hocon;
    requires transitive org.spongepowered.configurate.gson;
    requires transitive java.sql;
    requires static net.luckperms.api;
    requires com.google.gson;
    requires org.apache.commons.lang3;
    requires com.google.common;
    requires java.base;
    requires ch.qos.logback.classic;
    requires com.github.benmanes.caffeine;

    // Exports
    exports com.gizmo.brennon.core;
    exports com.gizmo.brennon.core.config;
    exports com.gizmo.brennon.core.database;
    exports com.gizmo.brennon.core.redis;
    exports com.gizmo.brennon.core.platform;
    exports com.gizmo.brennon.core.service;
    exports com.gizmo.brennon.core.punishment;
    exports com.gizmo.brennon.core.module;
    exports com.gizmo.brennon.core.ticket;
    exports com.gizmo.brennon.core.announcement;
    exports com.gizmo.brennon.core.permission;
    exports com.gizmo.brennon.core.logging;
    exports com.gizmo.brennon.core.monitoring;
    exports com.gizmo.brennon.core.balancing;
    exports com.gizmo.brennon.core.scheduler;
    exports com.gizmo.brennon.core.command;
    exports com.gizmo.brennon.core.messaging;
    exports com.gizmo.brennon.core.event;
    exports com.gizmo.brennon.core.util;
}