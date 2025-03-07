package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.job.HasUserJob;
import com.gizmo.brennon.core.api.punishments.PunishmentInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserWarnJob extends HasUserJob
{

    private final PunishmentInfo info;

    public UserWarnJob( final UUID uuid, final String userName, final PunishmentInfo info )
    {
        super( uuid, userName );

        this.info = info;
    }

    @Override
    public boolean isAsync()
    {
        return true;
    }

}
