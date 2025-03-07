package com.gizmo.brennon.core.api.event.events.user;

import com.gizmo.brennon.core.api.event.AbstractEvent;
import com.gizmo.brennon.core.api.event.event.Cancellable;
import com.gizmo.brennon.core.api.server.IProxyServer;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode( callSuper = true )
public class UserServerConnectedEvent extends AbstractEvent implements Cancellable
{

    private final User user;
    private final Optional<IProxyServer> previous;
    private final IProxyServer target;
    private boolean cancelled;

}
