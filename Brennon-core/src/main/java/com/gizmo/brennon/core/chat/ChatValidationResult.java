package com.gizmo.brennon.core.chat;

import lombok.Data;

@Data
public class ChatValidationResult
{

    public static ChatValidationResult VALID = new ChatValidationResult( true );
    public static ChatValidationResult INVALID = new ChatValidationResult( false );

    private final boolean valid;

}
