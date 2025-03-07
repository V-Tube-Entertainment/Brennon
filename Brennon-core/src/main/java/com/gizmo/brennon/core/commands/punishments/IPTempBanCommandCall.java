package com.gizmo.brennon.core.commands.punishments;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.punishments.IPunishmentHelper;
import com.gizmo.brennon.core.api.punishments.PunishmentType;
import com.gizmo.brennon.core.api.user.UserStorage;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;

import java.util.List;

public class IPTempBanCommandCall extends PunishmentCommand
{

    public IPTempBanCommandCall()
    {
        super( "punishments.iptempban", true );
    }

    @Override
    public void onPunishmentExecute( final User user, final List<String> args, final List<String> parameters, final PunishmentArgs punishmentArgs )
    {
        final String reason = punishmentArgs.getReason();
        final UserStorage storage = punishmentArgs.getStorage();
        final long time = punishmentArgs.getTime();

        if ( time == 0L )
        {
            user.sendLangMessage( "punishments.iptempban.non-valid" );
            return;
        }
        if ( dao().getPunishmentDao().getBansDao().isIPBanned( storage.getIp(), punishmentArgs.getServerOrAll() ).join() )
        {
            user.sendLangMessage( "punishments.iptempban.already-banned" );
            return;
        }
        if ( punishmentArgs.launchEvent( PunishmentType.IPTEMPBAN ) )
        {
            return;
        }
        final IPunishmentHelper executor = BuX.getApi().getPunishmentExecutor();
        dao().getPunishmentDao().getBansDao().insertTempIPBan(
            storage.getUuid(),
            storage.getUserName(),
            storage.getIp(),
            reason,
            punishmentArgs.getServerOrAll(),
            true,
            user.getName(),
            time
        ).thenAccept( info ->
        {
            // Attempting to kick if player is online. If briding is enabled and player is not online, it will attempt to kick on other bungee's.
            super.attemptKick( storage, "punishments.iptempban.kick", info );

            user.sendLangMessage( "punishments.iptempban.executed", executor.getPlaceHolders( info ) );

            if ( !parameters.contains( "-s" ) )
            {
                if ( parameters.contains( "-nbp" ) )
                {
                    BuX.getApi().langBroadcast(
                        "punishments.iptempban.broadcast",
                        executor.getPlaceHolders( info )
                    );
                }
                else
                {
                    BuX.getApi().langPermissionBroadcast(
                        "punishments.iptempban.broadcast",
                        ConfigFiles.PUNISHMENT_CONFIG.getConfig().getString( "commands.iptempban.broadcast" ),
                        executor.getPlaceHolders( info )
                    );
                }
            }

            punishmentArgs.launchPunishmentFinishEvent( PunishmentType.IPTEMPBAN );
        } );
    }

    @Override
    public String getDescription()
    {
        return "Temporarily ipbans a given user from the network (or given server if per-server punishments are enabled).";
    }

    @Override
    public String getUsage()
    {
        return "/iptempban (user / ip) (time) <server> (reason)";
    }
}