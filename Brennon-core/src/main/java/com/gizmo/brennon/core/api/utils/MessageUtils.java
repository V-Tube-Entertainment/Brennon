package com.gizmo.brennon.core.api.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.HashMap;
import java.util.Map;

public class MessageUtils
{

    public static final char SECTION_CHAR = '\u00A7';
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer
        .builder()
        .flattener( ComponentFlattener.textOnly() )
        .build();
    private static final LegacyComponentSerializer SECTION_WITH_RGB = LegacyComponentSerializer
        .builder()
        .character( SECTION_CHAR )
        .hexCharacter( '#' )
        .hexColors()
        .build();
    private static final LegacyComponentSerializer SECTION_WITH_UNUSUAL_RGB = LegacyComponentSerializer
        .builder()
        .character( SECTION_CHAR )
        .hexCharacter( '#' )
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build();
    private static final LegacyComponentSerializer AMPERSAND_WITH_RGB = LegacyComponentSerializer
        .builder()
        .character( '&' )
        .hexCharacter( '#' )
        .hexColors()
        .build();
    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer
        .builder()
        .character( SECTION_CHAR )
        .build();

    private static final Map<String, String> COLOR_MAPPINGS = new HashMap<>()
    {{
        put( "0", "black" );
        put( "1", "dark_blue" );
        put( "2", "dark_green" );
        put( "3", "dark_aqua" );
        put( "4", "dark_red" );
        put( "5", "dark_purple" );
        put( "6", "gold" );
        put( "7", "gray" );
        put( "8", "dark_gray" );
        put( "9", "blue" );
        put( "a", "green" );
        put( "b", "aqua" );
        put( "c", "red" );
        put( "d", "light_purple" );
        put( "e", "yellow" );
        put( "f", "white" );
        put( "k", "obfuscated" );
        put( "l", "bold" );
        put( "m", "strikethrough" );
        put( "n", "underlined" );
        put( "o", "italic" );
        put( "r", "reset" );
    }};

    private MessageUtils()
    {
    }

    public static Component fromText( String text )
    {
        text = convertForMiniMessage( text );

        return MiniMessage.miniMessage().deserialize( text );
    }

    public static Component fromTextNoColors( String text )
    {
        return LEGACY_COMPONENT_SERIALIZER.deserialize( text );
    }

    public static String colorizeLegacy( String str )
    {
        return SECTION_WITH_UNUSUAL_RGB.serialize(
            AMPERSAND_WITH_RGB.deserialize(
                SECTION_WITH_UNUSUAL_RGB.serialize(
                    fromText( str )
                )
            )
        );
    }

    public static String convertForMiniMessage( String text )
    {
        if ( !text.contains( "&" ) && !text.contains( "§" ) )
        {
            return text;
        }

        for ( Map.Entry<String, String> entry : COLOR_MAPPINGS.entrySet() )
        {
            if ( text.contains( "&" + entry.getKey() ) )
            {
                text = text.replace( "&" + entry.getKey(), "<" + entry.getValue() + ">" );
            }
            else if ( text.contains( "§" + entry.getKey() ) )
            {
                text = text.replace( "§" + entry.getKey(), "<" + entry.getValue() + ">" );
            }
        }

        return text;
    }
}
