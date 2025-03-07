package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.job.MultiProxyJob;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ChatSyncJob implements MultiProxyJob
{

    private final String serverGroupName;
    private final String serverToSkip;
    private final String message;

    @Override
    public boolean isAsync()
    {
        return true;
    }
}
