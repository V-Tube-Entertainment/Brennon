package com.gizmo.brennon.core.api.utils;

import com.gizmo.brennon.core.BuX;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.util.Optional;

public class FileUtils
{

    private FileUtils()
    {
    }

    @SneakyThrows
    public static InputStream getResourceAsStream( final String path )
    {
        return Optional.ofNullable( BuX.class.getClassLoader().getResourceAsStream( path ) )
            .orElse( path.startsWith( "/" )
                ? BuX.class.getClassLoader().getResourceAsStream( path.replaceFirst( "/", "" ) )
                : BuX.class.getClassLoader().getResourceAsStream( "/" + path ) );
    }
}
