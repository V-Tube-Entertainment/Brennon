package com.gizmo.brennon.core.api.utils.server;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.server.IProxyServer;
import com.google.common.collect.Lists;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ServerGroup
{

    private final String name;
    private final boolean global;
    private final boolean dynamic;
    private final List<String> serverNames;

    public ServerGroup( String name, boolean global, boolean dynamic, List<String> servers )
    {
        this.name = name;
        this.global = global;
        this.dynamic = dynamic;
        this.serverNames = dynamic ? servers : searchServers( servers );
    }

    public int getPlayers()
    {
        int players = 0;

        if ( global )
        {
            return BuX.getApi().getPlayerUtils().getTotalCount();
        }

        for ( String server : this.searchServers() )
        {
            players += BuX.getApi().getPlayerUtils().getPlayerCount( server );
        }

        return players;
    }

    public List<String> getPlayerList()
    {
        List<String> players = Lists.newArrayList();

        if ( global )
        {
            return BuX.getApi().getPlayerUtils().getPlayers();
        }

        for ( String server : this.searchServers() )
        {
            players.addAll( BuX.getApi().getPlayerUtils().getPlayers( server ) );
        }

        return players;
    }

    public List<IProxyServer> getServers()
    {
        final List<IProxyServer> servers = Lists.newArrayList();

        this.searchServers().forEach( serverName ->
        {
            IProxyServer info = BuX.getInstance().serverOperations().getServerInfo( serverName );

            if ( info != null )
            {
                servers.add( info );
            }
        } );

        return servers;
    }

    public boolean isInGroup( final String serverName )
    {
        for ( String server : this.searchServers() )
        {
            if ( server.equalsIgnoreCase( serverName ) )
            {
                return true;
            }
        }
        return false;
    }

    private List<String> searchServers()
    {
        return dynamic ? searchServers( serverNames ) : serverNames;
    }

    private List<String> searchServers( List<String> servers )
    {
        LinkedList<String> foundServers = Lists.newLinkedList();

        BuX.debug( "Scanning servers for group %s with server list %s. Available servers are: %s".formatted(
                name,
                String.join( ", ", servers ),
                BuX.getInstance().serverOperations().getServers().stream().map( IProxyServer::getName ).collect( Collectors.joining( ", " ) )
        ) );

        servers.forEach( server ->
        {
            for ( IProxyServer info : BuX.getInstance().serverOperations().getServers() )
            {
                String name = info.getName().toLowerCase();

                if ( server.startsWith( "*" ) )
                {
                    if ( name.endsWith( server.substring( 1 ).toLowerCase() ) )
                    {
                        foundServers.add( info.getName() );
                    }
                }
                else if ( server.endsWith( "*" ) )
                {
                    if ( name.startsWith( server.substring( 0, server.length() - 1 ).toLowerCase() ) )
                    {
                        foundServers.add( info.getName() );
                    }
                }
                else
                {
                    if ( name.equalsIgnoreCase( server ) )
                    {
                        foundServers.add( info.getName() );
                    }
                }
            }
        } );

        BuX.debug( "Found servers: %s".formatted( String.join( ", ", foundServers ) ) );

        return foundServers;
    }
}
