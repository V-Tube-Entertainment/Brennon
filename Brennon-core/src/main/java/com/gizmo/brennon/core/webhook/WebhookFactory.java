package com.gizmo.brennon.core.webhook;

public class WebhookFactory
{

    public static DiscordWebhookHelper discord()
    {
        return new DiscordWebhookHelper();
    }
}
