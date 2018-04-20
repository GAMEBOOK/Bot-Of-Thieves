package brightspark.botofthieves.util;

import brightspark.botofthieves.BotOfThieves;
import brightspark.botofthieves.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nullable;
import java.awt.*;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Utils
{
    public static final String EMOJI_GREEN_HEART = "\uD83D\uDC9A";
    public static final String EMOJI_NAME_BADGE = "\uD83D\uDCDB";
    public static final String EMOJI_ANCHOR = "\u2693";
    public static final String EMOJI_SKULL_CROSSBONES = "\uD83D\uDC80";
    public static final String EMOJI_SAILBOAT = "\u26F5";

    public static String getFullUser(User user)
    {
        return user.getName() + "#" + user.getDiscriminator();
    }

    /**
     * Finds a channel using an ID or name
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
     * Logs to the assigned log channel if it has been set
     */
    public static void logChannel(LogLevel level, User author, String text, Object... args)
    {
        if(BotOfThieves.LOG_CHANNEL != null && level.isHigherThanOrEqualTo(Config.get("log_level", "error")))
        {
            if(author == null)
                author = BotOfThieves.JDA.getSelfUser();
            EmbedBuilder message = new EmbedBuilder();
            message.setColor(level.colour);
            message.setAuthor(getFullUser(author), null, author.getEffectiveAvatarUrl());
            message.setTitle(level.toString());
            message.setDescription(String.format(text, args));
            message.setTimestamp(Instant.now());
            BotOfThieves.LOG_CHANNEL.sendMessage(message.build()).queue();
        }
    }

    public static void logChannel(LogLevel level, String text, Object... args)
    {
        logChannel(level, null, text, args);
    }

    public static Color getBotColour(Guild guild)
    {
        return guild == null ? Color.BLUE : guild.getMember(BotOfThieves.JDA.getSelfUser()).getColor();
    }

    /**
     * Creates an embedded message for the bot to send (uses the bot's main role colour)
     */
    public static MessageEmbed createBotMessage(Guild guild, String message, Object... args)
    {
        return createEmbedMessage(getBotColour(guild), String.format(message, args), null);
    }

    /**
     * Creates an embedded message for the bot to send (uses the bot's main role colour)
     */
    public static MessageEmbed createBotMessage(Guild guild, String title, String description, Object... args)
    {
        return createEmbedMessage(getBotColour(guild), title, String.format(description, args));
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

    public static Duration extractDurationFromTimes(String times)
    {
        Duration duration = Duration.ZERO;
        String[] split = times.split(" ");
        for(String s : split)
        {
            if(s.matches("\\d+[smhd]"))
                duration = duration.plus(Integer.parseInt(s.substring(0, s.length() - 1)), extractUnitFromTime(s));
            else
                return null;
        }
        return duration;
    }

    public static ChronoUnit extractUnitFromTime(String time)
    {
        char unitChar = time.toLowerCase().charAt(time.length() - 1);
        switch(unitChar)
        {
            case 's':   return ChronoUnit.SECONDS;
            case 'm':   return ChronoUnit.MINUTES;
            case 'h':   return ChronoUnit.HOURS;
            case 'd':   return ChronoUnit.DAYS;
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
}
