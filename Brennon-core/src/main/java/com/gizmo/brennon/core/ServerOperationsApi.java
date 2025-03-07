package com.gizmo.brennon.core;

import com.gizmo.brennon.core.api.command.Command;
import com.gizmo.brennon.core.api.server.IProxyServer;
import com.gizmo.brennon.core.api.utils.other.PluginInfo;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Optional;

public interface ServerOperationsApi
{

    void registerCommand( Command command );

    void unregisterCommand( Command command );

    List<IProxyServer> getServers();

    IProxyServer getServerInfo( String serverName );

    List<PluginInfo> getPlugins();

    Optional<PluginInfo> getPlugin( String pluginName );

    Optional<Object> getPluginInstance( String pluginName );

    long getMaxPlayers();

    Object getMessageComponent( final Component component );

}
