package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.job.HasUserJob;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenGuiJob extends HasUserJob
{

    private final String gui;
    private final String[] args;

    public OpenGuiJob( final String userName, final String gui, final String[] args )
    {
        super( null, userName );

        this.gui = gui;
        this.args = args;
    }

    @Override
    public boolean isAsync()
    {
        return true;
    }
}
