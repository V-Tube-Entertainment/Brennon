package com.gizmo.brennon.core.api.job;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@RequiredArgsConstructor
public abstract class HasUuidJob implements MultiProxyJob
{

    private final UUID uuid;

    public Optional<User> getUser()
    {
        if ( uuid == null )
        {
            return Optional.empty();
        }
        return BuX.getApi().getUser( uuid );
    }
}
