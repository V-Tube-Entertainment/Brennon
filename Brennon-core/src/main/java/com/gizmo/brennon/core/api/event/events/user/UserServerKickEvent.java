package com.gizmo.brennon.core.api.event.events.user;

import com.gizmo.brennon.core.api.event.AbstractEvent;
import com.gizmo.brennon.core.api.server.IProxyServer;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.kyori.adventure.text.Component;

@Data
@AllArgsConstructor
@EqualsAndHashCode( callSuper = true )
public class UserServerKickEvent extends AbstractEvent
{

    private final User user;
    private final IProxyServer kickedFrom;
    private IProxyServer redirectServer;
    private Component kickMessage;
    private boolean targetChanged;

    public UserServerKickEvent( User user, IProxyServer kickedFrom, IProxyServer redirectServer, Component kickMessage )
    {
        this.user = user;
        this.kickedFrom = kickedFrom;
        this.redirectServer = redirectServer;
        this.kickMessage = kickMessage;
    }

    public void setRedirectServer( IProxyServer redirectServer )
    {
        this.redirectServer = redirectServer;
        this.targetChanged = true;
    }
}