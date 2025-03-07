package com.gizmo.brennon.core.api.job.jobs;

import com.gizmo.brennon.core.api.job.MultiProxyJob;
import com.gizmo.brennon.core.api.utils.placeholders.MessagePlaceholders;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalPluginBroadcastLanguageMessageJob implements MultiProxyJob
{

    private final String pluginName;
    private final String languagePath;
    private final String permission;
    private final MessagePlaceholders placeholders;

    public ExternalPluginBroadcastLanguageMessageJob( String pluginName, String languagePath, String permission, MessagePlaceholders placeholders )
    {
        this.pluginName = pluginName;
        this.languagePath = languagePath;
        this.permission = permission;
        this.placeholders = placeholders;
    }

    @Override
    public boolean isAsync()
    {
        return true;
    }
}
