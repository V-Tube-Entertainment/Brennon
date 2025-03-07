package com.gizmo.brennon.core.permission.integrations;

import com.gizmo.brennon.core.permission.PermissionIntegration;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DefaultPermissionIntegration implements PermissionIntegration
{

    @Override
    public boolean isActive()
    {
        return true;
    }

    @Override
    public CompletableFuture<String> getGroup( final UUID uuid )
    {
        return CompletableFuture.completedFuture( "" );
    }

    @Override
    public String getPrefix( UUID uuid )
    {
        return "";
    }

    @Override
    public String getSuffix( UUID uuid )
    {
        return "";
    }
}
