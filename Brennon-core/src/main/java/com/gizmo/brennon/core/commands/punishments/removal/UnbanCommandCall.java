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

public class UnbanCommandCall extends PunishmentCommand
{

    public UnbanCommandCall()
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
            user.sendLangMessage( "punishments.unban.usage" + ( useServerPunishments() ? "-server" : "" ) );
            return;
        }
        if ( !punishmentRemovalArgs.hasJoined() )
        {
            user.sendLangMessage( "never-joined" );
            return;
        }
        final UserStorage storage = punishmentRemovalArgs.getStorage();
        if ( !dao().getPunishmentDao().getBansDao().isBanned( storage.getUuid(), punishmentRemovalArgs.getServerOrAll() ).join() )
        {
            user.sendLangMessage( "punishments.unban.not-banned" );
            return;
        }

        if ( punishmentRemovalArgs.launchEvent( UserPunishRemoveEvent.PunishmentRemovalAction.UNBAN ) )
        {
            return;
        }

        final IPunishmentHelper executor = BuX.getApi().getPunishmentExecutor();
        dao().getPunishmentDao().getBansDao().removeCurrentBan(
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

        user.sendLangMessage( "punishments.unban.executed", executor.getPlaceHolders( info ) );

        if ( !parameters.contains( "-s" ) )
        {
            if ( parameters.contains( "-nbp" ) )
            {
                BuX.getApi().langBroadcast(
                    "punishments.unban.broadcast",
                    executor.getPlaceHolders( info )
                );
            }
            else
            {
                BuX.getApi().langPermissionBroadcast(
                    "punishments.unban.broadcast",
                    ConfigFiles.PUNISHMENT_CONFIG.getConfig().getString( "commands.unban.broadcast" ),
                    executor.getPlaceHolders( info )
                );
            }
        }
    }

    @Override
    public String getDescription()
    {
        return "Removes a ban for a given user.";
    }

    @Override
    public String getUsage()
    {
        return "/unban (user) <server>";
    }
}