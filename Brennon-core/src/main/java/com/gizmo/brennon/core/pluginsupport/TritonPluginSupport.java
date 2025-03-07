package com.gizmo.brennon.core.pluginsupport;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.pluginsupport.PluginSupport;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.rexcantor64.triton.api.Triton;
import com.rexcantor64.triton.api.TritonAPI;
import com.rexcantor64.triton.api.language.Localized;
import com.rexcantor64.triton.api.players.LanguagePlayer;

import java.util.logging.Level;

public class TritonPluginSupport implements PluginSupport
{
    @Override
    public boolean isEnabled()
    {
        return BuX.getInstance().serverOperations().getPlugin( "Triton" ).isPresent();
    }

    @Override
    public void registerPluginSupport()
    {
    }

    public String formatGuiMessage( User user, String message )
    {
        if ( user == null )
        {
            return message;
        }

        try
        {
            Triton triton = TritonAPI.getInstance();
            LanguagePlayer player = triton.getPlayerManager().get( user.getUuid() );
            Localized localized = player.getLanguage();

            return triton.getMessageParser().translateString( message, localized, triton.getConfig().getGuiSyntax() ).getResult()
                .orElse( message );
        }
        catch ( Exception e )
        {
            BuX.getLogger().log( Level.SEVERE, "Failed to format GUI message with Triton: ", e );
            return message;
        }
    }
}