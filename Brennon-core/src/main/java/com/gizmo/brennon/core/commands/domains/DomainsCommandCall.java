package com.gizmo.brennon.core.commands.domains;

import com.gizmo.brennon.core.api.command.CommandBuilder;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.command.ParentCommand;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;

public class DomainsCommandCall extends ParentCommand implements CommandCall
{

    public DomainsCommandCall()
    {
        super( "general-commands.domains.help" );

        super.registerSubCommand(
            CommandBuilder.builder()
                .name( "list" )
                .fromSection( ConfigFiles.GENERALCOMMANDS.getConfig().getSection( "domains.subcommands.list" ) )
                .executable( new DomainsListSubCommandCall() )
                .build()
        );
        super.registerSubCommand(
            CommandBuilder.builder()
                .name( "search" )
                .fromSection( ConfigFiles.GENERALCOMMANDS.getConfig().getSection( "domains.subcommands.search" ) )
                .executable( new DomainsSearchSubCommandCall() )
                .build()
        );
    }

    @Override
    public String getDescription()
    {
        return "This command allows you to see on what domains users first joined. This might not work when behind another proxy.";
    }

    @Override
    public String getUsage()
    {
        return "/domains";
    }
}
