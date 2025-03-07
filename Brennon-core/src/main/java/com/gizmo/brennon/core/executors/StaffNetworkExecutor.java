package com.gizmo.brennon.core.executors;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.event.event.Event;
import com.gizmo.brennon.core.api.event.event.EventExecutor;
import com.gizmo.brennon.core.api.event.events.staff.NetworkStaffJoinEvent;
import com.gizmo.brennon.core.api.event.events.staff.NetworkStaffLeaveEvent;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.other.StaffRankData;
import com.gizmo.brennon.core.api.utils.other.StaffUser;

public class StaffNetworkExecutor implements EventExecutor
{

    @Event
    public void onJoin( final NetworkStaffJoinEvent event )
    {
        BuX.getInstance().getStaffMembers().add(
            new StaffUser( event.getUserName(), event.getUuid(), findStaffRank( event.getStaffRank() ) )
        );
    }

    @Event
    public void onLeave( final NetworkStaffLeaveEvent event )
    {
        BuX.getInstance().getStaffMembers().removeIf(
            staffUser -> staffUser.getName().equals( event.getUserName() )
        );
    }

    private StaffRankData findStaffRank( final String rankName )
    {
        return ConfigFiles.RANKS.getRanks().stream()
            .filter( rank -> rank.getName().equals( rankName ) )
            .findFirst()
            .orElseThrow( () -> new RuntimeException(
                "Could not find a staff rank called \"" + rankName + "\"."
                    + " If you are using redis, make sure the configs are synchronized."
            ) );
    }
}
