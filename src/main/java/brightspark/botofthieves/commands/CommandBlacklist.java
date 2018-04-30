package brightspark.botofthieves.commands;

import brightspark.botofthieves.data.userdata.UserDataHandler;
import brightspark.botofthieves.util.Utils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.Set;

public class CommandBlacklist extends CommandBase
{
    public CommandBlacklist()
    {
        super("bl", "", "blacklist");
        setDmOnly();
    }

    @Override
    protected void doCommand(CommandEvent event, String... args)
    {
        long userId = event.getAuthor().getIdLong();
        MessageChannel channel = event.getChannel();
        if(args.length == 0)
        {
            Set<Long> list = UserDataHandler.getBlacklist(userId);
            if(list.isEmpty())
                channel.sendMessage("You have no blacklisted users!").queue();
            else
            {
                StringBuilder sb = new StringBuilder();
                list.forEach(id -> {
                    User u = event.getJDA().getUserById(id);
                    if(u != null) sb.append(Utils.getFullUser(u)).append("\n");
                });
                channel.sendMessage("*Blacklisted Users:*\n" + sb.toString()).queue();
            }
            return;
        }

        String otherName = Utils.joinStrings(args);
        User otherUser = Utils.findUser(otherName);
        if(otherUser == null)
        {
            channel.sendMessage("Couldn't find user '" + otherName + "'").queue();
            return;
        }
        long otherUserId = otherUser.getIdLong();

        if(UserDataHandler.isFavourite(userId, otherUserId))
        {
            channel.sendMessage("User " + otherUser.getName() + " is already favourited!" +
                    "\nRemove them from your favourites first to blacklist them.").queue();
        }
        else if(UserDataHandler.isBlacklisted(userId, otherUserId))
        {
            UserDataHandler.removeFromBlacklist(userId, otherUserId);
            channel.sendMessage("User " + otherUser.getName() + " removed from blacklist").queue();
        }
        else
        {
            UserDataHandler.addToBlacklist(userId, otherUserId);
            channel.sendMessage("User " + otherUser.getName() + " added to blacklist").queue();
        }
    }
}
