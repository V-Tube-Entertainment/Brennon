package com.gizmo.brennon.core.commands.punishments.removal;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.event.events.punishment.UserPunishRemoveEvent;
import com.gizmo.brennon.core.api.punishments.IPunishmentHelper;
import com.gizmo.brennon.core.api.punishments.PunishmentInfo;
import com.gizmo.brennon.core.api.user.UserStorage;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.commands.punishments.PunishmentCommand;

import java.util.List;

public class UnmuteCommandCall extends PunishmentCommand
{

    public UnmuteCommandCall()
    {
        super( null, false );
    }

    @Override
    public void onPunishmentExecute( final User user, final List<String> args, final List<String> parameters, final PunishmentArgs punishmentArgs )
    {
        // do nothing
    }

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        final PunishmentRemovalArgs punishmentRemovalArgs = loadRemovalArguments( user, args );

        if ( punishmentRemovalArgs == null )
        {
            user.sendLangMessage( "punishments.unmute.usage" + ( useServerPunishments() ? "-server" : "" ) );
            return;
        }
        if ( !punishmentRemovalArgs.hasJoined() )
        {
            user.sendLangMessage( "never-joined" );
            return;
        }
        final UserStorage storage = punishmentRemovalArgs.getStorage();
        if ( !dao().getPunishmentDao().getMutesDao().isMuted( storage.getUuid(), punishmentRemovalArgs.getServerOrAll() ).join() )
        {
            user.sendLangMessage( "punishments.unmute.not-muted" );
            return;
        }

        if ( punishmentRemovalArgs.launchEvent( UserPunishRemoveEvent.PunishmentRemovalAction.UNMUTE ) )
        {
            return;
        }

        final IPunishmentHelper executor = BuX.getApi().getPunishmentExecutor();
        dao().getPunishmentDao().getMutesDao().removeCurrentMute(
            storage.getUuid(),
            user.getName(),
            punishmentRemovalArgs.getServerOrAll()
        );

        final PunishmentInfo info = new PunishmentInfo();
        info.setUser( punishmentRemovalArgs.getPlayer() );
        info.setId( "-1" );
        info.setExecutedBy( user.getName() );
        info.setRemovedBy( user.getName() );
        info.setServer( punishmentRemovalArgs.getServerOrAll() );

        punishmentRemovalArgs.removeCachedMute();

        user.sendLangMessage( "punishments.unmute.executed", executor.getPlaceHolders( info ) );

        if ( !parameters.contains( "-s" ) )
        {
            if ( parameters.contains( "-nbp" ) )
            {
                BuX.getApi().langBroadcast(
                    "punishments.unmute.broadcast",
                    executor.getPlaceHolders( info )
                );
            }
            else
            {
                BuX.getApi().langPermissionBroadcast(
                    "punishments.unmute.broadcast",
                    ConfigFiles.PUNISHMENT_CONFIG.getConfig().getString( "commands.unmute.broadcast" ),
                    executor.getPlaceHolders( info )
                );
            }
        }
    }

    @Override
    public String getDescription()
    {
        return "Removes a mute for a given user.";
    }

    @Override
    public String getUsage()
    {
        return "/unmute (user) <server>";
    }
}