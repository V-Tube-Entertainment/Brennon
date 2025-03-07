package com.gizmo.brennon.core.commands.general;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.server.IProxyServer;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.other.StaffRankData;
import com.gizmo.brennon.core.api.utils.other.StaffUser;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;
import com.gizmo.brennon.core.api.utils.text.MessageBuilder;
import com.google.common.collect.Lists;
import dev.endoy.configuration.api.IConfiguration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StaffCommandCall implements CommandCall
{

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        if ( !args.isEmpty() )
        {
            final IConfiguration config = ConfigFiles.GENERALCOMMANDS.getConfig();

            // Big yikes code, but works for now
            final List<String> commands = Arrays.asList( config.getString( "staff.toggle.aliases" ).split( ", " ) );

            if ( config.getBoolean( "staff.toggle.enabled" )
                && ( config.getString( "staff.toggle.name" ).equalsIgnoreCase( args.get( 0 ) ) || commands.contains( args.get( 0 ) ) )
                && user.hasPermission( config.getString( "staff.toggle.permission" ) ) )
            {
                StaffUser staffUser = null;

                for ( StaffUser su : BuX.getInstance().getStaffMembers() )
                {
                    if ( su.getUuid().equals( user.getUuid() ) )
                    {
                        staffUser = su;
                    }
                }

                if ( staffUser != null )
                {
                    if ( staffUser.isHidden() )
                    {
                        staffUser.setHidden( false );
                        user.sendLangMessage( "general-commands.staff.toggle.unhidden" );
                    }
                    else
                    {
                        staffUser.setHidden( true );
                        user.sendLangMessage( "general-commands.staff.toggle.hidden" );
                    }
                    return;
                }
            }
        }

        final List<StaffUser> staffUsers = BuX.getInstance().getStaffMembers()
            .stream()
            .filter( staffUser -> !staffUser.isHidden() && !staffUser.isVanished() )
            .toList();

        if ( staffUsers.isEmpty() )
        {
            user.sendLangMessage( "general-commands.staff.no_staff" );
            return;
        }

        final Map<String, List<StaffUser>> staffMembers = staffUsers
            .stream()
            .collect( Collectors.groupingBy( it -> it.getRank().getName() ) );

        final LinkedList<StaffRankData> onlineStaffRanks = staffMembers
            .keySet()
            .stream()
            .map( it -> ConfigFiles.RANKS.getRankData( it ) )
            .filter( Objects::nonNull )
            .sorted( Comparator.comparingInt( StaffRankData::getPriority ) )
            .collect( Collectors.toCollection( Lists::newLinkedList ) );

        user.sendLangMessage( "general-commands.staff.head", MessagePlaceholders.create().append( "total", staffUsers.size() ) );

        for ( StaffRankData rank : onlineStaffRanks )
        {
            List<StaffUser> users = staffMembers.get( rank.getName() );
            Component originalComponent = MessageBuilder.buildMessage(
                user,
                user.getLanguageConfig().getConfig().getSection( "general-commands.staff.rank" ),
                MessagePlaceholders.create()
                    .append( "rank_displayname", rank.getDisplay() )
                    .append( "amount_online", users.size() )
                    .append( "total", staffUsers.size() )
            );

            Component replacedComponent = this.replaceAndRebuild( originalComponent, () ->
            {
                String languagePathPrefix = user.getLanguageConfig().getConfig().exists( "general-commands.staff.users." + rank.getName().toLowerCase() )
                        ? "general-commands.staff.users." + rank.getName().toLowerCase()
                        : "general-commands.staff.users";

                TextComponent.Builder builder = Component.text();

                users.sort( Comparator.comparing( StaffUser::getName ) );
                final Iterator<StaffUser> userIt = users.iterator();

                while ( userIt.hasNext() )
                {
                    final StaffUser u = userIt.next();
                    final IProxyServer info = BuX.getApi().getPlayerUtils().findPlayer( u.getName() );

                    builder.append( MessageBuilder.buildMessage(
                        user,
                        user.getLanguageConfig().getConfig().getSection( languagePathPrefix + ".user" ),
                        MessagePlaceholders.create()
                            .append( "username", u.getName() )
                            .append( "server", info == null ? "Unknown" : info.getName() )
                    ) );

                    if ( userIt.hasNext() )
                    {
                        builder.append( Utils.format( user.getLanguageConfig().buildLangMessage(
                            languagePathPrefix + ".separator",
                            MessagePlaceholders.empty()
                        ) ) );
                    }
                }

                return builder.build();
            } );

            user.sendMessage( replacedComponent );
        }

        user.sendLangMessage( "general-commands.staff.foot", MessagePlaceholders.create().append( "total", staffUsers.size() ) );
    }

    @Override
    public String getDescription()
    {
        return "Lists online staff by rank.";
    }

    @Override
    public String getUsage()
    {
        return "/staff";
    }

    private TextComponent replaceAndRebuild( Component original, Supplier<TextComponent> userComponentSupplier )
    {
        TextComponent.Builder componentBuilder = Component.text();

        original.children()
            .stream()
            .filter( c -> c instanceof TextComponent )
            .map( c -> (TextComponent) c )
            .forEach( textComponent ->
            {
                TextComponent.Builder subComponent = Component.text();

                if ( !textComponent.children().isEmpty() )
                {
                    subComponent.append( replaceAndRebuild( textComponent, userComponentSupplier ) );
                }
                else if ( textComponent.content().contains( "{users}" ) )
                {
                    subComponent.append( userComponentSupplier.get() );
                }
                else
                {
                    subComponent.append( textComponent );
                }

                componentBuilder.append( subComponent );
            } );

        return componentBuilder.build();
    }
}
