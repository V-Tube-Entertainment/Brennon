package com.gizmo.brennon.core.executors;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.event.event.Event;
import com.gizmo.brennon.core.api.event.event.EventExecutor;
import com.gizmo.brennon.core.api.event.events.staff.NetworkStaffJoinEvent;
import com.gizmo.brennon.core.api.event.events.staff.NetworkStaffLeaveEvent;
import com.gizmo.brennon.core.api.event.events.user.UserLoadEvent;
import com.gizmo.brennon.core.api.event.events.user.UserUnloadEvent;
import com.gizmo.brennon.core.api.job.jobs.ExecuteNetworkStaffEventJob;
import com.gizmo.brennon.core.api.redis.IRedisDataManager;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.StaffUtils;

public class UserExecutor implements EventExecutor
{

    @Event
    public void onLoad( final UserLoadEvent event )
    {
        event.getApi().addUser( event.getUser() );

        if ( BuX.getInstance().isRedisManagerEnabled() )
        {
            final IRedisDataManager redisDataManager = BuX.getInstance().getRedisManager().getDataManager();

            redisDataManager.loadRedisUser( event.getUser() );
        }
    }

    @Event
    public void onUnload( final UserUnloadEvent event )
    {
        event.getApi().removeUser( event.getUser() );

        if ( BuX.getInstance().isRedisManagerEnabled() )
        {
            final IRedisDataManager redisDataManager = BuX.getInstance().getRedisManager().getDataManager();

            redisDataManager.unloadRedisUser( event.getUser() );
        }
    }

    @Event
    public void onStaffLoad( final UserLoadEvent event )
    {
        final User user = event.getUser();

        StaffUtils.getStaffRankForUser( user ).ifPresent( rank ->
        {
            BuX.getInstance().getJobManager().executeJob( new ExecuteNetworkStaffEventJob(
                NetworkStaffJoinEvent.class,
                user.getName(),
                user.getUuid(),
                rank.getName()
            ) );
        } );
    }

    @Event
    public void onStaffUnload( UserUnloadEvent event )
    {
        final User user = event.getUser();

        StaffUtils.getStaffRankForUser( user ).ifPresent( rank ->
        {
            BuX.getInstance().getJobManager().executeJob( new ExecuteNetworkStaffEventJob(
                NetworkStaffLeaveEvent.class,
                user.getName(),
                user.getUuid(),
                rank.getName()
            ) );
        } );
    }
}