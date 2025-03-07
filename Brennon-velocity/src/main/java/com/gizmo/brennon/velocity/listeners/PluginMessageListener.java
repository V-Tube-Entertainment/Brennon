package com.gizmo.brennon.velocity.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.event.events.user.UserPluginMessageReceiveEvent;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.velocity.Bootstrap;

import java.util.Optional;

public class PluginMessageListener
{

    private static final MinecraftChannelIdentifier MAIN_CHANNEL = MinecraftChannelIdentifier.create( "bux", "main" );

    public PluginMessageListener()
    {
        Bootstrap.getInstance().getProxyServer().getChannelRegistrar().register( MAIN_CHANNEL );
    }

    @Subscribe
    public void onMainPluginMessage( final PluginMessageEvent event )
    {
        if ( !event.getIdentifier().equals( MAIN_CHANNEL ) )
        {
            return;
        }
        if ( !( event.getTarget() instanceof Player ) )
        {
            return;
        }
        final Optional<User> optionalUser = BuX.getApi().getUser( ( (Player) event.getTarget() ).getUniqueId() );
        if ( optionalUser.isEmpty() )
        {
            return;
        }
        final UserPluginMessageReceiveEvent userPluginMessageReceiveEvent = new UserPluginMessageReceiveEvent(
                optionalUser.get(),
                event.getIdentifier().getId(),
                event.getData().clone()
        );
        BuX.getApi().getEventLoader().launchEventAsync( userPluginMessageReceiveEvent );
    }
}
