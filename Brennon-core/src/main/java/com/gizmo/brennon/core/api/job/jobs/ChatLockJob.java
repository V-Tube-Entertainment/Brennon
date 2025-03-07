package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.job.MultiProxyJob;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class ChatLockJob implements MultiProxyJob
{

    private final String serverName;
    private final String by;

    @Override
    public boolean isAsync()
    {
        return true;
    }
}
