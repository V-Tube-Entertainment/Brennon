package com.gizmo.brennon.core.placeholders;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.placeholder.event.InputPlaceHolderEvent;
import com.gizmo.brennon.core.api.placeholder.impl.InputPlaceHolderImpl;
import com.gizmo.brennon.core.api.utils.javascript.Script;

import java.util.Optional;

public class JavaScriptPlaceHolder extends InputPlaceHolderImpl
{

    public JavaScriptPlaceHolder()
    {
        super( false, "javascript" );
    }

    @Override
    public String getReplacement( InputPlaceHolderEvent event )
    {
        final Optional<Script> optional = BuX.getInstance().getScripts()
            .stream()
            .filter( s -> s.getFile().equalsIgnoreCase( event.getArgument() ) )
            .findFirst();

        if ( optional.isPresent() )
        {
            return optional.get().getReplacement( event.getUser() );
        }
        else
        {
            return "script not found";
        }
    }
}