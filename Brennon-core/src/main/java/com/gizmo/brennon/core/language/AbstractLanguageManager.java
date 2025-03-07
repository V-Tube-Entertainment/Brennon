package com.gizmo.brennon.core.language;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.language.ILanguageManager;
import com.gizmo.brennon.core.api.language.Language;
import com.gizmo.brennon.core.api.language.LanguageConfig;
import com.gizmo.brennon.core.api.language.LanguageIntegration;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.endoy.configuration.api.FileStorageType;
import dev.endoy.configuration.api.IConfiguration;
import dev.endoy.configuration.api.ISection;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

@Getter
public abstract class AbstractLanguageManager implements ILanguageManager
{

    protected Map<String, File> plugins = Maps.newHashMap();
    protected Map<String, FileStorageType> fileTypes = Maps.newHashMap();
    protected Map<File, LanguageConfig> configurations = Maps.newHashMap();
    protected List<Language> languages = Lists.newArrayList();
    protected LanguageIntegration integration;

    AbstractLanguageManager()
    {
        ISection section = ConfigFiles.LANGUAGES_CONFIG.getConfig().getSection( "languages" );

        for ( String key : section.getKeys() )
        {
            languages.add( new Language( key, section.getBoolean( key + ".default" ) ) );
        }
    }

    @Override
    public Language getLangOrDefault( String language )
    {
        return getLanguage( language ).orElse( getDefaultLanguage() );
    }

    @Override
    public LanguageIntegration getLanguageIntegration()
    {
        return integration;
    }

    @Override
    public void setLanguageIntegration( LanguageIntegration integration )
    {
        this.integration = integration;
    }

    @Override
    public Language getDefaultLanguage()
    {
        return languages.stream().filter( Language::isDefault ).findFirst().orElse( languages.stream().findFirst().orElse( null ) );
    }

    @Override
    public Optional<Language> getLanguage( String language )
    {
        return languages.stream().filter( lang -> lang.getName().equalsIgnoreCase( language ) ).findFirst();
    }

    @Override
    public void addPlugin( String plugin, File folder, FileStorageType type )
    {
        plugins.put( plugin, folder );
        fileTypes.put( plugin, type );
    }

    @Override
    public LanguageConfig getLanguageConfiguration( String plugin, User user )
    {
        LanguageConfig config = null;
        if ( user != null )
        {
            config = getConfig( plugin, user.getLanguage() );
        }
        if ( config == null )
        {
            config = getConfig( plugin, getDefaultLanguage() );
        }
        return config;
    }

    @Override
    public LanguageConfig getLanguageConfiguration( String plugin, String player )
    {
        return getLanguageConfiguration( plugin, BuX.getApi().getUser( player ).orElse( null ) );
    }

    @Override
    public File getFile( String plugin, Language language )
    {
        if ( !plugins.containsKey( plugin ) )
        {
            throw new RuntimeException( "The plugin " + plugin + " is not registered!" );
        }
        if ( fileTypes.get( plugin ).equals( FileStorageType.JSON ) )
        {
            return new File( plugins.get( plugin ), language.getName() + ".json" );
        }
        return new File( plugins.get( plugin ), language.getName() + ".yml" );
    }

    @Override
    public LanguageConfig getConfig( String plugin, Language language )
    {
        language = language == null ? getDefaultLanguage() : language;
        if ( !plugins.containsKey( plugin ) )
        {
            throw new RuntimeException( "The plugin " + plugin + " is not registered!" );
        }
        File lang = getFile( plugin, language );

        if ( !configurations.containsKey( lang ) )
        {
            BuX.getLogger().warning( "The plugin " + plugin + " did not register the language " + language.getName() + " yet!" );

            File deflang = getFile( plugin, getDefaultLanguage() );
            if ( configurations.containsKey( deflang ) )
            {
                return configurations.get( deflang );
            }
            return null;
        }
        return configurations.get( lang );
    }

    @Override
    public boolean isRegistered( String plugin, Language language )
    {
        if ( !plugins.containsKey( plugin ) )
        {
            throw new RuntimeException( "The plugin " + plugin + " is not registered!" );
        }
        return getConfig( plugin, language ) != null;
    }

    @Override
    public boolean saveLanguage( String plugin, Language language )
    {
        if ( !plugins.containsKey( plugin ) )
        {
            throw new RuntimeException( "The plugin " + plugin + " is not registered!" );
        }
        File lang = getFile( plugin, language );
        IConfiguration config = configurations.get( lang ).getConfig();

        try
        {
            config.save();
        }
        catch ( IOException e )
        {
            BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
        }
        return true;
    }

    @Override
    public boolean reloadConfig( String plugin, Language language )
    {
        if ( !plugins.containsKey( plugin ) )
        {
            throw new RuntimeException( "The plugin " + plugin + " is not registered!" );
        }
        File lang = getFile( plugin, language );
        IConfiguration config = configurations.get( lang ).getConfig();
        try
        {
            config.reload();
        }
        catch ( IOException e )
        {
            BuX.getLogger().log( Level.SEVERE, "An error occured: ", e );
        }
        return true;
    }

    @Override
    public boolean useCustomIntegration()
    {
        return integration != null;
    }

    protected abstract File loadResource( Class<?> resourceClass, String plugin, String source, File target );
}