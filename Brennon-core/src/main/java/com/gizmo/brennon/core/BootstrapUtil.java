package com.gizmo.brennon.core;

import com.gizmo.brennon.core.api.utils.reflection.LibraryClassLoader;
import com.gizmo.brennon.core.library.Library;
import com.gizmo.brennon.core.library.StandardLibrary;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.util.logging.Logger;

public class BootstrapUtil
{

    @Getter
    private static File dataFolder;

    @SneakyThrows
    public static void loadLibraries( File dataFolder,
                                      LibraryClassLoader libraryClassLoader,
                                      Logger logger )
    {
        BootstrapUtil.dataFolder = dataFolder;

        logger.info( "Loading libraries ..." );

        for ( StandardLibrary standardLibrary : StandardLibrary.values() )
        {
            Library library = standardLibrary.getLibrary();

            if ( library.isToLoad() && !library.isPresent() )
            {
                library.load( dataFolder, libraryClassLoader, logger );
            }
        }
        logger.info( "Libraries have been loaded." );
    }
}
