package com.gizmo.brennon.core.chat;

import com.gizmo.brennon.core.chat.protections.AdvertisementChatProtection;
import com.gizmo.brennon.core.chat.protections.CapsChatProtection;
import com.gizmo.brennon.core.chat.protections.SpamChatProtection;
import com.gizmo.brennon.core.chat.protections.SwearChatProtection;
import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

public class ChatProtections
{

    public static ChatProtection ADVERTISEMENT_PROTECTION = new AdvertisementChatProtection();
    public static ChatProtection CAPS_PROTECTION = new CapsChatProtection();
    public static ChatProtection SPAM_PROTECTION = new SpamChatProtection();
    public static SwearChatProtection SWEAR_PROTECTION = new SwearChatProtection();

    public static void reloadAllProtections()
    {
        final List<ChatProtection> protections = getAllProtections();

        for ( ChatProtection protection : protections )
        {
            protection.reload();
        }
    }

    public static List<ChatProtection> getAllProtections()
    {
        final List<ChatProtection> protections = Lists.newArrayList();

        for ( Field field : ChatProtections.class.getFields() )
        {
            if ( !Modifier.isStatic( field.getModifiers() ) )
            {
                continue;
            }
            try
            {
                final Object value = field.get( null );

                if ( value instanceof ChatProtection )
                {
                    protections.add( (ChatProtection) value );
                }
            }
            catch ( IllegalAccessException e )
            {
                // ignore
            }
        }
        return protections;
    }
}