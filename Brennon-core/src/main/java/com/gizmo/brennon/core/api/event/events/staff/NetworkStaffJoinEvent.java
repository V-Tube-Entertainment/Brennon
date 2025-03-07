package com.gizmo.brennon.core.api.event.events.staff;

import com.gizmo.brennon.core.api.event.AbstractEvent;
import com.gizmo.brennon.core.api.event.event.Cancellable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * This event will be executed upon network join
 */
@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
public class NetworkStaffJoinEvent extends AbstractEvent implements Cancellable
{

    private final String userName;
    private final UUID uuid;
    private final String staffRank;

    private boolean cancelled;

    public NetworkStaffJoinEvent( String userName, UUID uuid, String staffRank )
    {
        this.userName = userName;
        this.uuid = uuid;
        this.staffRank = staffRank;
    }
}
