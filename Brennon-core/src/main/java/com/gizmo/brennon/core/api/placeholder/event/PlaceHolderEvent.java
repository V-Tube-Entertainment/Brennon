package com.gizmo.brennon.core.api.placeholder.event;

import com.gizmo.brennon.core.api.placeholder.placeholders.PlaceHolder;
import com.gizmo.brennon.core.api.user.interfaces.User;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class PlaceHolderEvent
{

    private User user;
    private PlaceHolder placeHolder;
    private String message;

}