package brightspark.botofthieves.commands;

import brightspark.botofthieves.data.reputation.ReputationHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.User;

public class CommandStats extends CommandBase
{
    public CommandStats()
    {
        super("stats", "Shows the reputation stats for a user");
        guildOnly = false;
    }

    @Override
    protected void doCommand(CommandEvent event, String... args)
    {
        User user = event.getAuthor();
        if(args.length > 0)
        {
            user = getUserFromString(event.getGuild(), args[0]);
            if(user == null)
            {
                fail(event, "Couldn't find user '%s'", args[0]);
                return;
            }
        }
        reply(event, String.format("%s's reputation stats:", user.getName()), ReputationHandler.getRep(user).getText());
    }
}
