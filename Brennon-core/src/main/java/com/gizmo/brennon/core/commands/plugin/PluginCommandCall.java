package com.gizmo.brennon.core.commands.plugin;

import com.gizmo.brennon.core.api.command.CommandBuilder;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.command.ParentCommand;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.commands.plugin.sub.ReloadSubCommandCall;
import com.gizmo.brennon.core.commands.plugin.sub.VersionSubCommandCall;

public class PluginCommandCall extends ParentCommand implements CommandCall
{

    public PluginCommandCall()
    {
        super( "general-commands.bungeeutilisals.help" );

        registerSubCommand(
            CommandBuilder.builder()
                .name( "version" )
                .fromSection( ConfigFiles.GENERALCOMMANDS.getConfig().getSection( "bungeeutilisals.subcommands.version" ) )
                .executable( new VersionSubCommandCall() )
                .build()
        );

        registerSubCommand(
            CommandBuilder.builder()
                .name( "reload" )
                .fromSection( ConfigFiles.GENERALCOMMANDS.getConfig().getSection( "bungeeutilisals.subcommands.reload" ) )
                .executable( new ReloadSubCommandCall() )
                .build()
        );
    }

    @Override
    public String getDescription()
    {
        return "The default / admin command to help manage the plugin.";
    }

    @Override
    public String getUsage()
    {
        return "/bungeeutilisals";
    }
}
