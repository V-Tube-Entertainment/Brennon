package com.gizmo.brennon.core.api.event.events.user;

import com.gizmo.brennon.core.api.event.AbstractEvent;
import com.gizmo.brennon.core.api.event.event.Cancellable;
import com.gizmo.brennon.core.api.server.IProxyServer;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode( callSuper = true )
public class UserServerConnectEvent extends AbstractEvent implements Cancellable
{

    private final User user;
    private final ConnectReason connectReason;
    private IProxyServer target;
    private boolean cancelled;

    public UserServerConnectEvent( User user, IProxyServer target, ConnectReason connectReason )
    {
        this.user = user;
        this.connectReason = connectReason;
        this.target = target;
    }

    public enum ConnectReason
    {
        LOBBY_FALLBACK,
        COMMAND,
        SERVER_DOWN_REDIRECT,
        KICK_REDIRECT,
        PLUGIN_MESSAGE,
        JOIN_PROXY,
        PLUGIN,
        UNKNOWN;

        public static ConnectReason parse( String str )
        {
            for ( ConnectReason reason : values() )
            {
                if ( reason.toString().equalsIgnoreCase( str ) )
                {
                    return reason;
                }
            }
            return UNKNOWN;
        }
    }
}
