package com.gizmo.brennon.core.motd.handlers;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.utils.Version;
import com.gizmo.brennon.core.motd.ConditionHandler;
import com.gizmo.brennon.core.motd.MotdConnection;

import java.util.Arrays;
import java.util.stream.Collectors;

public class VersionConditionHandler extends ConditionHandler
{

    public VersionConditionHandler( String condition )
    {
        super( condition.replaceFirst( "version ", "" ).replace( ".", "_" ) );
    }

    @Override
    public boolean checkCondition( final MotdConnection connection )
    {
        final Version version = formatVersion( value );

        if ( version == null )
        {
            return false;
        }

        return switch ( operator )
        {
            case LT -> connection.getVersion() < version.getVersionId();
            case LTE -> connection.getVersion() <= version.getVersionId();
            case EQ -> connection.getVersion() == version.getVersionId();
            case NOT_EQ -> connection.getVersion() != version.getVersionId();
            case GTE -> connection.getVersion() >= version.getVersionId();
            case GT -> connection.getVersion() > version.getVersionId();
        };
    }

    private Version formatVersion( String mcVersion )
    {
        try
        {
            return Version.valueOf( "MINECRAFT_" + mcVersion );
        }
        catch ( IllegalArgumentException e )
        {
            BuX.getLogger().warning( "Found an invalid version in condition 'version " + condition + "'!" );
            BuX.getLogger().warning( "Available versions:" );
            BuX.getLogger().warning( listVersions() );
            return null;
        }
    }

    private String listVersions()
    {
        return Arrays.stream( Version.values() )
            .map( Version::toString )
            .collect( Collectors.joining( ", " ) );
    }
}