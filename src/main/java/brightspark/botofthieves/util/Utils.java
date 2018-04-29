package brightspark.botofthieves.util;

import brightspark.botofthieves.BotOfThieves;
import brightspark.botofthieves.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import javax.annotation.Nullable;
import java.awt.*;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Utils
{
    public static String getFullUser(User user)
    {
        return user.getName() + "#" + user.getDiscriminator();
    }

    /**
     * Finds a text channel using an ID or name
     */
    public static TextChannel findTextChannel(String channel)
    {
        TextChannel channelFound = null;
        try
        {
            long channelId = Long.parseLong(channel);
            channelFound = BotOfThieves.JDA.getTextChannelById(channelId);
            if(channelFound == null) BotOfThieves.LOG.warn("Channel with ID " + channel + " not found");
        }
        catch(NumberFormatException e)
        {
            List<TextChannel> channels = BotOfThieves.JDA.getTextChannelsByName(channel, false);
            if(!channels.isEmpty()) channelFound = channels.get(0);
            else BotOfThieves.LOG.warn("Channel '" + channel + "' not found");
        }
        return channelFound;
    }

    /**
     * Finds a voice channel using an ID or name
     */
    public static VoiceChannel findVoiceChannel(String channel)
    {
        VoiceChannel channelFound = null;
        try
        {
            long channelId = Long.parseLong(channel);
            channelFound = BotOfThieves.JDA.getVoiceChannelById(channelId);
            if(channelFound == null) BotOfThieves.LOG.warn("Channel with ID " + channel + " not found");
        }
        catch(NumberFormatException e)
        {
            List<VoiceChannel> channels = BotOfThieves.JDA.getVoiceChannelByName(channel, false);
            if(!channels.isEmpty()) channelFound = channels.get(0);
            else BotOfThieves.LOG.warn("Channel '" + channel + "' not found");
        }
        return channelFound;
    }

    /**
     * Finds a role using an ID or name
     */
    public static Role findRole(String role)
    {
        Role roleFound = null;
        try
        {
            long roleId = Long.parseLong(role);
            roleFound = BotOfThieves.JDA.getRoleById(roleId);
            if(roleFound == null) BotOfThieves.LOG.warn("Role with ID " + role + " not found");
        }
        catch(NumberFormatException e)
        {
            List<Role> roles = BotOfThieves.JDA.getRolesByName(role, false);
            if(!roles.isEmpty()) roleFound = roles.get(0);
            else BotOfThieves.LOG.warn("Role '" + role + "' not found");
        }
        return roleFound;
    }

    /**
     * Finds a category using an ID or name
     */
    public static Category findCategory(String category)
    {
        Category categoryFound = null;
        try
        {
            long categoryId = Long.parseLong(category);
            categoryFound = BotOfThieves.JDA.getCategoryById(categoryId);
            if(categoryFound == null) BotOfThieves.LOG.warn("Category with ID " + category + " not found");
        }
        catch(NumberFormatException e)
        {
            List<Category> categories = BotOfThieves.JDA.getCategoriesByName(category, false);
            if(!categories.isEmpty()) categoryFound = categories.get(0);
            else BotOfThieves.LOG.warn("Category '" + category + "' not found");
        }
        return categoryFound;
    }

    /**
     * Logs to the assigned log channel if it has been set
     */
    public static void logChannel(LogLevel level, User author, String text)
    {
        if(BotOfThieves.LOG_CHANNEL != null && level.isHigherThanOrEqualTo(Config.get("log_level", "error")))
        {
            if(author == null)
                author = BotOfThieves.JDA.getSelfUser();
            EmbedBuilder message = new EmbedBuilder();
            message.setColor(level.colour);
            message.setAuthor(getFullUser(author), null, author.getEffectiveAvatarUrl());
            message.setTitle(level.toString());
            message.setDescription(text);
            message.setTimestamp(Instant.now());
            BotOfThieves.LOG_CHANNEL.sendMessage(message.build()).queue();
        }
    }

    public static void logChannel(LogLevel level, String text)
    {
        logChannel(level, null, text);
    }

    public static Color getBotColour(Guild guild)
    {
        return guild == null ? Color.BLUE : guild.getMember(BotOfThieves.JDA.getSelfUser()).getColor();
    }

    /**
     * Creates an embedded message for the bot to send (uses the bot's main role colour)
     */
    public static MessageEmbed createBotMessage(Guild guild, String message, boolean bold)
    {
        return createEmbedMessage(getBotColour(guild), bold ? message : null, bold ? null : message);
    }

    /**
     * Creates an embedded message for the bot to send (uses the bot's main role colour)
     */
    public static MessageEmbed createBotMessage(Guild guild, String title, String description)
    {
        return createEmbedMessage(getBotColour(guild), title, description);
    }

    /**
     * Creates an EmbedBuilder already with the bot colour set
     */
    public static EmbedBuilder createBotMessageTemplate(Guild guild)
    {
        return new EmbedBuilder().setColor(getBotColour(guild));
    }

    /**
     * Creates a simple embedded message
     */
    public static MessageEmbed createEmbedMessage(Color colour, @Nullable String title, @Nullable String description)
    {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(colour);
        if(title != null)
            builder.setTitle(title);
        if(description != null)
            builder.setDescription(description);
        return builder.build();
    }

    public static String millisTimeToReadable(long millis)
    {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);

        if(hours == 0)
            return String.format("%d minutes", minutes);
        else
            return String.format("%d hours, %d minutes", hours, minutes);
    }

    public static long extractMillisFromTimes(String times)
    {
        long time = 0;
        String[] split = times.split(" ");
        for(String s : split)
        {
            if(s.matches("\\d+[smhd]"))
            {
                TimeUnit unit = extractUnitFromTime(s);
                if(unit == null) return - 1;
                time += unit.toMillis(Integer.parseInt(s.substring(0, s.length() - 1)));
            }
            else
                return -1;
        }
        return time;
    }

    public static TimeUnit extractUnitFromTime(String time)
    {
        char unitChar = time.toLowerCase().charAt(time.length() - 1);
        switch(unitChar)
        {
            case 's':   return TimeUnit.SECONDS;
            case 'm':   return TimeUnit.MINUTES;
            case 'h':   return TimeUnit.HOURS;
            case 'd':   return TimeUnit.DAYS;
            default:    return null;
        }
    }

    /**
     * Capitalises the first letter of the string
     */
    public static String capitaliseFirstLetter(String text)
    {
        if(text == null || text.length() <= 0)
            return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static String commaSeparate(long num)
    {
        return NumberFormat.getIntegerInstance().format(num);
    }

    public static String joinStrings(String[] array, int start)
    {
        return joinStrings(array, start, array.length);
    }

    public static String joinStrings(String[] array, int start, int end)
    {
        StringBuilder sb = new StringBuilder();
        for(int i = start; i < end; i++)
        {
            if(i > start) sb.append(" ");
            sb.append(array[i]);
        }
        return sb.toString();
    }
}
