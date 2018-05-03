package brightspark.botofthieves.commands;

import brightspark.botofthieves.BotOfThieves;
import brightspark.botofthieves.util.LogLevel;
import brightspark.botofthieves.util.Utils;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public abstract class CommandBase extends Command
{
    protected final Logger LOG = LoggerFactory.getLogger(getClass());
    private boolean removeSentMessage = false;
    private boolean dmOnly = false;

    public CommandBase(String name, String help, String... aliases)
    {
        this.name = name;
        this.help = help;
        if(aliases != null) this.aliases = aliases;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        if(removeSentMessage)
            event.getMessage().delete().queue();
        if(dmOnly && event.getChannelType() != ChannelType.PRIVATE)
            return;
        info("%s executed command '%s'", Utils.getFullUser(event.getAuthor()), event.getMessage().getContentDisplay());
        doCommand(event, splitArgs(event.getArgs()));
    }

    protected abstract void doCommand(CommandEvent event, String... args);

    private static String[] splitArgs(String args)
    {
        return args.isEmpty() ? new String[] {} : args.split("\\s+");
    }

    /**
     * Gets the user from the guild if they exist.
     * @param userString Can be either the user's display name or their @ mention.
     */
    protected User getUserFromString(Guild guild, String userString)
    {
        if(guild == null) return getUserFromString(userString);

        User user = null;

        //If the user is tagged, extract just the user ID
        if(userString.startsWith("<@"))
            userString = userString.substring(2, userString.length() - 1);

        try
        {
            //Try parse argument as a user ID
            Member member = guild.getMemberById(userString);
            if(member != null) user = member.getUser();
        }
        catch(NumberFormatException ignored) {}

        if(user == null)
        {
            //Try parse argument as a member name
            List<Member> members = guild.getMembersByEffectiveName(userString, true);
            if(!members.isEmpty()) user = members.get(0).getUser();
        }

        return user == null ? getUserFromString(userString) : user;
    }

    /**
     * Gets the user if they exist.
     * @param userString Can be either the user's display name or their @ mention.
     */
    protected User getUserFromString(String userString)
    {
        User user = null;

        //If the user is tagged, extract just the user ID
        if(userString.startsWith("<@"))
            userString = userString.substring(2, userString.length() - 1);

        try
        {
            //Try parse argument as a user ID
            user = BotOfThieves.JDA.getUserById(userString);
        }
        catch(NumberFormatException ignored) {}

        if(user == null)
        {
            //Try parse argument as a member name
            List<User> users = BotOfThieves.JDA.getUsersByName(userString, true);
            if(!users.isEmpty()) user = users.get(0);
        }

        return user;
    }

    protected boolean checkMemberPerms(Member member)
    {
        if(member.isOwner()) return true;
        for(Role role : member.getRoles())
            if(role.equals(BotOfThieves.ADMIN_ROLE))
                return true;
        return false;
    }

    protected void setRemoveSentMessage()
    {
        removeSentMessage = true;
    }

    protected void setDmOnly()
    {
        guildOnly = false;
        dmOnly = true;
    }

    protected void replySuccess(CommandEvent event, String message)
    {
        reply(event, null, String.format("%s %s", event.getClient().getSuccess(), message));
    }

    protected void replySuccess(CommandEvent event, String title, String desc)
    {
        reply(event, String.format("%s %s", event.getClient().getSuccess(), title), desc);
    }

    protected void replyWarning(CommandEvent event, String message)
    {
        reply(event, null, String.format("%s %s", event.getClient().getWarning(), message));
    }

    protected void replyWarning(CommandEvent event, String title, String desc)
    {
        reply(event, String.format("%s %s", event.getClient().getWarning(), title), desc);
    }

    protected void replyError(CommandEvent event, String message)
    {
        reply(event, null, String.format("%s %s", event.getClient().getError(), message));
        warn("Command '%s' execution failed: %s", event.getMessage().getContentDisplay(), message);
        if(event.getClient().getListener() != null)
            event.getClient().getListener().onTerminatedCommand(event, this);
    }

    public void reply(CommandEvent event, @Nullable String title, String desc)
    {
        MessageChannel channel = event.getChannel();
        debug("Sending message to %s channel %s -> Title: %s # Desc: %s",
                channel.getType(), channel.getName(), title, desc);
        switch(channel.getType())
        {
            case PRIVATE:
            case GROUP:
                event.reply(title == null ? desc : String.format("**%s**\n%s", title, desc));
                return;
            case TEXT:
                if(title == null)
                    event.reply(Utils.createBotMessage(event.getGuild(), desc, false));
                else
                    event.reply(Utils.createBotMessage(event.getGuild(), title, desc));
                return;
            default:
                error("Can't send message to a %s channel! Doing nothing", channel.getType());
        }
    }

    protected void reply(CommandEvent event, MessageEmbed message)
    {
        debug("Sending message to %s channel %s -> Title: %s # Desc: %s",
                event.getChannel().getType(), event.getChannel().getName(), message.getTitle(), message.getDescription());
        event.reply(message);
    }

    protected void replyWithConsumer(CommandEvent event, MessageEmbed message, Consumer<Message> onSuccess)
    {
        debug("Sending message to %s channel %s -> Title: %s # Desc: %s",
                event.getChannel().getType(), event.getChannel().getName(), message.getTitle(), message.getDescription());
        event.reply(message, onSuccess);
    }

    protected void info(String message, Object... args)
    {
        String m = String.format(message, args);
        LOG.info(m);
        Utils.logChannel(LogLevel.INFO, m);
    }

    protected void debug(String message, Object... args)
    {
        String m = String.format(message, args);
        LOG.debug(m);
        Utils.logChannel(LogLevel.DEBUG, m);
    }

    protected void warn(String message, Object... args)
    {
        String m = String.format(message, args);
        LOG.warn(m);
        Utils.logChannel(LogLevel.WARN, m);
    }

    protected void error(String message, Object... args)
    {
        String m = String.format(message, args);
        LOG.error(m);
        Utils.logChannel(LogLevel.ERROR, m);
    }

    protected void error(Throwable throwable, String message, Object... args)
    {
        String m = String.format(message, args);
        LOG.error(m, throwable);
        Utils.logChannel(LogLevel.ERROR, m);
    }
}
