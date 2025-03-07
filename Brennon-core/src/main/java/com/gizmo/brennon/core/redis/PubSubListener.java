package com.gizmo.brennon.core.redis;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.event.events.redis.RedisMessageEvent;
import io.lettuce.core.pubsub.RedisPubSubAdapter;

public class PubSubListener extends RedisPubSubAdapter<String, String>
{

    @Override
    public void message( String channel, String message )
    {
        BuX.getApi().getEventLoader().launchEvent( new RedisMessageEvent( channel, message ) );
    }
}