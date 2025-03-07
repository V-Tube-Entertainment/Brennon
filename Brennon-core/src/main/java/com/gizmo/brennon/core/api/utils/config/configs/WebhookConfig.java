package com.gizmo.brennon.core.api.utils.config.configs;

import com.gizmo.brennon.core.api.utils.config.Config;
import dev.endoy.configuration.api.ISection;
import lombok.Getter;

public class WebhookConfig extends Config
{

    @Getter
    private DiscordWebhookConfig discordWebhook;

    public WebhookConfig( String location )
    {
        super( location );
    }

    @Override
    public void purge()
    {
        discordWebhook = null;
    }

    @Override
    public void setup()
    {
        if ( config == null )
        {
            return;
        }

        this.discordWebhook = DiscordWebhookConfig.fromSection( config.getSection( "webhooks.discord" ) );
    }

    public record DiscordWebhookConfig(boolean enabled,
                                       String url,
                                       String userName,
                                       String avatarUrl)
    {
        public static DiscordWebhookConfig fromSection( final ISection section )
        {
            return new DiscordWebhookConfig(
                section.getBoolean( "enabled" ),
                section.getString( "url" ),
                section.getString( "username" ),
                section.getString( "avatar-url" )
            );
        }
    }
}
