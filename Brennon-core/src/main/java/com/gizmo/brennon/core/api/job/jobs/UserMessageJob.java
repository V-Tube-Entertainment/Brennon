package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.job.HasUuidJob;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserMessageJob extends HasUuidJob
{

    private final String message;

    public UserMessageJob( final HasUuidJob job,
                           final String message )
    {
        this( job.getUuid(), message );
    }

    public UserMessageJob( final UUID uuid,
                           final String message )
    {
        super( uuid );
        this.message = message;
    }

    @Override
    public boolean isAsync()
    {
        return true;
    }
}
