package com.gizmo.brennon.core.commands;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.command.Command;
import com.gizmo.brennon.core.api.command.CommandBuilder;
import com.gizmo.brennon.core.api.command.CommandCall;
import com.gizmo.brennon.core.api.server.IProxyServer;
import com.gizmo.brennon.core.api.utils.config.ConfigFiles;
import com.gizmo.brennon.core.api.utils.config.configs.ServerBalancerConfig.ServerBalancerGroup;
import com.gizmo.brennon.core.api.utils.server.ServerGroup;
import com.gizmo.brennon.core.commands.domains.DomainsCommandCall;
import com.gizmo.brennon.core.commands.friends.FriendsCommandCall;
import com.gizmo.brennon.core.commands.general.*;
import com.gizmo.brennon.core.commands.general.message.*;
import com.gizmo.brennon.core.commands.general.spy.CommandSpyCommandCall;
import com.gizmo.brennon.core.commands.general.spy.SocialSpyCommandCall;
import com.gizmo.brennon.core.commands.party.PartyCommandCall;
import com.gizmo.brennon.core.commands.plugin.PluginCommandCall;
import com.gizmo.brennon.core.commands.punishments.*;
import com.gizmo.brennon.core.commands.punishments.removal.UnbanCommandCall;
import com.gizmo.brennon.core.commands.punishments.removal.UnbanIPCommandCall;
import com.gizmo.brennon.core.commands.punishments.removal.UnmuteCommandCall;
import com.gizmo.brennon.core.commands.punishments.removal.UnmuteIPCommandCall;
import com.gizmo.brennon.core.commands.report.ReportCommandCall;
import com.google.common.collect.Lists;
import dev.endoy.configuration.api.IConfiguration;
import dev.endoy.configuration.api.ISection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandManager
{

    protected final List<Command> commands = Lists.newArrayList();

    public void load()
    {
        if ( !commands.isEmpty() )
        {
            this.unregisterAll();
        }

        this.registerGeneralCommands();
        this.registerCustomCommands();
        this.registerPunishmentCommands();
        this.registerServerBalancerCommands();
    }

    protected void registerGeneralCommands()
    {
        registerGeneralCommand( "bungeeutilisals", new PluginCommandCall() );
        registerGeneralCommand( "server", new ServerCommandCall() );
        registerGeneralCommand( "announce", new AnnounceCommandCall() );
        registerGeneralCommand( "find", new FindCommandCall() );
        registerGeneralCommand( "glag", new GLagCommandCall() );
        registerGeneralCommand( "clearchat", new ClearChatCommandCall() );
        registerGeneralCommand( "helpop", new HelpOpCommandCall() );
        registerGeneralCommand( "staff", new StaffCommandCall() );
        registerGeneralCommand( "ping", new PingCommandCall() );
        registerGeneralCommand( "language", new LanguageCommandCall() );
        registerGeneralCommand( "glist", new GListCommandCall() );
        registerGeneralCommand( "shout", new ShoutCommandCall() );
        registerGeneralCommand( "socialspy", new SocialSpyCommandCall() );
        registerGeneralCommand( "commandspy", new CommandSpyCommandCall() );
        registerGeneralCommand( "msg", new MsgCommandCall() );
        registerGeneralCommand( "reply", new ReplyCommandCall() );
        registerGeneralCommand( "ignore", new IgnoreCommandCall() );
        registerGeneralCommand( "msgtoggle", new MsgToggleCommandCall() );
        registerGeneralCommand( "report", new ReportCommandCall() );
        registerGeneralCommand( "chatlock", new ChatLockCommandCall() );
        registerGeneralCommand( "staffchat", new StaffChatCommandCall() );
        registerGeneralCommand( "domains", new DomainsCommandCall() );
        registerGeneralCommand( "staffannouncement", new StaffAnnouncementCommandCall() );
        registerGeneralCommand( "offlinemessage", new OfflineMessageCommandCall() );
        registerGeneralCommand( "togglebossbar", new ToggleBossBarCommandCall() );

        if ( BuX.getInstance().isProtocolizeEnabled() )
        {
            registerGeneralCommand( "opengui", new OpenGuiCommandCall(), List.of( "-u" ) );
        }

        if ( ConfigFiles.GENERALCOMMANDS.getConfig().getBoolean( "server.slash-server.enabled" ) )
        {
            registerSlashServerCommands();
        }
        registerCommand(
            "friends",
            ConfigFiles.FRIENDS_CONFIG.getConfig().getBoolean( "enabled" ),
            ConfigFiles.FRIENDS_CONFIG.getConfig().getSection( "command" ),
            new FriendsCommandCall()
        );
        registerCommand(
            "party",
            ConfigFiles.PARTY_CONFIG.getConfig().getBoolean( "enabled" ),
            ConfigFiles.PARTY_CONFIG.getConfig().getSection( "command" ),
            new PartyCommandCall()
        );
    }

    protected void registerPunishmentCommands()
    {
        final IConfiguration config = ConfigFiles.PUNISHMENT_CONFIG.getConfig();

        if ( !config.getBoolean( "enabled" ) )
        {
            return;
        }
        final List<String> parameters = this.getParameterList( config.getSection( "parameters" ) );

        registerPunishmentCommand( "ban", "commands.ban", new BanCommandCall(), parameters );
        registerPunishmentCommand( "ipban", "commands.ipban", new IPBanCommandCall(), parameters );
        registerPunishmentCommand( "tempban", "commands.tempban", new TempBanCommandCall(), parameters );
        registerPunishmentCommand( "iptempban", "commands.iptempban", new IPTempBanCommandCall(), parameters );
        registerPunishmentCommand( "mute", "commands.mute", new MuteCommandCall(), parameters );
        registerPunishmentCommand( "ipmute", "commands.ipmute", new IPMuteCommandCall(), parameters );
        registerPunishmentCommand( "tempmute", "commands.tempmute", new TempMuteCommandCall(), parameters );
        registerPunishmentCommand( "iptempmute", "commands.iptempmute", new IPTempMuteCommandCall(), parameters );
        registerPunishmentCommand( "kick", "commands.kick", new KickCommandCall(), parameters );
        registerPunishmentCommand( "warn", "commands.warn", new WarnCommandCall(), parameters );
        registerPunishmentCommand( "unban", "commands.unban", new UnbanCommandCall(), parameters );
        registerPunishmentCommand( "unbanip", "commands.unbanip", new UnbanIPCommandCall(), parameters );
        registerPunishmentCommand( "unmute", "commands.unmute", new UnmuteCommandCall(), parameters );
        registerPunishmentCommand( "unmuteip", "commands.unmuteip", new UnmuteIPCommandCall(), parameters );
        registerPunishmentCommand( "punishmentinfo", "commands.punishmentinfo", new PunishmentInfoCommandCall(), null );
        registerPunishmentCommand( "punishmenthistory", "commands.punishmenthistory", new PunishmentHistoryCommandCall(), null );
        registerPunishmentCommand( "punishmentdata", "commands.punishmentdata", new PunishmentDataCommandCall(), null );
        registerPunishmentCommand( "checkip", "commands.checkip", new CheckIpCommandCall(), null );
        registerPunishmentCommand( "staffhistory", "commands.staffhistory", new StaffHistoryCommandCall(), parameters );
        registerPunishmentCommand( "trackpunish", "commands.trackpunish", new TrackPunishCommandCall(), parameters );
        registerPunishmentCommand(
            "staffrollback",
            "commands.staffrollback",
            new StaffRollbackCommandCall(),
            this.getParameterList( config.getSection( "staffrollback-parameters" ) )
        );
    }

    protected void registerSlashServerCommands()
    {
        final String permission = ConfigFiles.GENERALCOMMANDS.getConfig().getString( "server.slash-server.permission" );

        for ( IProxyServer proxyServer : BuX.getInstance().serverOperations().getServers() )
        {
            final String name = proxyServer.getName().toLowerCase();
            final CommandBuilder builder = CommandBuilder.builder()
                .enabled( true )
                .name( name )
                .permission( permission.replace( "{server}", name ) )
                .executable( new SlashServerCommandCall( name ) );

            buildCommand( name, builder );
        }
    }

    protected void registerCustomCommands()
    {
        final IConfiguration config = ConfigFiles.CUSTOMCOMMANDS.getConfig();

        for ( ISection section : config.getSectionList( "commands" ) )
        {
            final String name = section.getString( "name" );
            final List<String> aliases = section.exists( "aliases" ) ? section.getStringList( "aliases" ) : Lists.newArrayList();
            final String permission = section.exists( "permission" ) ? section.getString( "permission" ) : null;
            final List<String> commands = section.exists( "execute" ) ? section.getStringList( "execute" ) : Lists.newArrayList();
            final String server = section.exists( "server" ) ? section.getString( "server" ) : "ALL";
            final List<ServerGroup> disabledServers = section.exists( "disabled-servers" ) ? section.getStringList( "disabled-servers" )
                .stream()
                .filter( s -> ConfigFiles.SERVERGROUPS.getServers().containsKey( s ) )
                .map( s -> ConfigFiles.SERVERGROUPS.getServer( s ) )
                .flatMap( Optional::stream )
                .collect( Collectors.toList() ) : new ArrayList<>();
            final boolean listenerBased = section.exists( "listener-based" ) && section.getBoolean( "listener-based" );

            final CommandBuilder commandBuilder = CommandBuilder.builder()
                .enabled( true )
                .name( name )
                .aliases( aliases )
                .permission( permission )
                .disabledServers( disabledServers )
                .listenerBased( listenerBased )
                .executable( new CustomCommandCall( section, server, commands ) );

            buildCommand( name, commandBuilder );
        }
    }

    protected void registerServerBalancerCommands()
    {
        if ( !ConfigFiles.SERVER_BALANCER_CONFIG.isEnabled() )
        {
            return;
        }

        for ( ServerBalancerGroup balancerGroup : ConfigFiles.SERVER_BALANCER_CONFIG.getBalancerGroups() )
        {
            registerCommand(
                balancerGroup.getServerGroup().getName(),
                balancerGroup.getCommandSection(),
                new ServerBalancerCommandCall( balancerGroup )
            );
        }
    }

    public void registerGeneralCommand( final String section, final CommandCall call )
    {
        this.registerGeneralCommand( section, call, new ArrayList<>() );
    }

    public void registerGeneralCommand( final String section, final CommandCall call, final List<String> parameters )
    {
        final CommandBuilder commandBuilder = CommandBuilder.builder()
            .name( section )
            .fromSection( ConfigFiles.GENERALCOMMANDS.getConfig().getSection( section ) )
            .parameters( parameters )
            .executable( call );

        buildCommand( section, commandBuilder );
    }

    protected void registerPunishmentCommand( final String name, final String section, final CommandCall call, final List<String> parameters )
    {
        final IConfiguration config = ConfigFiles.PUNISHMENT_CONFIG.getConfig();

        final CommandBuilder commandBuilder = CommandBuilder.builder()
            .name( name )
            .fromSection( config.getSection( section ) )
            .executable( call );

        if ( parameters != null )
        {
            commandBuilder.parameters( parameters );
        }

        buildCommand( name, commandBuilder );
    }

    protected void registerCommand( final String name, final ISection section, final CommandCall call )
    {
        final CommandBuilder commandBuilder = CommandBuilder.builder()
            .name( name )
            .fromSection( section )
            .executable( call );

        buildCommand( name, commandBuilder );
    }

    protected void registerCommand( final String name, final boolean enabled, final ISection section, final CommandCall call )
    {
        final CommandBuilder commandBuilder = CommandBuilder.builder()
            .name( name )
            .fromSection( section )
            .enabled( enabled )
            .executable( call );

        buildCommand( name, commandBuilder );
    }

    protected void buildCommand( final String name, final CommandBuilder builder )
    {
        final Command command = builder.build();

        if ( command != null )
        {
            command.register();

            commands.add( command );
            BuX.debug( "Registered a command named " + command.getName() + "." );
        }
        else
        {
            BuX.debug( "Skipping registration of a command named " + name + "." );
        }
    }

    public void unregisterAll()
    {
        for ( Command command : commands )
        {
            command.unload();
        }
        commands.clear();
    }

    public Optional<Command> findCommandByName( final String name )
    {
        return this.commands.stream()
            .filter( command ->
                command.getName().equalsIgnoreCase( name )
                    || Arrays.stream( command.getAliases() ).anyMatch( alias -> alias.equalsIgnoreCase( name ) ) )
            .findFirst();
    }

    private List<String> getParameterList( final ISection parameterSection )
    {
        final List<String> parameters = Lists.newArrayList();

        for ( String key : parameterSection.getKeys() )
        {
            if ( parameterSection.getBoolean( key ) )
            {
                parameters.add( "-" + key );
            }
        }

        return parameters;
    }
}
