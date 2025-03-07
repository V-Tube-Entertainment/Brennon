package com.gizmo.brennon.core.commands.punishments;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.punishments.PunishmentInfo;
import com.gizmo.brennon.core.api.punishments.PunishmentType;
import com.gizmo.brennon.core.api.storage.dao.Dao;
import com.gizmo.brennon.core.api.storage.dao.PunishmentDao;
import com.gizmo.brennon.core.api.user.UserStorage;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.gizmo.brennon.core.api.storage.dao.PunishmentDao.useServerPunishments;

public class PunishmentInfoCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( useServerPunishments() )
        {
            if ( args.size() < 2 )
            {
                user.sendLangMessage( "punishments.punishmentinfo.usage-server" );
                return;
            }
        }
        else
        {
            if ( args.size() < 1 )
            {
                user.sendLangMessage( "punishments.punishmentinfo.usage" );
                return;
            }
        }

        final Dao dao = BuX.getApi().getStorageManager().getDao();
        final String username = args.get( 0 );
        final String server = useServerPunishments() ? args.get( 1 ) : null;

        dao.getUserDao().getUserData( username ).thenAccept( optionalStorage ->
        {
            final UserStorage storage = optionalStorage.orElse( null );
            if ( storage == null || !storage.isLoaded() )
            {
                user.sendLangMessage( "never-joined" );
                return;
            }

            final String action;

            if ( useServerPunishments() )
            {
                action = args.size() > 2 ? args.get( 2 ) : "all";
            }
            else
            {
                action = args.size() > 1 ? args.get( 1 ) : "all";
            }

            if ( action.equalsIgnoreCase( "all" ) )
            {
                for ( PunishmentType type : PunishmentType.values() )
                {
                    if ( type.equals( PunishmentType.KICK ) || type.equals( PunishmentType.WARN ) )
                    {
                        continue;
                    }
                    sendTypeInfo( user, storage, server, type );
                }
            }
            else
            {
                for ( String typeStr : action.split( "," ) )
                {
                    final PunishmentType type = Utils.valueOfOr( typeStr.toUpperCase(), PunishmentType.BAN );
                    if ( type.equals( PunishmentType.KICK ) || type.equals( PunishmentType.WARN ) )
                    {
                        continue;
                    }
                    sendTypeInfo( user, storage, server, type );
                }
            }
        } );
    }

    @Override
    public String getDescription()
    {
        return "Shows you the current status for a specific punishment type or all punishment types for a specific user.";
    }

    @Override
    public String getUsage()
    {
        return "/punishmentinfo (user) <server> [type / all]";
    }

    private void sendTypeInfo( final User user, final UserStorage storage, String server, final PunishmentType type )
    {
        if ( !useServerPunishments() || server == null )
        {
            server = "ALL";
        }
        final PunishmentDao dao = BuX.getApi().getStorageManager().getDao().getPunishmentDao();

        if ( !type.isEnabled() )
        {
            return;
        }

        final CompletableFuture<PunishmentInfo> task;
        switch ( type )
        {
            case BAN:
            case TEMPBAN:
                task = dao.getBansDao().getCurrentBan( storage.getUuid(), server );
                break;
            case IPBAN:
            case IPTEMPBAN:
                task = dao.getBansDao().getCurrentIPBan( storage.getIp(), server );
                break;
            case MUTE:
            case TEMPMUTE:
                task = dao.getMutesDao().getCurrentMute( storage.getUuid(), server );
                break;
            case IPMUTE:
            case IPTEMPMUTE:
                task = dao.getMutesDao().getCurrentIPMute( storage.getIp(), server );
                break;
            default:
                task = null;
                break;
        }

        if ( task != null )
        {
            task.thenAccept( info -> sendInfoMessage( user, storage, type, info ) );
        }
    }

    private void sendInfoMessage( final User user,
                                  final UserStorage storage,
                                  final PunishmentType type,
                                  final PunishmentInfo info )
    {
        if ( info != null )
        {
            user.sendLangMessage(
                "punishments.punishmentinfo.typeinfo.found",
                BuX.getApi().getPunishmentExecutor().getPlaceHolders( info )
            );
        }
        else
        {
            user.sendLangMessage(
                "punishments.punishmentinfo.typeinfo.notfound",
                MessagePlaceholders.create()
                    .append( "user", storage.getUserName() )
                    .append( "type", type.toString().toLowerCase() )
            );
        }
    }
}
