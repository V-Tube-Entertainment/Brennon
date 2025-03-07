package com.gizmo.brennon.core.api.placeholder.xml;

import com.gizmo.brennon.core.api.user.interfaces.User;
import org.jsoup.nodes.Document;

public abstract class XMLPlaceHolder
{

    public abstract String format( User user, String original, Document document );

}
