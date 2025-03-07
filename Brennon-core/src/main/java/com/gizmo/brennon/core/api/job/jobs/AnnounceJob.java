package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.announcer.AnnouncementType;
import com.gizmo.brennon.core.api.job.MultiProxyJob;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
public class AnnounceJob implements MultiProxyJob
{

    private final Set<AnnouncementType> types;
    private final String message;

    @Override
    public boolean isAsync()
    {
        return true;
    }
}
