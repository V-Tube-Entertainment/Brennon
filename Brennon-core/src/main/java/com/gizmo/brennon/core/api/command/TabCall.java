package com.gizmo.brennon.core.api.command;

import com.gizmo.brennon.core.api.user.interfaces.User;

import java.util.List;

public interface TabCall
{

    List<String> onTabComplete( final User user, final String[] args );
}
