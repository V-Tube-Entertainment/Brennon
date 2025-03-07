package com.gizmo.brennon.core.migration;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.utils.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class FileMigration implements Migration
{

    private final List<String> migrationStatements = new ArrayList<>();

    public FileMigration( final String filePath )
    {
        try ( InputStream is = FileUtils.getResourceAsStream( filePath ) )
        {
            try ( BufferedReader reader = new BufferedReader( new InputStreamReader( is, StandardCharsets.UTF_8 ) ) )
            {
                StringBuilder builder = new StringBuilder();

                String line;
                while ( ( line = reader.readLine() ) != null )
                {
                    builder.append( line );

                    if ( line.endsWith( ";" ) )
                    {
                        builder.deleteCharAt( builder.length() - 1 );

                        String statement = SqlConstants.replaceConstants( builder.toString().trim() );
                        if ( !statement.isEmpty() )
                        {
                            this.migrationStatements.add( statement );
                        }

                        builder = new StringBuilder();
                    }
                }
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public void migrate() throws SQLException
    {
        try ( Connection connection = BuX.getInstance().getAbstractStorageManager().getConnection() )
        {
            try ( Statement statement = connection.createStatement() )
            {
                for ( String migrationStatement : migrationStatements )
                {
                    statement.execute( migrationStatement );
                }
            }
        }
    }
}
