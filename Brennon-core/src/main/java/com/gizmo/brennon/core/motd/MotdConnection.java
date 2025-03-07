package com.gizmo.brennon.core.motd;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.utils.Utils;
import lombok.SneakyThrows;

import java.net.InetSocketAddress;

public abstract class MotdConnection
{

    private String name;

    public abstract int getVersion();

    public abstract InetSocketAddress getRemoteIp();

    public abstract InetSocketAddress getVirtualHost();

    @SneakyThrows
    public String getName()
    {
        if ( name == null )
        {
            this.name = BuX.getApi().getStorageManager().getDao().getUserDao()
                .getUsersOnIP( Utils.getIP( this.getRemoteIp() ) )
                .get()
                .stream()
                .findFirst()
                .orElse( null );
        }

        return this.name;
    }
}
