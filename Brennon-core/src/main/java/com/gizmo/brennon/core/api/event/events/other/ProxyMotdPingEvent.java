package com.gizmo.brennon.core.api.event.events.other;

import com.gizmo.brennon.core.api.event.AbstractEvent;
import com.gizmo.brennon.core.api.event.event.HasCompletionHandlers;
import com.gizmo.brennon.core.motd.MotdConnection;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Data
@EqualsAndHashCode( callSuper = true )
public class ProxyMotdPingEvent extends AbstractEvent implements HasCompletionHandlers<ProxyMotdPingEvent>
{

    private final MotdConnection motdConnection;
    private final List<Consumer<ProxyMotdPingEvent>> completionHandlers;
    private MotdPingResponse motdPingResponse;

    public ProxyMotdPingEvent( final MotdConnection connection, final Consumer<ProxyMotdPingEvent>... completionTasks )
    {
        this.motdConnection = connection;
        this.completionHandlers = List.of( completionTasks );
    }

    @Value
    public static class MotdPingResponse
    {
        Component motd;
        List<MotdPingPlayer> players;
    }

    @Value
    public static class MotdPingPlayer
    {
        String name;
        UUID uuid;
    }

}
