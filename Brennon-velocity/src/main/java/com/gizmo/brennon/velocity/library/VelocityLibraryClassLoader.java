package com.gizmo.brennon.velocity.library;

import com.gizmo.brennon.core.api.utils.reflection.LibraryClassLoader;
import com.gizmo.brennon.velocity.Bootstrap;

import java.io.File;

public class VelocityLibraryClassLoader implements LibraryClassLoader
{

    @Override
    public void loadJar( final File file )
    {
        Bootstrap.getInstance().getProxyServer().getPluginManager().addToClasspath(
                Bootstrap.getInstance(),
                file.toPath()
        );
    }
}
