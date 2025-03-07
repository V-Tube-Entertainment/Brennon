package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.job.MultiProxyJob;
import com.gizmo.brennon.core.api.server.IProxyServer;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class UserSwitchServerJob implements MultiProxyJob
{

    private final String targetName;
    private final String server;

    public UserSwitchServerJob( final String targetName,
                                final String server )
    {
        this.targetName = targetName;
        this.server = server;
    }

    @Override
    public boolean isAsync()
    {
        return true;
    }

    public Optional<User> getTargetUser()
    {
        return BuX.getApi().getUser( targetName );
    }

    public Optional<IProxyServer> getTargetServer()
    {
        return Optional.ofNullable( BuX.getInstance().serverOperations().getServerInfo( server ) );
    }
}
