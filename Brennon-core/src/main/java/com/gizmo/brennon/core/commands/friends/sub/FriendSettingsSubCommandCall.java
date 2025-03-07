package com.gizmo.brennon.core.commands.friends.sub;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.friends.FriendSetting;
import com.gizmo.brennon.core.api.friends.FriendSettings;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FriendSettingsSubCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( args.size() == 0 )
        {
            final FriendSettings settings = user.getFriendSettings();
            user.sendLangMessage( "friends.settings.noargs.header" );

            for ( FriendSetting setting : FriendSetting.getEnabledSettings() )
            {
                user.sendLangMessage(
                    "friends.settings.noargs.format",
                    MessagePlaceholders.create()
                        .append( "type", setting.getName( user.getLanguageConfig().getConfig() ) )
                        .append( "unformatted-type", setting.toString() )
                        .append( "status", user.getLanguageConfig().getConfig().getString( "friends.settings.noargs." + ( settings.getSetting( setting ) ? "enabled" : "disabled" ) ) )
                );
            }

            user.sendLangMessage( "friends.settings.noargs.footer" );
        }
        else if ( args.size() == 2 )
        {
            final FriendSetting type = Utils.valueOfOr( FriendSetting.class, args.get( 0 ).toUpperCase(), null );

            if ( type == null )
            {
                final String settings = Stream.of( FriendSetting.values() )
                    .map( Enum::toString )
                    .collect( Collectors.joining() );

                user.sendLangMessage( "friends.settings.invalid", MessagePlaceholders.create().append( "settings", settings ) );
                return;
            }
            final boolean value = args.get( 1 ).contains( "toggle" )
                ? !user.getFriendSettings().getSetting( type )
                : !args.get( 1 ).toLowerCase().contains( "d" );

            user.getFriendSettings().set( type, value );
            BuX.getApi().getStorageManager().getDao().getFriendsDao().setSetting( user.getUuid(), type, value );

            user.sendLangMessage(
                "friends.settings.updated",
                MessagePlaceholders.create()
                    .append( "type", type.toString().toLowerCase() )
                    .append( "value", value )
            );
        }
        else
        {
            user.sendLangMessage( "friends.settings.usage" );
        }
    }

    @Override
    public String getDescription()
    {
        return "Updates a setting value for one of the existing setting types.";
    }

    @Override
    public String getUsage()
    {
        return "/friend settings [setting] [value]";
    }
}
