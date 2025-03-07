package com.gizmo.brennon.core.executors;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.event.event.Event;
import com.gizmo.brennon.core.api.event.event.EventExecutor;
import com.gizmo.brennon.core.api.event.events.punishment.UserPunishmentFinishEvent;
import com.gizmo.brennon.core.api.punishments.PunishmentAction;
import com.gizmo.brennon.core.api.punishments.PunishmentInfo;
import com.gizmo.brennon.core.api.user.UserStorageKey;
import com.gizmo.brennon.core.api.utils.ReportUtils;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserPunishExecutor implements EventExecutor
{

    @Event
    public void handleReports( UserPunishmentFinishEvent event )
    {
        BuX.getInstance().getScheduler().runAsync( () ->
            ReportUtils.handleReportsFor( event.getExecutor().getName(), event.getUuid(), event.getType() )
        );
    }

    @Event
    public void updateMute( UserPunishmentFinishEvent event )
    {
        if ( event.isMute() )
        {
            event.getUser().ifPresent( user ->
            {
                if ( !user.getStorage().hasData( UserStorageKey.CURRENT_MUTES ) )
                {
                    user.getStorage().setData( UserStorageKey.CURRENT_MUTES, Lists.newArrayList() );
                }
                final List<PunishmentInfo> mutes = user.getStorage().getData( UserStorageKey.CURRENT_MUTES );

                mutes.add( event.getInfo() );
            } );
        }
    }

    @Event
    public void executeActions( UserPunishmentFinishEvent event )
    {
        if ( !ConfigFiles.PUNISHMENT_ACTIONS.getPunishmentActions().containsKey( event.getType() ) )
        {
            return;
        }
        final List<PunishmentAction> actions = ConfigFiles.PUNISHMENT_ACTIONS.getPunishmentActions().get( event.getType() );

        for ( PunishmentAction action : actions )
        {
            this.getPunishmentAmount( event, action ).thenAccept( amount ->
            {
                if ( amount >= action.getLimit() )
                {
                    action.getActions().forEach( command ->
                        BuX.getApi().getConsoleUser().executeCommand( command.replace( "%user%", event.getName() ) )
                    );

                    if ( event.isUserPunishment() )
                    {
                        BuX.getApi().getStorageManager().getDao().getPunishmentDao().updateActionStatus(
                            action.getLimit(),
                            event.getType(),
                            event.getUuid(),
                            new Date( System.currentTimeMillis() - action.getUnit().toMillis( action.getTime() ) )
                        );
                    }
                    else
                    {
                        BuX.getApi().getStorageManager().getDao().getPunishmentDao().updateIPActionStatus(
                            action.getLimit(),
                            event.getType(),
                            event.getIp(),
                            new Date( System.currentTimeMillis() - action.getUnit().toMillis( action.getTime() ) )
                        );
                    }

                    BuX.getApi().getStorageManager().getDao().getPunishmentDao().savePunishmentAction(
                        event.getUuid(),
                        event.getName(),
                        event.getIp(),
                        action.getUid()
                    );
                }
            } );
        }
    }

    private CompletableFuture<Long> getPunishmentAmount( final UserPunishmentFinishEvent event, final PunishmentAction action )
    {
        if ( event.isUserPunishment() )
        {
            // uuid involved
            return BuX.getApi().getStorageManager().getDao().getPunishmentDao().getPunishmentsSince(
                event.getType(),
                event.getUuid(),
                new Date( System.currentTimeMillis() - action.getUnit().toMillis( action.getTime() ) )
            );
        }
        else
        {
            // ip involved
            return BuX.getApi().getStorageManager().getDao().getPunishmentDao().getIPPunishmentsSince(
                event.getType(),
                event.getIp(),
                new Date( System.currentTimeMillis() - action.getUnit().toMillis( action.getTime() ) )
            );
        }
    }
}