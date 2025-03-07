package com.gizmo.brennon.core.commands.plugin.sub;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.command.TabCall;
import com.gizmo.brennon.core.api.command.TabCompleter;
import com.gizmo.brennon.core.api.user.interfaces.User;

import java.util.List;

public class ReloadSubCommandCall implements CommandCall, TabCall
{

    @Override
    public List<String> onTabComplete( final User user, final String[] args )
    {
        return TabCompleter.empty();
    }

    @Override
    public void onExecute( final User user, final List<String> args, final List<String> parameters )
    {
        BuX.getInstance().reload();
        user.sendMessage( "&7All configuration files have been &areloaded&7!" );
    }

    @Override
    public String getDescription()
    {
        return "Reloads all configuration files and most systems of BungeeUtilisalsX.";
    }

    @Override
    public String getUsage()
    {
        return "/bungeeutilisals reload";
    }
}
