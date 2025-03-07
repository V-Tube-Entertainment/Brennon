package com.gizmo.brennon.core.api.serverbalancer;

import com.gizmo.brennon.core.api.server.IProxyServer;
import com.gizmo.brennon.core.api.utils.config.configs.ServerBalancerConfig.ServerBalancerGroup;

import java.util.Optional;

public interface ServerBalancer
{

    void setup();

    void shutdown();

    void reload();

    Optional<IProxyServer> getOptimalServer( ServerBalancerGroup serverBalancerGroup );

    Optional<IProxyServer> getOptimalServer( ServerBalancerGroup serverBalancerGroup, String serverToIgnore );

}
