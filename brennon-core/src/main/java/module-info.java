module brennon.core {
    requires com.google.guice;
    requires org.slf4j;
    requires com.zaxxer.hikari;
    requires lettuce.core;
    requires net.kyori.adventure;
    requires net.kyori.adventure.text.minimessage;
    requires net.kyori.adventure.text.serializer.gson;
    requires org.spongepowered.configurate.hocon;
    requires com.google.gson;
    requires java.sql;
    requires api;
    requires org.spongepowered.configurate;

    exports com.gizmo.brennon.core;
    exports com.gizmo.brennon.core.config;
    exports com.gizmo.brennon.core.database;
    exports com.gizmo.brennon.core.redis;
    exports com.gizmo.brennon.core.platform;
    exports com.gizmo.brennon.core.service;
    exports com.gizmo.brennon.core.punishment;
    exports com.gizmo.brennon.core.module;
}