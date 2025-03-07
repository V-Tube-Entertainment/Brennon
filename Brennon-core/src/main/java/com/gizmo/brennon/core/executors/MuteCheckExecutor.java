package com.gizmo.brennon.core.executors;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.event.event.Event;
import com.gizmo.brennon.core.api.event.event.EventExecutor;
import com.gizmo.brennon.core.api.event.event.Priority;
import com.gizmo.brennon.core.api.event.events.user.UserChatEvent;
import com.gizmo.brennon.core.api.event.events.user.UserCommandEvent;
import com.gizmo.brennon.core.api.punishments.PunishmentInfo;
import com.gizmo.brennon.core.api.punishments.PunishmentType;
import com.gizmo.brennon.core.api.storage.dao.punishments.MutesDao;
import com.gizmo.brennon.core.api.user.UserStorageKey;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.google.common.collect.Lists;

import java.util.List;

import static com.gizmo.brennon.core.api.storage.dao.PunishmentDao.useServerPunishments;

public class MuteCheckExecutor implements EventExecutor
{

    @Event
    public void onCommand( UserCommandEvent event )
    {
        final User user = event.getUser();

        if ( !isMuted( user, user.getServerName() ) )
        {
            return;
        }
        final PunishmentInfo info = getCurrentMuteForUser( user, user.getServerName() );
        if ( info == null || checkTemporaryMute( user, info ) )
        {
            return;
        }

        if ( ConfigFiles.PUNISHMENT_CONFIG.getConfig().getStringList( "blocked-mute-commands" )
            .contains( event.getActualCommand().replaceFirst( "/", "" ) ) )
        {

            user.sendLangMessage(
                "punishments." + info.getType().toString().toLowerCase() + ".onmute",
                event.getApi().getPunishmentExecutor().getPlaceHolders( info )
            );
            event.setCancelled( true );
        }
    }

    // high priority
    @Event( priority = Priority.HIGHEST )
    public void onChat( UserChatEvent event )
    {
        final User user = event.getUser();

        if ( !isMuted( user, user.getServerName() ) )
        {
            return;
        }
        final PunishmentInfo info = getCurrentMuteForUser( user, user.getServerName() );
        if ( info == null || checkTemporaryMute( user, info ) )
        {
            return;
        }

        user.sendLangMessage(
            "punishments." + info.getType().toString().toLowerCase() + ".onmute",
            event.getApi().getPunishmentExecutor().getPlaceHolders( info )
        );
        event.setCancelled( true );
    }

    private boolean checkTemporaryMute( final User user, final PunishmentInfo info )
    {
        if ( info.isExpired() )
        {
            final MutesDao mutesDao = BuX.getApi().getStorageManager().getDao().getPunishmentDao().getMutesDao();

            if ( info.getType().equals( PunishmentType.TEMPMUTE ) )
            {
                mutesDao.removeCurrentMute( user.getUuid(), "CONSOLE", info.getServer() );
            }
            else
            {
                mutesDao.removeCurrentIPMute( user.getIp(), "CONSOLE", info.getServer() );
            }
            return true;
        }
        return false;
    }

    private boolean isMuted( final User user, final String server )
    {
        return getCurrentMuteForUser( user, server ) != null;
    }

    private PunishmentInfo getCurrentMuteForUser( final User user, final String server )
    {
        if ( !user.getStorage().hasData( UserStorageKey.CURRENT_MUTES ) )
        {
            // mutes seem to not have loaded yet, loading them now ...
            final MutesDao dao = BuX.getApi().getStorageManager().getDao().getPunishmentDao().getMutesDao();
            final List<PunishmentInfo> mutes = Lists.newArrayList();

            mutes.addAll( dao.getActiveMutes( user.getUuid() ).join() );
            mutes.addAll( dao.getActiveIPMutes( user.getIp() ).join() );

            user.getStorage().setData( UserStorageKey.CURRENT_MUTES, mutes );
        }
        final List<PunishmentInfo> mutes = user.getStorage().getData( UserStorageKey.CURRENT_MUTES );
        if ( mutes.isEmpty() )
        {
            return null;
        }

        if ( useServerPunishments() )
        {
            return mutes.stream()
                .filter( mute -> mute.getServer().equalsIgnoreCase( "ALL" ) || mute.getServer().equalsIgnoreCase( server ) )
                .findAny()
                .orElse( null );
        }
        else
        {
            return mutes.get( 0 );
        }
    }
}