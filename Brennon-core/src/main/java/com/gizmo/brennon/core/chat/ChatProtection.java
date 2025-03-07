package com.gizmo.brennon.core.chat;

import com.gizmo.brennon.core.api.user.interfaces.User;

public interface ChatProtection
{

    void reload();

    <T extends ChatValidationResult> T validateMessage( User user, String message );

}
