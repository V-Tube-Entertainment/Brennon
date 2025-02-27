package com.gizmo.brennon.core.service;

import com.google.inject.Inject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServiceRegistry {
    private final Logger logger;
    private final List<Service> services;

    @Inject
    public ServiceRegistry(Logger logger) {
        this.logger = logger;
        this.services = new CopyOnWriteArrayList<>();
    }

    public void registerService(Service service) {
        services.add(service);
    }

    public void enableServices() {
        logger.info("Enabling services...");
        for (Service service : services) {
            try {
                service.enable();
                logger.info("Enabled service: {}", service.getClass().getSimpleName());
            } catch (Exception e) {
                logger.error("Failed to enable service: " + service.getClass().getSimpleName(), e);
            }
        }
    }

    public void disableServices() {
        logger.info("Disabling services...");
        // Disable in reverse order
        for (int i = services.size() - 1; i >= 0; i--) {
            Service service = services.get(i);
            try {
                service.disable();
                logger.info("Disabled service: {}", service.getClass().getSimpleName());
            } catch (Exception e) {
                logger.error("Failed to disable service: " + service.getClass().getSimpleName(), e);
            }
        }
        services.clear();
    }

    public <T extends Service> T getService(Class<T> serviceClass) {
        return services.stream()
                .filter(service -> serviceClass.isAssignableFrom(service.getClass()))
                .map(serviceClass::cast)
                .findFirst()
                .orElse(null);
    }
}