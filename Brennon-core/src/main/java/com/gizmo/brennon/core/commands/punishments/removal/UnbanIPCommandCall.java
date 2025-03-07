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

public class UnbanIPCommandCall extends PunishmentCommand
{

    public UnbanIPCommandCall()
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
            user.sendLangMessage( "punishments.unbanip.usage" + ( useServerPunishments() ? "-server" : "" ) );
            return;
        }
        if ( !punishmentRemovalArgs.hasJoined() )
        {
            user.sendLangMessage( "never-joined" );
            return;
        }
        final UserStorage storage = punishmentRemovalArgs.getStorage();
        if ( !dao().getPunishmentDao().getBansDao().isIPBanned( storage.getIp(), punishmentRemovalArgs.getServerOrAll() ).join() )
        {
            user.sendLangMessage( "punishments.unbanip.not-banned" );
            return;
        }

        if ( punishmentRemovalArgs.launchEvent( UserPunishRemoveEvent.PunishmentRemovalAction.UNBANIP ) )
        {
            return;
        }
        final IPunishmentHelper executor = BuX.getApi().getPunishmentExecutor();
        dao().getPunishmentDao().getBansDao().removeCurrentIPBan(
            storage.getIp(),
            user.getName(),
            punishmentRemovalArgs.getServerOrAll()
        );

        final PunishmentInfo info = new PunishmentInfo();
        info.setUser( punishmentRemovalArgs.getPlayer() );
        info.setId( "-1" );
        info.setExecutedBy( user.getName() );
        info.setRemovedBy( user.getName() );
        info.setServer( punishmentRemovalArgs.getServerOrAll() );

        user.sendLangMessage( "punishments.unbanip.executed", executor.getPlaceHolders( info ) );

        if ( !parameters.contains( "-s" ) )
        {
            if ( parameters.contains( "-nbp" ) )
            {
                BuX.getApi().langBroadcast(
                    "punishments.unbanip.broadcast",
                    executor.getPlaceHolders( info )
                );
            }
            else
            {
                BuX.getApi().langPermissionBroadcast(
                    "punishments.unbanip.broadcast",
                    ConfigFiles.PUNISHMENT_CONFIG.getConfig().getString( "commands.unbanip.broadcast" ),
                    executor.getPlaceHolders( info )
                );
            }
        }
    }

    @Override
    public String getDescription()
    {
        return "Removes an IP ban for a given user / IP.";
    }

    @Override
    public String getUsage()
    {
        return "/unbanip (user / IP) <server>";
    }
}