package com.gizmo.brennon.core.motd.handlers;

import com.gizmo.brennon.core.motd.ConditionHandler;
import com.gizmo.brennon.core.motd.ConditionOperator;
import com.gizmo.brennon.core.motd.MotdConnection;

public class DomainConditionHandler extends ConditionHandler
{

    public DomainConditionHandler( String condition )
    {
        super( condition.replaceFirst( "domain ", "" ) );
    }

    @Override
    public boolean checkCondition( final MotdConnection connection )
    {
        if ( connection.getVirtualHost() == null || connection.getVirtualHost().getHostName() == null )
        {
            return false;
        }
        final String joinedHost = connection.getVirtualHost().getHostName();

        return operator == ConditionOperator.EQ && joinedHost.equalsIgnoreCase( value );
    }
}
