package com.gizmo.brennon.core.executors;

import com.gizmo.brennon.core.api.event.event.Event;
import com.gizmo.brennon.core.api.event.event.EventExecutor;
import com.gizmo.brennon.core.api.event.events.other.ProxyMotdPingEvent;
import com.gizmo.brennon.core.api.event.events.other.ProxyMotdPingEvent.MotdPingPlayer;
import com.gizmo.brennon.core.api.event.events.other.ProxyMotdPingEvent.MotdPingResponse;
import com.gizmo.brennon.core.api.utils.MathUtils;
import com.gizmo.brennon.core.api.utils.MessageUtils;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.Version;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.motd.ConditionHandler;
import com.gizmo.brennon.core.motd.MotdConnection;
import com.gizmo.brennon.core.motd.MotdData;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ProxyMotdPingExecutor implements EventExecutor
{

    private static final UUID RANDOM_UUID = UUID.fromString( "1d92ed4b-2d2e-4b6b-9cc0-75b018c0e0ca" );

    @Event
    public void onProxyMotdPing( final ProxyMotdPingEvent event )
    {
        final List<MotdData> dataList = ConfigFiles.MOTD.getMotds();
        MotdPingResponse result = loadConditionalMotd( event.getMotdConnection(), dataList );

        if ( result == null )
        {
            result = loadDefaultMotd( event.getMotdConnection(), dataList );
        }
        if ( result != null )
        {
            event.setMotdPingResponse( result );
        }
    }

    private MotdPingResponse loadMotd( final MotdConnection connection,
                                       final MotdData motd )
    {
        if ( motd == null )
        {
            return null;
        }
        final String message = formatMessage( motd.getMotd(), connection );
        final Component component = Utils.format( message );

        return new MotdPingResponse(
            component,
            motd.getHoverMessages()
                .stream()
                .map( m -> new MotdPingPlayer(
                    MessageUtils.colorizeLegacy( Utils.formatString( formatMessage( m, connection ) ) ),
                        RANDOM_UUID
                ) )
                .collect( Collectors.toList() )
        );
    }

    private String formatMessage( String message, final MotdConnection connection )
    {
        if ( message.contains( "{user}" ) )
        {
            message = message.replace( "{user}", connection.getName() == null ? "Unknown" : connection.getName() );
        }
        Version version = Version.getVersion( connection.getVersion() );
        message = message.replace( "{version}", version == Version.UNKNOWN_NEW_VERSION ? "Unknown" : version.toString() );

        if ( connection.getVirtualHost() == null || connection.getVirtualHost().getHostName() == null )
        {
            message = message.replace( "{domain}", "Unknown" );
        }
        else
        {
            message = message.replace( "{domain}", connection.getVirtualHost().getHostName() );
        }

        return message;
    }

    private MotdPingResponse loadDefaultMotd( final MotdConnection connection, final List<MotdData> motds )
    {
        final List<MotdData> defMotds = motds.stream().filter( MotdData::isDef ).collect( Collectors.toList() );
        final MotdData motd = MathUtils.getRandomFromList( defMotds );

        return loadMotd( connection, motd );
    }

    private MotdPingResponse loadConditionalMotd( final MotdConnection connection, final List<MotdData> motds )
    {
        final List<MotdData> conditions = motds.stream().filter( data -> !data.isDef() ).toList();

        for ( final MotdData condition : conditions )
        {
            final ConditionHandler handler = condition.getConditionHandler();

            if ( handler.checkCondition( connection ) )
            {
                final List<MotdData> conditionalMotds = conditions.stream().filter(
                    data -> data.getConditionHandler().getCondition().equalsIgnoreCase( handler.getCondition() )
                ).collect( Collectors.toList() );
                final MotdData motd = MathUtils.getRandomFromList( conditionalMotds );

                return loadMotd( connection, motd );
            }
        }
        return null;
    }
}
