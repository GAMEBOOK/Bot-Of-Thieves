package brightspark.botofthieves.commands;

import brightspark.botofthieves.data.userdata.UserDataHandler;
import brightspark.botofthieves.util.Utils;
import com.jagrosh.jdautilities.command.CommandEvent;
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
        if(args.length == 0)
        {
            Set<Long> list = UserDataHandler.getBlacklist(userId);
            if(list.isEmpty())
                replyWarning(event, "You have no blacklisted users!");
            else
            {
                StringBuilder sb = new StringBuilder();
                list.forEach(id -> {
                    User u = event.getJDA().getUserById(id);
                    if(u != null) sb.append(Utils.getFullUser(u)).append("\n");
                });
                reply(event, "Blacklisted Users:", sb.toString(), false);
            }
            return;
        }

        String otherName = Utils.joinStrings(args);
        User otherUser = Utils.findUser(otherName);
        if(otherUser == null)
        {
            replyError(event, "Couldn't find user '" + otherName + "'");
            return;
        }
        long otherUserId = otherUser.getIdLong();

        if(UserDataHandler.isFavourite(userId, otherUserId))
        {
            replyWarning(event, "User " + otherUser.getName() + " is already favourited!" +
                    "\nRemove them from your favourites first to blacklist them.");
        }
        else if(UserDataHandler.isBlacklisted(userId, otherUserId))
        {
            UserDataHandler.removeFromBlacklist(userId, otherUserId);
            replySuccess(event, "User " + otherUser.getName() + " removed from blacklist");
        }
        else
        {
            UserDataHandler.addToBlacklist(userId, otherUserId);
            replySuccess(event, "User " + otherUser.getName() + " added to blacklist");
        }
    }
}
