package brightspark.botofthieves.commands;

import brightspark.botofthieves.data.ReputationHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public class CommandStats extends CommandBase
{
    public CommandStats()
    {
        super("stats", "Shows the reputation stats for a user");
    }

    @Override
    protected void doCommand(CommandEvent event, String... args)
    {
        User user = event.getAuthor();
        if(args.length > 0)
        {
            Member member = getMemberFromString(event, args[0]);
            if(member == null) return;
            user = member.getUser();
        }
        reply(event, String.format("%s's reputation stats:", user.getName()), ReputationHandler.getRep(user).getText());
    }
}
