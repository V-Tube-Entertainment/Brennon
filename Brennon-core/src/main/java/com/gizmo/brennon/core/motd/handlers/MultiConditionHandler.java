package com.gizmo.brennon.core.motd.handlers;

import com.gizmo.brennon.core.motd.ConditionHandler;
import com.gizmo.brennon.core.motd.MotdConnection;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode( callSuper = true )
public class MultiConditionHandler extends ConditionHandler
{

    private final boolean and;
    private final ConditionHandler[] handlers;

    public MultiConditionHandler( final String condition, final boolean and, final ConditionHandler[] handlers )
    {
        super( condition );

        this.and = and;
        this.handlers = handlers;
    }

    @Override
    public boolean checkCondition( final MotdConnection connection )
    {
        if ( and )
        {
            for ( ConditionHandler handler : handlers )
            {
                if ( !handler.checkCondition( connection ) )
                {
                    return false;
                }
            }
        }
        else
        {
            for ( ConditionHandler handler : handlers )
            {
                if ( handler.checkCondition( connection ) )
                {
                    return true;
                }
            }
        }
        // defaulting to false
        return false;
    }
}
