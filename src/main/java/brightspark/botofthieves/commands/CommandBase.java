package brightspark.botofthieves.commands;

import brightspark.botofthieves.BotOfThieves;
import brightspark.botofthieves.util.Utils;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;

public abstract class CommandBase extends Command
{
    protected final Logger LOG = LoggerFactory.getLogger(getClass());
    protected boolean removeSentMessage = false;

    public CommandBase(String name, String help, String... aliases)
    {
        this.name = name;
        this.help = help;
        if(aliases != null) this.aliases = aliases;
    }

    @Override
    protected void execute(CommandEvent event)
    {
        debug("Executing command '%s'", event.getMessage().getContentRaw());
        if(removeSentMessage)
            event.getMessage().delete().queue();
        doCommand(event, splitArgs(event.getArgs()));
    }

    protected abstract void doCommand(CommandEvent event, String... args);

    private static String[] splitArgs(String args)
    {
        return args.isEmpty() ? new String[] {} : args.split("\\s+");
    }

    /**
     * Gets the member from the guild if they exist.
     * @param memberString Can be either the member's display name or their @ mention.
     */
    protected Member getMemberFromString(CommandEvent event, String memberString)
    {
        Member member = null;

        //If the user is tagged, extract just the user ID
        if(memberString.startsWith("<@"))
            memberString = memberString.substring(2, memberString.length() - 1);

        try
        {
            //Try parse argument as a user ID
            member = event.getGuild().getMemberById(memberString);
            if(member == null) fail(event, "Couldn't find member '%s'", memberString);
        }
        catch(NumberFormatException e)
        {
            //Try parse argument as a member name
            List<Member> members = event.getGuild().getMembersByEffectiveName(memberString, true);
            if(!members.isEmpty())
            {
                if(members.size() > 1)
                {
                    fail(event, "Found %s members with the name '%s'.\n" +
                            "Please `@` mention the user with this command instead.", members.size(), memberString);
                }
                else
                    member = members.get(0);
            }
        }

        return member;
    }

    protected boolean checkMemberPerms(Member member)
    {
        if(member.isOwner()) return true;
        for(Role role : member.getRoles())
            if(role.equals(BotOfThieves.ADMIN_ROLE))
                return true;
        return false;
    }

    protected void reply(CommandEvent event, String message, boolean bold)
    {
        event.getChannel().sendMessage(Utils.createBotMessage(event.getGuild(), message, bold)).queue();
    }

    protected void reply(CommandEvent event, String title, String description)
    {
        event.getChannel().sendMessage(Utils.createBotMessage(event.getGuild(), title, description)).queue();
    }

    protected void reply(CommandEvent event, MessageEmbed message)
    {
        event.getChannel().sendMessage(message).queue();
    }

    private void fail(CommandEvent event)
    {
        if(event.getClient().getListener() != null)
            event.getClient().getListener().onTerminatedCommand(event, this);
    }

    /**
     * Call this method when a command fails at any point
     */
    protected void fail(CommandEvent event, @Nullable String message, Object... args)
    {
        if(message != null) reply(event, String.format(message, args), true);
        fail(event);
    }

    /**
     * Call this method when a command fails at any point
     */
    protected void fail(CommandEvent event, MessageEmbed message)
    {
        if(message != null) reply(event, message);
        fail(event);
    }

    protected void info(String message, Object... args)
    {
        LOG.info(String.format(message, args));
    }

    protected void debug(String message, Object... args)
    {
        LOG.debug(String.format(message, args));
    }

    protected void warn(String message, Object... args)
    {
        LOG.warn(String.format(message, args));
    }

    protected void error(String message, Object... args)
    {
        LOG.error(String.format(message, args));
    }
}
