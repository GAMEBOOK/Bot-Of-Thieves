package brightspark.botofthieves.commands;

import brightspark.botofthieves.data.Reputation;
import brightspark.botofthieves.data.ReputationChangeResult;
import brightspark.botofthieves.data.ReputationHandler;
import brightspark.botofthieves.data.ReputationType;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;

public class CommandReputation extends CommandBase
{
    public CommandReputation()
    {
        super("rep", "");
    }

    @Override
    protected void doCommand(CommandEvent event)
    {
        String[] args = splitArgs(event.getArgs());
        if(args.length < 2)
        {
            fail(event, "Insufficient arguments");
            return;
        }

        Member member = getMemberFromString(event, args[1]);
        if(member == null) return;
        User user = member.getUser();
        ReputationType repType;
        long amount = 1;

        switch(args[0].toLowerCase())
        {
            case "add":
                if(args.length < 3 || (repType = ReputationType.fromString(args[2])) == null)
                {
                    fail(event, "Missing whether to change good or bad reputation!");
                    return;
                }
                if(args.length >= 4)
                {
                    try
                    {
                        amount = Long.parseLong(args[3]);
                    }
                    catch(NumberFormatException e)
                    {
                        fail(event, "%s is not a number!", args[3]);
                        return;
                    }
                }
                ReputationChangeResult result = ReputationHandler.addRep(user, repType);
                if(result.successful())
                    reply(event, "Added %s to %s's %s reputation.\nThey now have %s %s reputation",
                            amount, user.getName(), repType, result.getReputation().getType(repType), repType);
                break;
            case "remove":
                if(args.length < 3 || (repType = ReputationType.fromString(args[2])) == null)
                {
                    fail(event, "Missing whether to change good or bad reputation!");
                    return;
                }
                if(args.length >= 4)
                {
                    try
                    {
                        amount = Long.parseLong(args[3]);
                    }
                    catch(NumberFormatException e)
                    {
                        fail(event, "%s is not a number!", args[3]);
                        return;
                    }
                }
                Reputation reputation = ReputationHandler.subRep(user, repType, amount);
                reply(event, "Deducted %s from %s's %s reputation.\nThey now have %s %s reputation",
                        amount, user.getName(), repType, reputation.getType(repType));
                break;
            case "reset":

                break;
            case "stats":

                break;
            case "ban":

                break;
            default:
                fail(event, "%s is not a valid argument!", args[0]);
        }
    }
}
