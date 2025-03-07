package com.gizmo.brennon.core.announcers.bossbar;

import com.gizmo.brennon.core.api.bossbar.BarColor;
import com.gizmo.brennon.core.api.bossbar.BarStyle;
import dev.endoy.configuration.api.ISection;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class BossBarMessage
{
    BarColor color;
    BarStyle style;
    float progress;
    boolean language;
    String text;

    public BossBarMessage( final ISection section )
    {
        this(
            BarColor.valueOf( section.getString( "color" ) ),
            BarStyle.valueOf( section.getString( "style" ) ),
            section.getFloat( "progress" ),
            section.exists( "language" ) ? section.getBoolean( "language" ) : false,
            section.getString( "text" )
        );
    }
}
