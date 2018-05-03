package brightspark.botofthieves.commands;

import brightspark.botofthieves.data.userdata.UserDataHandler;
import brightspark.botofthieves.util.Utils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.User;

import java.util.Set;

public class CommandFavourite extends CommandBase
{
    public CommandFavourite()
    {
        super("favs", "", "favourites");
        setDmOnly();
    }

    @Override
    protected void doCommand(CommandEvent event, String... args)
    {
        long userId = event.getAuthor().getIdLong();
        if(args.length == 0)
        {
            Set<Long> list = UserDataHandler.getFavourites(userId);
            if(list.isEmpty())
                replyWarning(event, "You have no favourite users!");
            else
            {
                StringBuilder sb = new StringBuilder();
                list.forEach(id -> {
                    User u = event.getJDA().getUserById(id);
                    if(u != null) sb.append(Utils.getFullUser(u)).append("\n");
                });
                reply(event, "Favourite Users:", sb.toString());
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

        if(UserDataHandler.isBlacklisted(userId, otherUserId))
        {
            replyWarning(event, "User " + otherUser.getName() + " is already blacklisted!" +
                    "\nRemove them from your blacklist first to favourite them.");
        }
        else if(UserDataHandler.isFavourite(userId, otherUserId))
        {
            UserDataHandler.removeFromFavourites(userId, otherUserId);
            replySuccess(event, "User " + otherUser.getName() + " removed from favourites");
        }
        else
        {
            UserDataHandler.addToFavourites(userId, otherUserId);
            replySuccess(event, "User " + otherUser.getName() + " added to favourites");
        }
    }
}
