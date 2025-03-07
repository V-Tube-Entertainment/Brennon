package com.gizmo.brennon.core.api.placeholder;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.placeholder.event.handler.InputPlaceHolderEventHandler;
import com.gizmo.brennon.core.api.placeholder.event.handler.PlaceHolderEventHandler;
import com.gizmo.brennon.core.api.placeholder.placeholders.ClassPlaceHolder;
import com.gizmo.brennon.core.api.placeholder.placeholders.DefaultPlaceHolder;
import com.gizmo.brennon.core.api.placeholder.placeholders.InputPlaceHolder;
import com.gizmo.brennon.core.api.placeholder.placeholders.PlaceHolder;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.logging.Level;

public class PlaceHolderAPI
{

    private static final List<PlaceHolder> PLACEHOLDERS = Lists.newArrayList();

    private PlaceHolderAPI()
    {
    }

    public static String formatMessage( User user, String message )
    {
        if ( user == null )
        {
            return formatMessage( message );
        }
        try
        {
            for ( PlaceHolder placeholder : PLACEHOLDERS )
            {
                message = placeholder.format( user, message );
            }
            return message;
        }
        catch ( Exception e )
        {
            BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            return message;
        }
    }

    public static String formatMessage( String message )
    {
        if ( message == null )
        {
            return "";
        }
        try
        {
            for ( PlaceHolder placeholder : PLACEHOLDERS )
            {
                if ( placeholder.requiresUser() )
                {
                    continue;
                }
                message = placeholder.format( null, message );
            }
            return message;
        }
        catch ( Exception e )
        {
            BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
            return message;
        }
    }

    public static void loadPlaceHolderPack( PlaceHolderPack pack )
    {
        pack.loadPack();
    }

    public static void addPlaceHolder( ClassPlaceHolder placeholder )
    {
        PLACEHOLDERS.add( placeholder );
    }

    public static void addPlaceHolder( String placeholder, boolean requiresUser, PlaceHolderEventHandler handler )
    {
        PLACEHOLDERS.add( new DefaultPlaceHolder( placeholder, requiresUser, handler ) );
    }

    public static void addPlaceHolder( boolean requiresUser, String prefix, InputPlaceHolderEventHandler handler )
    {
        PLACEHOLDERS.add( new InputPlaceHolder( requiresUser, prefix, handler ) );
    }

    public static PlaceHolder getPlaceHolder( String placeholder )
    {
        for ( PlaceHolder ph : PLACEHOLDERS )
        {
            if ( ph.getPlaceHolderName().equalsIgnoreCase( placeholder ) )
            {
                return ph;
            }
        }
        return null;
    }
}