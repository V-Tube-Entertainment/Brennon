package com.gizmo.brennon.core.webhook;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.utils.DiscordWebhook;
import com.gizmo.brennon.core.api.utils.DiscordWebhook.EmbedObject;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;

import java.io.IOException;

public class DiscordWebhookHelper implements WebhookHelper<EmbedObject>
{

    @Override
    public void send( final EmbedObject embedObject )
    {
        final DiscordWebhook webhook = new DiscordWebhook( ConfigFiles.WEBHOOK_CONFIG.getDiscordWebhook() );
        webhook.addEmbed( embedObject );

        BuX.getInstance().getScheduler().runAsync( () ->
        {
            try
            {
                webhook.execute();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        } );
    }
}
