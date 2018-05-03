package brightspark.botofthieves.commands;

import brightspark.botofthieves.data.reputation.Reputation;
import brightspark.botofthieves.data.reputation.ReputationChangeResult;
import brightspark.botofthieves.data.reputation.ReputationHandler;
import brightspark.botofthieves.data.reputation.ReputationType;
import brightspark.botofthieves.util.Utils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.User;

import java.util.concurrent.TimeUnit;

public class CommandReputation extends CommandBase
{
    public CommandReputation()
    {
        super("rep", "");
    }

    @Override
    protected void doCommand(CommandEvent event, String... args)
    {
        String arg0 = args[0].toLowerCase();

        if(args.length < 2)
        {
            replyError(event, "Insufficient arguments");
            return;
        }

        if(!checkMemberPerms(event.getMember()))
        {
            replyError(event, "You do not have permission to use this command!");
            return;
        }

        User user = getUserFromString(event.getGuild(), args[1]);
        if(user == null)
        {
            replyError(event, String.format("Couldn't find user '%s'", args[1]));
            return;
        }

        ReputationType repType;
        int amount = 1;
        Reputation reputation;

        switch(arg0)
        {
            case "add": //add [user] [type] [amount]
                if(args.length < 3 || (repType = ReputationType.fromString(args[2])) == null)
                {
                    replyError(event, "Missing whether to change good or bad reputation!");
                    return;
                }
                if(args.length >= 4)
                {
                    try
                    {
                        amount = Integer.parseInt(args[3]);
                    }
                    catch(NumberFormatException e)
                    {
                        replyError(event, String.format("%s is not a number!", args[3]));
                        return;
                    }
                }

                ReputationChangeResult result = ReputationHandler.addRep(user, repType, amount);
                if(result.successful())
                    replySuccess(event, "", String.format("Added %s to %s's %s reputation\nThey now have %s %s reputation",
                            Utils.commaSeparate(amount), user.getName(), repType,
                            Utils.commaSeparate(result.getReputation().getType(repType)), repType));
                else
                {
                    replyWarning(event, String.format("Can't add good reputation to %s - they're banned for %s",
                            user.getName(), Utils.millisTimeToReadable(ReputationHandler.getRep(user).getBan())));
                }
                break;
            case "remove": //remove [user] [type] [amount]
                if(args.length < 3 || (repType = ReputationType.fromString(args[2])) == null)
                {
                    replyError(event, "Missing whether to change good or bad reputation!");
                    return;
                }
                if(args.length >= 4)
                {
                    try
                    {
                        amount = Integer.parseInt(args[3]);
                    }
                    catch(NumberFormatException e)
                    {
                        replyError(event, String.format("%s is not a number!", args[3]));
                        return;
                    }
                }

                reputation = ReputationHandler.subRep(user, repType, amount);
                replySuccess(event, "", String.format("Deducted %s from %s's %s reputation\nThey now have %s %s reputation",
                        Utils.commaSeparate(amount), user.getName(), repType,
                        Utils.commaSeparate(reputation.getType(repType)), repType));
                break;
            case "reset": //reset [user]
                ReputationHandler.setRep(user, ReputationType.GOOD, 0);
                ReputationHandler.setRep(user, ReputationType.BAD, 0);
                replySuccess(event, String.format("Reset %s's reputation", user.getName()));
                break;
            case "ban": //ban [user] [time]
                long millis = TimeUnit.MINUTES.toMillis(30);
                if(args.length > 2)
                    millis = Utils.extractMillisFromTimes(Utils.joinStrings(args, 2));
                if(millis < 0)
                {
                    replyError(event, "Couldn't parse ban time - make sure it's separate values ending in either d, h, m, or s (days, hours, minutes, or seconds respectively)");
                    return;
                }

                if(ReputationHandler.ban(user, millis))
                {
                    replySuccess(event, String.format("Banned %s for %s", user.getName(), Utils.millisTimeToReadable(millis)));
                }
                else
                {
                    long currentBan = ReputationHandler.getRep(user).getBan();
                    String banTime = Utils.millisTimeToReadable(currentBan);
                    replyWarning(event, String.format("Couldn't ban %s as they're already banned for %s", user.getName(), banTime));
                }
                break;
            case "removeban":
                if(ReputationHandler.removeBan(user))
                    replySuccess(event, String.format("Removed ban from %s", user.getName()));
                else
                    replyWarning(event, String.format("Couldn't remove non-existant ban from %s", user.getName()));
                break;
            default:
                replyError(event, String.format("%s is not a valid argument!", args[0]));
        }
    }
}
