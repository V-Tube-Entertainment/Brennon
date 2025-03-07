package com.gizmo.brennon.core.api.command;

import com.gizmo.brennon.core.api.user.interfaces.User;

import java.util.List;

public interface CommandCall
{

    void onExecute( final User user, final List<String> args, final List<String> parameters );

    String getDescription();

    String getUsage();

}
