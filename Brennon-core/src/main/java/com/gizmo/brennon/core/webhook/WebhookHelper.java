package com.gizmo.brennon.core.webhook;

public interface WebhookHelper<T>
{

    void send( T data );

}
