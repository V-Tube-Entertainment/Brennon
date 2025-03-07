package com.gizmo.brennon.core.api.redis;

import com.gizmo.brennon.core.api.user.interfaces.User;

import java.util.concurrent.TimeUnit;

public interface IRedisDataManager
{

    void loadRedisUser( User user );

    void unloadRedisUser( User user );

    long getAmountOfOnlineUsersOnDomain( String domain );

    PartyDataManager getRedisPartyDataManager();

    boolean attemptShedLock( String type, int period, TimeUnit unit );

    default boolean attemptShedLock( String type, int period, com.gizmo.brennon.core.api.utils.TimeUnit unit )
    {
        return attemptShedLock( type, period, unit.toJavaTimeUnit() );
    }

}
