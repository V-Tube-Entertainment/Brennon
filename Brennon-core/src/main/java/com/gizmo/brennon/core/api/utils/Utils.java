package com.gizmo.brennon.core.api.utils;

import com.gizmo.brennon.core.BuX;
import com.gizmo.brennon.core.api.language.LanguageConfig;
import com.gizmo.brennon.core.api.placeholder.PlaceHolderAPI;
import com.gizmo.brennon.core.api.user.UserStorage;
import com.gizmo.brennon.core.api.user.interfaces.User;
import com.gizmo.brennon.core.api.utils.placeholders.HasMessagePlaceholders;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import dev.endoy.configuration.api.IConfiguration;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.CodeSource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils
{

    private static final Pattern timePattern = Pattern.compile( "(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?"
        + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?"
        + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE );

    private Utils()
    {
    }

    public static String formatString( final User user, final String message )
    {
        return PlaceHolderAPI.formatMessage( user, message );
    }

    /**
     * Formats a message, translates color codes and replaces general placeholders.
     *
     * @param message The message to be formatted.
     * @return The formatted message.
     */
    public static String formatString( final String message )
    {
        return UnicodeTranslator.translate( PlaceHolderAPI.formatMessage( message ) );
    }

    /**
     * Formats a message to TextComponent, translates color codes and replaces general placeholders.
     *
     * @param message The message to be formatted.
     * @return The formatted message.
     */
    public static Component format( final String message )
    {
        return asComponent( UnicodeTranslator.translate( PlaceHolderAPI.formatMessage( message ) ) );
    }

    /**
     * Wraps a string as a BaseComponent.
     *
     * @param message The message to be wrapped.
     * @return The wrapped message.
     */
    public static Component asComponent( final String message )
    {
        return MessageUtils.fromText( message );
    }

    /**
     * Formats a message to TextComponent, translates color codes and replaces placeholders.
     *
     * @param messages The messages to be formatted.
     * @return The formatted message.
     */
    public static Component format( final List<String> messages )
    {
        return format( null, messages );
    }

    /**
     * Formats a message to TextComponent, translates color codes and replaces placeholders.
     *
     * @param user    The user for which the placeholders should be formatted.
     * @param message The message to be formatted.
     * @return The formatted message.
     */
    public static Component format( final User user, final String message )
    {
        return MessageUtils.fromText( formatString( user, message ) );
    }

    /**
     * Formats a message to TextComponent, translates color codes and replaces placeholders.
     *
     * @param user     The user for which the placeholders should be formatted.
     * @param messages The messages to be formatted.
     * @return The formatted message.
     */
    public static Component format( final User user, final List<String> messages )
    {
        Component component = Component.empty();

        for ( int i = 0; i < messages.size(); i++ )
        {
            String message = messages.get( i );

            component = component.append( format( user, i + 1 >= messages.size() ? message : message + "\n" ) );
        }

        return component;
    }

    /**
     * Formats a message to TextComponent with given prefix.
     *
     * @param prefix  The prefix to be before the message.
     * @param message The message to be formatted.
     * @return The formatted message.
     */
    public static Component format( final String prefix, final String message )
    {
        return format( prefix + message );
    }

    /**
     * Util to get a key from value in a map.
     *
     * @param map   The map to get a key by value.
     * @param value The value to get thekey from.
     * @param <K>   The key type.
     * @param <V>   The value type
     * @return The key bound to the requested value.
     */
    public static <K, V> K getKeyFromValue( final Map<K, V> map, final V value )
    {
        for ( Map.Entry<K, V> entry : map.entrySet() )
        {
            if ( entry.getValue().equals( value ) )
            {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * @return The current date (dd-MM-yyyy)
     */
    public static String getCurrentDate()
    {
        SimpleDateFormat sdf = new SimpleDateFormat( "dd-MM-yyyy" );
        return sdf.format( new Date( System.currentTimeMillis() ) );
    }

    /**
     * @return The current time (kk:mm:ss)
     */
    public static String getCurrentTime()
    {
        SimpleDateFormat sdf = new SimpleDateFormat( "kk:mm:ss" );
        return sdf.format( new Date( System.currentTimeMillis() ) );
    }

    /**
     * @param stream The stream you want to read.
     * @return A list containing all lines from the input stream.
     */
    public static List<String> readFromStream( final InputStream stream )
    {
        final List<String> lines = Lists.newArrayList();

        try ( InputStream input = stream;
              InputStreamReader inputStreamReader = new InputStreamReader( input );
              BufferedReader reader = new BufferedReader( inputStreamReader ) )
        {
            reader.lines().forEach( lines::add );
        }
        catch ( IOException ignored )
        {
            // ignored
        }

        return lines;
    }

    /**
     * Attempts to parse a long time from a given string.
     *
     * @param time The string you want to parse relative to the current time.
     * @return The time, in MILLIS, you requested.
     */
    public static long parseDateDiff( String time )
    {
        return parseDateDiff( time, false );
    }

    /**
     * Attempts to parse a long time from a given string.
     *
     * @param time The string you want to parse relative to the current time.
     * @return The time, in MILLIS, you requested.
     */
    public static long parseDateDiffInPast( String time )
    {
        return parseDateDiff( time, true );
    }

    /**
     * Attempts to parse a long time from a given string.
     *
     * @param time The string you want to parse relative to the current time.
     * @return The time, in MILLIS, you requested.
     */
    public static long parseDateDiff( String time, boolean history )
    {
        try
        {
            final Matcher m = timePattern.matcher( time );
            int years = 0;
            int months = 0;
            int weeks = 0;
            int days = 0;
            int hours = 0;
            int minutes = 0;
            int seconds = 0;
            boolean found = false;
            while ( m.find() )
            {
                if ( m.group() == null || m.group().isEmpty() )
                {
                    continue;
                }
                for ( int i = 0; i < m.groupCount(); i++ )
                {
                    if ( m.group( i ) != null && !m.group( i ).isEmpty() )
                    {
                        found = true;
                        break;
                    }
                }
                if ( found )
                {
                    if ( m.group( 1 ) != null && !m.group( 1 ).isEmpty() )
                    {
                        years = Integer.parseInt( m.group( 1 ) );
                    }
                    if ( m.group( 2 ) != null && !m.group( 2 ).isEmpty() )
                    {
                        months = Integer.parseInt( m.group( 2 ) );
                    }
                    if ( m.group( 3 ) != null && !m.group( 3 ).isEmpty() )
                    {
                        weeks = Integer.parseInt( m.group( 3 ) );
                    }
                    if ( m.group( 4 ) != null && !m.group( 4 ).isEmpty() )
                    {
                        days = Integer.parseInt( m.group( 4 ) );
                    }
                    if ( m.group( 5 ) != null && !m.group( 5 ).isEmpty() )
                    {
                        hours = Integer.parseInt( m.group( 5 ) );
                    }
                    if ( m.group( 6 ) != null && !m.group( 6 ).isEmpty() )
                    {
                        minutes = Integer.parseInt( m.group( 6 ) );
                    }
                    if ( m.group( 7 ) != null && !m.group( 7 ).isEmpty() )
                    {
                        seconds = Integer.parseInt( m.group( 7 ) );
                    }
                    break;
                }
            }
            if ( !found )
            {
                return 0;
            }
            if ( years > 25 )
            {
                return 0;
            }
            final Calendar c = new GregorianCalendar();
            if ( years > 0 )
            {
                c.add( Calendar.YEAR, history ? -years : years );
            }
            if ( months > 0 )
            {
                c.add( Calendar.MONTH, history ? -months : months );
            }
            if ( weeks > 0 )
            {
                c.add( Calendar.WEEK_OF_YEAR, history ? -weeks : weeks );
            }
            if ( days > 0 )
            {
                c.add( Calendar.DAY_OF_MONTH, history ? -days : days );
            }
            if ( hours > 0 )
            {
                c.add( Calendar.HOUR_OF_DAY, history ? -hours : hours );
            }
            if ( minutes > 0 )
            {
                c.add( Calendar.MINUTE, history ? -minutes : minutes );
            }
            if ( seconds > 0 )
            {
                c.add( Calendar.SECOND, history ? -seconds : seconds );
            }
            return c.getTimeInMillis();
        }
        catch ( NumberFormatException e )
        {
            return 0;
        }
    }

    /**
     * Checks if given parameter is a Boolean.
     *
     * @param object The object you want to check.
     * @return True if Boolean, false if not.
     */
    public static boolean isBoolean( final Object object )
    {
        try
        {
            Boolean.parseBoolean( object.toString() );
            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
    }

    /**
     * Capitalizes first letter of every word found.
     *
     * @param words The string you want to capitalize.
     * @return A new capitalized String.
     */
    public static String capitalizeWords( final String words )
    {
        if ( words != null && words.length() != 0 )
        {
            char[] chars = words.toCharArray();
            char[] newCharacters = new char[chars.length];

            char lastChar = ' ';
            for ( int i = 0; i < chars.length; i++ )
            {
                char character = chars[i];

                if ( lastChar == ' ' )
                {
                    newCharacters[i] = Character.toUpperCase( character );
                }
                else
                {
                    newCharacters[i] = character;
                }
            }

            return new String( newCharacters );
        }
        else
        {
            return words;
        }
    }

    /**
     * Converts a InetSocketAddress into a String IPv4.
     *
     * @param a The address to be converted.
     * @return The converted address as a String.
     */
    public static String getIP( final InetSocketAddress a )
    {
        return getIP( a.getAddress() );
    }

    /**
     * Converts a InetAddress into a String IPv4.
     *
     * @param a The address to be converted.
     * @return The converted address as a String.
     */
    public static String getIP( final InetAddress a )
    {
        return a.toString().split( "/" )[1].split( ":" )[0];
    }

    /**
     * Formatting a list into a string with given seperators.
     *
     * @param objects   Iterable which has to be converted.
     * @param separator Seperator which will be used to seperate the list.
     * @return A string in which all sendable of the list are seperated by the separator.
     */
    public static String formatList( final Iterable<?> objects, final String separator )
    {
        if ( objects == null )
        {
            return null;
        }
        return Joiner.on( separator ).join( objects );
    }

    /**
     * Similar to {@link #formatList(Iterable, String)} but for Arrays.
     *
     * @param objects   Array which has to be converted.
     * @param separator Seperator which will be used to seperate the array.
     * @return A string in which all sendable of the array are seperated by the separator.
     */
    public static String formatList( final Object[] objects, final String separator )
    {
        if ( objects == null )
        {
            return null;
        }
        return Joiner.on( separator ).join( objects );
    }

    /**
     * Checks if a class is present or not.
     *
     * @param clazz The class to be checked.
     * @return True if found, false if not.
     */
    public static boolean classFound( final String clazz )
    {
        try
        {
            Class.forName( clazz );
            return true;
        }
        catch ( ClassNotFoundException e )
        {
            return false;
        }
    }

    /**
     * Formats current time into the following date format: "dd-MM-yyyy kk:mm:ss"
     *
     * @return a formatted date string.
     */
    public static String getFormattedDate()
    {
        return formatDate( new Date( System.currentTimeMillis() ) );
    }

    /**
     * Formats current date into a custom date format.
     *
     * @param format The date format to be used.
     * @return a formatted date string.
     */
    public static String getFormattedDate( final String format )
    {
        return formatDate( format, new Date( System.currentTimeMillis() ) );
    }

    /**
     * Formats the given date into the following format: "dd-MM-yyyy kk:mm:ss"
     *
     * @param date The date to be formatted.
     * @return a formatted date string.
     */
    public static String formatDate( final Date date )
    {
        SimpleDateFormat sdf = new SimpleDateFormat( "dd-MM-yyyy kk:mm:ss" );
        return sdf.format( date );
    }

    /**
     * Formats the given date into the following format: "dd-MM-yyyy kk:mm:ss"
     *
     * @param date           The date to be formatted.
     * @param languageConfig The config to take the date format from
     * @return a formatted date string.
     */
    public static String formatDate( final Date date, final IConfiguration languageConfig )
    {
        return formatDate( languageConfig.getString( "date-format" ), date );
    }

    /**
     * Formats a given date into the given format.
     *
     * @param format The date format to be used.
     * @param date   The date to be formatted.
     * @return a formatted date string.
     */
    public static String formatDate( final String format, final Date date )
    {
        SimpleDateFormat sdf = new SimpleDateFormat( format );
        return sdf.format( date );
    }

    /**
     * Executes enum valueOf, if that throws an error, a default is returned.
     *
     * @param name The name to be used.
     * @param def  The default value.
     * @param <T>  The enum type.
     * @return Parsed enum or default.
     */
    public static <T extends Enum<T>> T valueOfOr( final String name, final T def )
    {
        return valueOfOr( (Class<T>) def.getClass(), name, def );
    }

    /**
     * Executes enum valueOf, if that throws an error, a default is returned.
     *
     * @param clazz The enum class
     * @param name  The name to be used.
     * @param def   The default value.
     * @param <T>   The enum type.
     * @return Parsed enum or default.
     */
    public static <T extends Enum<T>> T valueOfOr( final Class<T> clazz, final String name, final T def )
    {
        try
        {
            T value = Enum.valueOf( clazz, name );

            return value == null ? def : value;
        }
        catch ( IllegalArgumentException e )
        {
            return def;
        }
    }

    /**
     * Reads UUID from string, if it's undashed, it will add these.
     *
     * @param str The UUID to be formatted
     * @return UUID object of the entered uuid
     */
    public static UUID readUUIDFromString( final String str )
    {
        try
        {
            return UUID.fromString( str );
        }
        catch ( IllegalArgumentException e )
        {
            return UUID.fromString(
                str.replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                    "$1-$2-$3-$4-$5"
                )
            );
        }
    }

    /**
     * Copies all elements from the iterable collection of originals to the
     * collection provided.
     *
     * @param <T>        the collection of strings
     * @param token      String to search for
     * @param originals  An iterable collection of strings to filter.
     * @param collection The collection to add matches to
     * @return the collection provided that would have the elements copied
     * into
     * @throws UnsupportedOperationException if the collection is immutable
     *                                       and originals contains a string which starts with the specified
     *                                       search string.
     * @throws IllegalArgumentException      if any parameter is null
     * @throws IllegalArgumentException      if originals contains a null element.
     */
    public static <T extends Collection<? super String>> T copyPartialMatches( final String token, final Iterable<String> originals, final T collection ) throws UnsupportedOperationException, IllegalArgumentException
    {
        for ( String string : originals )
        {
            if ( startsWithIgnoreCase( string, token ) )
            {
                collection.add( string );
            }
        }

        return collection;
    }

    /**
     * This method uses a region to check case-insensitive equality. This
     * means the internal array does not need to be copied like a
     * toLowerCase() call would.
     *
     * @param string String to check
     * @param prefix Prefix of string to compare
     * @return true if provided string starts with, ignoring case, the prefix
     * provided
     * @throws NullPointerException     if prefix is null
     * @throws IllegalArgumentException if string is null
     */
    public static boolean startsWithIgnoreCase( final String string, final String prefix ) throws IllegalArgumentException, NullPointerException
    {
        if ( string.length() < prefix.length() )
        {
            return false;
        }
        return string.regionMatches( true, 0, prefix, 0, prefix.length() );
    }

    /**
     * Replaces placeholders in a string.
     *
     * @param message      the message to replace in
     * @param placeholders the placeholders with their values to be replaced
     * @return the message with the replaced placeholders.
     */
    public static String replacePlaceHolders( final String message, final HasMessagePlaceholders placeholders )
    {
        return replacePlaceHolders( null, message, placeholders );
    }

    /**
     * Replaces placeholders in a string and formats using the placeholder api.
     *
     * @param user         the user to format placeholders for
     * @param message      the message to replace in
     * @param placeholders the placeholders with their values to be replaced
     * @return the message with the replaced placeholders.
     */
    public static String replacePlaceHolders( final User user, String message, final HasMessagePlaceholders placeholders )
    {
        return replacePlaceHolders( user, message, null, null, placeholders );
    }

    /**
     * Replaces placeholders in a string.
     *
     * @param message                  the message to replace in
     * @param prePlaceholderFormatter  executed before replacing placeholders
     * @param postPlaceholderFormatter executed after replacing placeholders
     * @param placeholders             the placeholders with their values to be replaced
     * @return the message with the replaced placeholders.
     */
    public static String replacePlaceHolders( final String message,
                                              final Function<String, String> prePlaceholderFormatter,
                                              final Function<String, String> postPlaceholderFormatter,
                                              final HasMessagePlaceholders placeholders )
    {
        return replacePlaceHolders( null, message, prePlaceholderFormatter, postPlaceholderFormatter, placeholders );
    }

    /**
     * Replaces placeholders in a string.
     *
     * @param user                     the user to format placeholders for
     * @param message                  the message to replace in
     * @param prePlaceholderFormatter  executed before replacing placeholders
     * @param postPlaceholderFormatter executed after replacing placeholders
     * @param placeholders             the placeholders with their values to be replaced
     * @return the message with the replaced placeholders.
     */
    public static String replacePlaceHolders( final User user,
                                              String message,
                                              final Function<String, String> prePlaceholderFormatter,
                                              final Function<String, String> postPlaceholderFormatter,
                                              final HasMessagePlaceholders placeholders )
    {
        if ( prePlaceholderFormatter != null )
        {
            message = prePlaceholderFormatter.apply( message );
        }
        message = placeholders.getMessagePlaceholders().format( message );
        message = PlaceHolderAPI.formatMessage( user, message );
        if ( postPlaceholderFormatter != null )
        {
            message = postPlaceholderFormatter.apply( message );
        }
        return message;
    }

    /**
     * Replaces the last occurence of a keyword in the given string.
     *
     * @param string      the string to replace in
     * @param toReplace   the string to search on
     * @param replacement the string that should be used to replace
     * @return the given string with (possible) replacement done
     */
    public static String replaceLast( final String string, final String toReplace, final String replacement )
    {
        int pos = string.lastIndexOf( toReplace );
        if ( pos > -1 )
        {
            return string.substring( 0, pos )
                + replacement
                + string.substring( pos + toReplace.length() );
        }
        else
        {
            return string;
        }
    }

    /**
     * @param chars  the characters to use for the random string
     * @param length the length that the random string should be
     * @return a random string of the requested characters and length
     */
    public static String createRandomString( final String chars, final int length )
    {
        final StringBuilder sb = new StringBuilder();
        final Random random = new Random();

        for ( int i = 0; i < length; i++ )
        {
            sb.append( chars.charAt( random.nextInt( chars.length() ) ) );
        }

        return sb.toString();
    }

    /**
     * Gets the time left until a certain date
     *
     * @param format the format that should be replaced
     * @param date   the date to calculate against
     * @return time left until the date
     */
    public static String getTimeLeft( final String format, final Date date )
    {
        return getTimeLeft( format, date.getTime() - System.currentTimeMillis() );
    }

    /**
     * Gets the time left until a certain time
     *
     * @param format the format that should be replaced
     * @param millis the time to calculate against
     * @return time left until the date
     */
    public static String getTimeLeft( final String format, final long millis )
    {
        final long seconds = millis / 1000;
        final long minutes = seconds / 60;
        final long hours = minutes / 60;
        final long days = hours / 24;

        return format
            .replace( "%days%", String.valueOf( days ) )
            .replace( "%hours%", String.valueOf( hours % 24 ) )
            .replace( "%minutes%", String.valueOf( minutes % 60 ) )
            .replace( "%seconds%", String.valueOf( seconds % 60 ) );
    }

    /**
     * Gets the LanguageConfig for a certain user, or default if the user is null
     *
     * @param user the user (or null) to get the language config for
     * @return the language config for the given user, or null
     */
    public static LanguageConfig getLanguageConfiguration( User user )
    {
        if ( user == null )
        {
            return BuX.getApi().getLanguageManager().getConfig(
                BuX.getInstance().getName(),
                BuX.getApi().getLanguageManager().getDefaultLanguage()
            );
        }
        return user.getLanguageConfig();
    }

    /**
     * Gets the classes inside a package for the BuX jar
     *
     * @param packageName the package to get the classes for
     * @return a list of classes inside the given BuX package
     */
    @SneakyThrows
    public static List<Class<?>> getClassesInPackage( final String packageName )
    {
        final List<Class<?>> classes = ClassPath.from( BuX.class.getClassLoader() )
            .getTopLevelClassesRecursive( packageName )
            .stream()
            .map( ClassPath.ClassInfo::load )
            .collect( Collectors.toList() );

        BuX.debug( "Found " + classes + " classes in package " + packageName );

        if ( classes.isEmpty() )
        {
            BuX.debug( "Class list is empty" );
            final CodeSource src = BuX.class.getProtectionDomain().getCodeSource();

            if ( src != null )
            {
                BuX.debug( "FOUND SRC" );
                try
                {
                    final URL jar = src.getLocation();
                    final ZipInputStream zip = new ZipInputStream( jar.openStream() );

                    while ( true )
                    {
                        final ZipEntry e = zip.getNextEntry();
                        if ( e == null )
                        {
                            break;
                        }
                        final String name = e.getName().replace( "/", "." );

                        BuX.debug( "class name: " + name );

                        if ( name.startsWith( packageName ) )
                        {
                            BuX.debug( "FOUND CLASS: " + name );

                            if ( name.endsWith( ".class" ) )
                            {
                                classes.add( Class.forName( name.replace( ".class", "" ) ) );
                            }
                        }
                    }
                }
                catch ( IOException | ClassNotFoundException e )
                {
                    e.printStackTrace();
                }
            }
        }

        return classes
            .stream()
            .sorted( Comparator.comparing( ( Class<?> o ) -> o.getSimpleName() ) )
            .toList();
    }

    /**
     * Gets the user storage for a specific user, or null if the user never joined
     *
     * @param name the name to get the data for
     * @return the data for the given user
     */
    public static UserStorage getUserStorageIfUserExists( final String name )
    {
        return getUserStorageIfUserExists( BuX.getApi().getUser( name ).orElse( null ), name );
    }

    /**
     * Gets the user storage for a specific user, or null if the user never joined
     *
     * @param user the user to take cached data for if not null
     * @param name the name to get the data for
     * @return the data for the given user
     */
    @SneakyThrows
    public static UserStorage getUserStorageIfUserExists( final User user, final String name )
    {
        final UserStorage storage;

        if ( user != null )
        {
            storage = user.getStorage();
        }
        else
        {
            storage = BuX.getApi().getStorageManager().getDao().getUserDao().getUserData( name ).join().orElse( null );

            if ( storage == null || !storage.isLoaded() )
            {
                return null;
            }
        }

        return storage;
    }

    /**
     * @param obj the obj to check
     * @param def the default value
     * @param <T> type of obj
     * @return the object given, or default if null
     */
    public static <T> T nullToDefault( final T obj, final T def )
    {
        if ( obj == null )
        {
            return def;
        }
        return obj;
    }

    /**
     * @param string the string to check
     * @param def    the default value
     * @return the object given, or default if null
     */
    public static String blankToDefault( final String string, final String def )
    {
        if ( string == null || string.isBlank() )
        {
            return def;
        }
        return string;
    }

    /**
     * @param optionals the optionals to be checked
     * @param <T>       type
     * @return the first optional with a value, or an empty if none
     */
    @SafeVarargs
    public static <T> Optional<T> firstPresent( final Optional<T>... optionals )
    {
        for ( Optional<T> optional : optionals )
        {
            if ( optional.isPresent() )
            {
                return optional;
            }
        }
        return Optional.empty();
    }
}