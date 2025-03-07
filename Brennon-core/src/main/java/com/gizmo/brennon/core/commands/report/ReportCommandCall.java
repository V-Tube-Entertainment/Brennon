package com.gizmo.brennon.core.commands.report;

import com.gizmo.brennon.core.api.command.CommandBuilder;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.command.ParentCommand;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.commands.report.sub.*;

public class ReportCommandCall extends ParentCommand implements CommandCall
{

    public ReportCommandCall()
    {
        super( "general-commands.report.help" );

        super.registerSubCommand(
            CommandBuilder.builder()
                .name( "create" )
                .fromSection( ConfigFiles.GENERALCOMMANDS.getConfig().getSection( "report.subcommands.create" ) )
                .executable( new ReportCreateSubCommandCall() )
                .build()
        );
        super.registerSubCommand(
            CommandBuilder.builder()
                .name( "list" )
                .fromSection( ConfigFiles.GENERALCOMMANDS.getConfig().getSection( "report.subcommands.list" ) )
                .executable( new ReportListSubCommandCall() )
                .build()
        );
        super.registerSubCommand(
            CommandBuilder.builder()
                .name( "accept" )
                .fromSection( ConfigFiles.GENERALCOMMANDS.getConfig().getSection( "report.subcommands.accept" ) )
                .executable( new ReportAcceptSubCommandCall() )
                .build()
        );
        super.registerSubCommand(
            CommandBuilder.builder()
                .name( "deny" )
                .fromSection( ConfigFiles.GENERALCOMMANDS.getConfig().getSection( "report.subcommands.deny" ) )
                .executable( new ReportDenySubCommandCall() )
                .build()
        );
        super.registerSubCommand(
            CommandBuilder.builder()
                .name( "history" )
                .fromSection( ConfigFiles.GENERALCOMMANDS.getConfig().getSection( "report.subcommands.history" ) )
                .executable( new ReportHistorySubCommandCall() )
                .build()
        );
    }

    @Override
    public String getDescription()
    {
        return "This command sends a list of available report commands.";
    }

    @Override
    public String getUsage()
    {
        return "/report";
    }
}