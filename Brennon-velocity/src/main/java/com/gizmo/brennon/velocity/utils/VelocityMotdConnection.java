package com.gizmo.brennon.velocity.utils;

import com.gizmo.brennon.core.motd.MotdConnection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.InetSocketAddress;

@Data
@AllArgsConstructor
@EqualsAndHashCode( callSuper = false)
public class VelocityMotdConnection extends MotdConnection
{

    private final int version;
    private final InetSocketAddress remoteIp;
    private final InetSocketAddress virtualHost;

}
