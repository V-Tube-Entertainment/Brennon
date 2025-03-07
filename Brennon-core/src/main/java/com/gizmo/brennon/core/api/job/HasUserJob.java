package com.gizmo.brennon.core.api.job;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
public abstract class HasUserJob extends HasUuidJob
{

    private final String userName;

    public HasUserJob( final UUID uuid, final String userName )
    {
        super( uuid );

        this.userName = userName;
    }

    public Optional<User> getUserByName()
    {
        if ( userName == null )
        {
            return Optional.empty();
        }

        return BuX.getApi().getUser( userName );
    }
}
