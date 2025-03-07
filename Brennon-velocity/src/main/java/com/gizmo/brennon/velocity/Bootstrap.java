package com.gizmo.brennon.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.gizmo.brennon.core.AbstractBungeeUtilisalsX;
import com.gizmo.brennon.core.BootstrapUtil;
import com.gizmo.brennon.core.api.utils.Platform;
import com.gizmo.brennon.velocity.library.VelocityLibraryClassLoader;
import com.gizmo.brennon.velocity.utils.Slf4jLoggerWrapper;
import lombok.Getter;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@Plugin(
        id = "bungeeutilisalsx",
        name = "BungeeUtilisalsX",
        version = "2.4.7",
        authors = { "Endoy" },
        dependencies = {
                @Dependency( id = "skinsrestorer", optional = true ),
                @Dependency( id = "protocolize", optional = true ),
                @Dependency( id = "luckperms", optional = true ),
                @Dependency( id = "triton", optional = true ),
                @Dependency( id = "redisbungee", optional = true )
        }
)
public class Bootstrap
{

    @Getter
    private static Bootstrap instance;
    @Getter
    private final ProxyServer proxyServer;
    @Getter
    private final Logger logger;
    @Getter
    private final File dataFolder;
    private AbstractBungeeUtilisalsX abstractBungeeUtilisalsX;

    @Inject
    public Bootstrap( final ProxyServer proxyServer,
                      final org.slf4j.Logger logger,
                      final @DataDirectory Path dataDirectory)
    {
        instance = this;
        this.proxyServer = proxyServer;
        this.logger = LogManager.getLogManager().getLogger( "" );
        for ( Handler handler : this.logger.getHandlers() )
        {
            this.logger.removeHandler( handler );
        }
        this.logger.addHandler( new Slf4jLoggerWrapper( logger ) );
        this.dataFolder = dataDirectory.toFile();
    }

    @Subscribe
    public void onProxyInitialization( final ProxyInitializeEvent event )
    {
        Platform.setCurrentPlatform( Platform.VELOCITYPOWERED );
        BootstrapUtil.loadLibraries( this.getDataFolder(), new VelocityLibraryClassLoader(), logger );

        abstractBungeeUtilisalsX = new BungeeUtilisalsX();
        abstractBungeeUtilisalsX.initialize();
    }

    @Subscribe
    public void onProxyShutdown( final ProxyShutdownEvent event )
    {
        abstractBungeeUtilisalsX.shutdown();
    }

}
