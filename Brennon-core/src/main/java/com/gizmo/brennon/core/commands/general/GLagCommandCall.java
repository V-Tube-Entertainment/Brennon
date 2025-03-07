package com.gizmo.brennon.core.commands.general;

import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.command.TabCall;
import com.gizmo.brennon.core.api.command.TabCompleter;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.Utils;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.List;

public class GLagCommandCall implements CommandCall, TabCall
{

    @Override
    public List<String> onTabComplete( User user, String[] args )
    {
        return TabCompleter.empty();
    }

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        final long uptime = ManagementFactory.getRuntimeMXBean().getStartTime();

        user.sendLangMessage(
            "general-commands.glag",
            MessagePlaceholders.create()
                .append( "maxmemory", ( Runtime.getRuntime().maxMemory() / 1024 / 1024 ) + " MB" )
                .append( "freememory", ( Runtime.getRuntime().freeMemory() / 1024 / 1024 ) + " MB" )
                .append( "totalmemory", ( Runtime.getRuntime().totalMemory() / 1024 / 1024 ) + " MB" )
                .append( "onlinesince", Utils.formatDate( new Date( uptime ), user.getLanguageConfig().getConfig() ) )
        );
    }

    @Override
    public String getDescription()
    {
        return "Gives you some basic information about the current proxy (online time and memory usage).";
    }

    @Override
    public String getUsage()
    {
        return "/glag";
    }
}
