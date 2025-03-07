package com.gizmo.brennon.core.api.utils.config.configs;

import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.config.Config;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.server.ServerGroup;
import dev.endoy.configuration.api.ISection;
import lombok.Getter;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public class ServerBalancerConfig extends Config
{

    private final List<ServerBalancerGroup> balancerGroups = new ArrayList<>();
    private FallbackConfig fallbackConfig;

    public ServerBalancerConfig( final String location )
    {
        super( location );
    }

    @Override
    public void purge()
    {
        this.balancerGroups.clear();
    }

    @Override
    protected void setup()
    {
        if ( config == null || !isEnabled() )
        {
            return;
        }

        balancerGroups.clear();

        for ( ISection section : config.getSectionList( "balancers" ) )
        {
            ServerGroup group = ConfigFiles.SERVERGROUPS.getServer( section.getString( "group" ) ).orElse( null );

            if ( group == null )
            {
                continue;
            }
            ServerBalancingMethod method = Utils.valueOfOr( section.getString( "method" ), ServerBalancingMethod.LEAST_PLAYERS );
            boolean allowSendingToOtherServers = section.getBoolean( "allow-sending-to-other-servers" );
            ISection commandSection = section.getSection( "command" );
            ISection pingerSection = section.getSection( "pinger" );
            ServerBalancerGroupPinger pinger = new ServerBalancerGroupPinger(
                pingerSection.getInteger( "delay" ),
                pingerSection.getInteger( "max-attempts" ),
                pingerSection.getInteger( "cooldown" ),
                pingerSection.exists( "motd-filter" )
                    ? pingerSection.getStringList( "motd-filter" ).stream().map( Pattern::compile ).collect( Collectors.toList() )
                    : new ArrayList<>()
            );

            balancerGroups.add( new ServerBalancerGroup( group, method, allowSendingToOtherServers, commandSection, pinger ) );
        }
        fallbackConfig = new FallbackConfig(
            FallbackMode.valueOf( config.getString( "fallback.type" ).toUpperCase() ),
            config.getStringList( "fallback.reasons" ),
            this.getServerBalancerGroupFor( config.getString( "fallback.fallback-to" ) ).orElse( null ),
            config.getStringList( "fallback.block-fallback-from", new ArrayList<>() )
                    .stream()
                    .map( ConfigFiles.SERVERGROUPS::getServer )
                    .filter( Optional::isPresent )
                    .map( Optional::get )
                    .collect( Collectors.toList() )
        );
    }

    public Optional<ServerBalancerGroup> getServerBalancerGroupByName( final String groupName )
    {
        return balancerGroups
            .stream()
            .filter( it -> it.getServerGroup().getName().equalsIgnoreCase( groupName ) )
            .findFirst();
    }

    public Optional<ServerBalancerGroup> getServerBalancerGroupFor( final String serverName )
    {
        return balancerGroups
            .stream()
            .filter( it -> it.getServerGroup().getName().equals( serverName ) || it.getServerGroup().isInGroup( serverName ) )
            .findFirst();
    }

    public enum ServerBalancingMethod
    {
        RANDOM, LEAST_PLAYERS, FIRST_NON_FULL, MOST_PLAYERS
    }

    public enum FallbackMode
    {
        BLACKLIST, WHITELIST
    }

    @Value
    public static class ServerBalancerGroup
    {
        ServerGroup serverGroup;
        ServerBalancingMethod method;
        boolean allowSendingToOtherServers;
        ISection commandSection;
        ServerBalancerGroupPinger pinger;
    }

    @Value
    public static class ServerBalancerGroupPinger
    {
        int delay;
        int maxAttempts;
        int cooldown;
        List<Pattern> motdFilters;
    }

    @Value
    public static class FallbackConfig
    {
        FallbackMode fallbackMode;
        List<String> reasons;
        ServerBalancerGroup fallbackGroup;
        List<ServerGroup> blockFallbackFrom;
    }
}