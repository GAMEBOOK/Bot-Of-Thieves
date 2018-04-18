package brightspark.botofthieves.util;

import brightspark.botofthieves.BotOfThieves;
import brightspark.botofthieves.Config;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;

import javax.annotation.Nullable;
import java.awt.*;
import java.time.Instant;
import java.util.List;

public class Utils
{
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
}
