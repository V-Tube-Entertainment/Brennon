module brennon.core {
    // Required dependencies
    requires com.google.guice;
    requires org.slf4j;
    requires com.zaxxer.hikari;
    requires io.lettuce.core;
    requires net.kyori.adventure.api;
    requires net.kyori.adventure.text.minimessage;
    requires net.kyori.adventure.text.serializer.gson;
    requires org.spongepowered.configurate.core;
    requires org.spongepowered.configurate.hocon;
    requires org.spongepowered.configurate.gson;
    requires java.sql;
    requires com.google.gson;
    requires org.apache.commons.lang3;
    requires com.google.common;
    requires java.base;
    requires ch.qos.logback.classic;
    requires com.github.benmanes.caffeine;
    requires static net.luckperms;
    requires lettuce.core;

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