package com.gizmo.brennon.core.executors;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.event.event.Event;
import com.gizmo.brennon.core.api.event.event.EventExecutor;
import com.gizmo.brennon.core.api.event.events.user.UserCommandEvent;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.config.configs.CommandBlockerConfig.BlockedCommand;
import com.gizmo.brennon.core.api.utils.config.configs.CommandBlockerConfig.BlockedSubCommand;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;
import com.google.common.base.Strings;

public class UserCommandExecutor implements EventExecutor
{

    @Event
    public void onCommand( final UserCommandEvent event )
    {
        if ( !ConfigFiles.COMMAND_BLOCKER.isEnabled() )
        {
            return;
        }

        final String command = event.getActualCommand().replaceFirst( "/", "" );

        if ( isBlocked( event.getUser(), command, event.getArguments() ) )
        {
            event.setCancelled( true );
            event.getUser().sendLangMessage(
                "command-disabled",
                MessagePlaceholders.create()
                    .append( "command", command )
            );
        }
    }

    @Event
    public void onListenerCommand( final UserCommandEvent event )
    {
        if ( !ConfigFiles.COMMAND_BLOCKER.isEnabled() )
        {
            return;
        }

        final String commandName = event.getActualCommand().replaceFirst( "/", "" );

        BuX.getInstance().getCommandManager().findCommandByName( commandName ).ifPresent( command ->
        {
            if ( command.isListenerBased() && !command.isDisabledInServer( event.getUser().getServerName() ) )
            {
                BuX.debug( "Executing listener command " + commandName );
                event.setCancelled( true );
                command.execute( event.getUser(), event.getArguments() );
            }
        } );
    }

    private boolean isBlocked( final User user, final String command, final String[] args )
    {
        final boolean blacklist = ConfigFiles.COMMAND_BLOCKER.getConfig().getString( "type" ).equalsIgnoreCase( "BLACKLIST" );

        for ( BlockedCommand blockedCommand : ConfigFiles.COMMAND_BLOCKER.getBlockedCommands() )
        {
            if ( !Strings.isNullOrEmpty( user.getServerName() )
                && !blockedCommand.getServers().isEmpty()
                && ( blacklist ? blockedCommand.getServers().stream().noneMatch( s -> s.isInGroup( user.getServerName() ) )
                : blockedCommand.getServers().stream().anyMatch( s -> s.isInGroup( user.getServerName() ) ) ) )
            {
                continue;
            }
            if ( !Strings.isNullOrEmpty( blockedCommand.getBypassPermission() ) && user.hasPermission( blockedCommand.getBypassPermission() ) )
            {
                continue;
            }
            if ( !blockedCommand.getCommand().equalsIgnoreCase( command ) )
            {
                continue;
            }

            if ( blockedCommand.getSubCommands().isEmpty() )
            {
                return true;
            }
            else
            {
                for ( BlockedSubCommand blockedSubCommand : blockedCommand.getSubCommands() )
                {
                    if ( args.length < blockedSubCommand.getIndex() )
                    {
                        continue;
                    }
                    if ( blockedSubCommand.getCommand().equalsIgnoreCase( args[blockedSubCommand.getIndex() - 1] ) )
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
