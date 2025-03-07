package com.gizmo.brennon.core.commands;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.configs.ServerBalancerConfig.ServerBalancerGroup;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ServerBalancerCommandCall implements CommandCall
{

    private final ServerBalancerGroup serverBalancerGroup;

    @Override
    public void onExecute( User user, List<String> args, List<String> parameters )
    {
        String serverToIgnore = null;

        if ( serverBalancerGroup.getServerGroup().isInGroup( user.getServerName() ) )
        {
            if ( !serverBalancerGroup.isAllowSendingToOtherServers() )
            {
                user.sendLangMessage( "server-balancer.command.already-in-group" );
                return;
            }
            else
            {
                serverToIgnore = user.getServerName();
            }
        }

        BuX.getApi().getServerBalancer().getOptimalServer( serverBalancerGroup, serverToIgnore )
            .ifPresentOrElse( server ->
            {
                user.sendToServer( server );

                user.sendLangMessage(
                    "server-balancer.command.sending",
                    MessagePlaceholders.create()
                        .append( "groupName", serverBalancerGroup.getServerGroup().getName() )
                        .append( "serverName", server.getName() )
                );
            }, () -> user.sendLangMessage(
                "server-balancer.command.no-servers-available",
                MessagePlaceholders.create().append( "groupName", serverBalancerGroup.getServerGroup().getName() )
            ) );
    }

    @Override
    public String getDescription()
    {
        return "Sends the player to a server balancer group.";
    }

    @Override
    public String getUsage()
    {
        return "/(groupname/command) [player]";
    }
}
