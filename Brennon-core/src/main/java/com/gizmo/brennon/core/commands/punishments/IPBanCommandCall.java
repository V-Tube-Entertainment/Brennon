package com.gizmo.brennon.core.commands.punishments;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.punishments.IPunishmentHelper;
import com.gizmo.brennon.core.api.punishments.PunishmentType;
import com.gizmo.brennon.core.api.user.UserStorage;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;

import java.util.List;

public class IPBanCommandCall extends PunishmentCommand
{

    public IPBanCommandCall()
    {
        super( "punishments.ipban", false );
    }

    @Override
    public void onPunishmentExecute( final User user, final List<String> args, final List<String> parameters, final PunishmentArgs punishmentArgs )
    {
        final String reason = punishmentArgs.getReason();
        final UserStorage storage = punishmentArgs.getStorage();

        if ( dao().getPunishmentDao().getBansDao().isIPBanned( storage.getIp(), punishmentArgs.getServerOrAll() ).join() )
        {
            user.sendLangMessage( "punishments.ipban.already-banned" );
            return;
        }

        if ( punishmentArgs.launchEvent( PunishmentType.IPBAN ) )
        {
            return;
        }
        final IPunishmentHelper executor = BuX.getApi().getPunishmentExecutor();
        BuX.getApi().getStorageManager().getDao().getPunishmentDao().getBansDao().insertIPBan(
            storage.getUuid(),
            storage.getUserName(),
            storage.getIp(),
            reason,
            punishmentArgs.getServerOrAll(),
            true,
            user.getName()
        ).thenAccept( info ->
        {
            // Attempting to kick if player is online. If briding is enabled and player is not online, it will attempt to kick on other bungee's.
            super.attemptKick( storage, "punishments.ipban.kick", info );
            user.sendLangMessage( "punishments.ipban.executed", executor.getPlaceHolders( info ) );

            if ( !parameters.contains( "-s" ) )
            {
                if ( parameters.contains( "-nbp" ) )
                {
                    BuX.getApi().langBroadcast(
                        "punishments.ipban.broadcast",
                        executor.getPlaceHolders( info )
                    );
                }
                else
                {
                    BuX.getApi().langPermissionBroadcast(
                        "punishments.ipban.broadcast",
                        ConfigFiles.PUNISHMENT_CONFIG.getConfig().getString( "commands.ipban.broadcast" ),
                        executor.getPlaceHolders( info )
                    );
                }
            }

            punishmentArgs.launchPunishmentFinishEvent( PunishmentType.IPBAN );
        } );
    }

    @Override
    public String getDescription()
    {
        return "Permanently ipbans a given user from the network (or given server if per-server punishments are enabled).";
    }

    @Override
    public String getUsage()
    {
        return "/ipban (user / ip) <server> (reason)";
    }
}