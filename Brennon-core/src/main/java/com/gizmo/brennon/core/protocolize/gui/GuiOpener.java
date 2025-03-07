package com.gizmo.brennon.core.protocolize.gui;

import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.Data;

@Data
public abstract class GuiOpener
{

    private final String name;

    public abstract void openGui( User user, String[] args );

    public void reload()
    {
    }
}
