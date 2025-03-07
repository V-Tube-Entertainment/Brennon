package com.gizmo.brennon.core.api.event.events.redis;

import com.gizmo.brennon.core.api.event.AbstractEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode( callSuper = true )
public class RedisMessageEvent extends AbstractEvent
{

    private final String channel;
    private final String message;

}
