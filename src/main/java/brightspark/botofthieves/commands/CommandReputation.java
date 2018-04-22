package brightspark.botofthieves.commands;

import brightspark.botofthieves.data.Reputation;
import brightspark.botofthieves.data.ReputationChangeResult;
import brightspark.botofthieves.data.ReputationHandler;
import brightspark.botofthieves.data.ReputationType;
import brightspark.botofthieves.util.Utils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.Member;
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

        if(arg0.equals("forcesave"))
        {
            int numSaved = ReputationHandler.forceSave();
            reply(event, String.format("Saved %s reputations to file", numSaved), true);
            return;
        }
        else if(args.length < 2)
        {
            fail(event, "Insufficient arguments");
            return;
        }

        Member member = getMemberFromString(event, args[1]);
        if(member == null) return;

        if(!checkMemberPerms(member))
        {
            fail(event, "You do not have permission to use this command!");
            return;
        }

        User user = member.getUser();
        ReputationType repType;
        long amount = 1;
        Reputation reputation;

        switch(arg0)
        {
            case "add": //add [user] [type] [amount]
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

                ReputationChangeResult result = ReputationHandler.addRep(user, repType, amount);
                if(result.successful())
                    reply(event, String.format("Added %s to %s's %s reputation\nThey now have %s %s reputation",
                            Utils.commaSeparate(amount), user.getName(), repType,
                            Utils.commaSeparate(result.getReputation().getType(repType)), repType), false);
                else
                {
                    reply(event, String.format("Can't add good reputation to %s - they're banned for %s",
                            user.getName(), Utils.millisTimeToReadable(ReputationHandler.getRep(user).getBan())), true);
                }
                break;
            case "remove": //remove [user] [type] [amount]
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

                reputation = ReputationHandler.subRep(user, repType, amount);
                reply(event, String.format("Deducted %s from %s's %s reputation\nThey now have %s %s reputation",
                        Utils.commaSeparate(amount), user.getName(), repType,
                        Utils.commaSeparate(reputation.getType(repType)), repType), false);
                break;
            case "reset": //reset [user]
                ReputationHandler.setRep(user, ReputationType.GOOD, 0);
                ReputationHandler.setRep(user, ReputationType.BAD, 0);
                reply(event, String.format("Reset %s's reputation", user.getName()), false);
                break;
            case "ban": //ban [user] [time]
                long millis = TimeUnit.MINUTES.toMillis(30);
                if(args.length > 2)
                    millis = Utils.extractMillisFromTimes(Utils.joinStrings(args, 2));
                if(millis < 0)
                {
                    fail(event, "Couldn't parse ban time - make sure it's separate values ending in either d, h, m, or s (days, hours, minutes, or seconds respectively)");
                    return;
                }

                if(ReputationHandler.ban(user, millis))
                {
                    reply(event, String.format("Banned %s for %s", user.getName(), Utils.millisTimeToReadable(millis)), true);
                }
                else
                {
                    long currentBan = ReputationHandler.getRep(user).getBan();
                    String banTime = Utils.millisTimeToReadable(currentBan);
                    reply(event, String.format("Couldn't ban %s as they're already banned for %s", user.getName(), banTime), true);
                }
                break;
            case "removeban":
                if(ReputationHandler.removeBan(user))
                    reply(event, String.format("Removed ban from %s", user.getName()), true);
                else
                    reply(event, String.format("Couldn't remove non-existant ban from %s", user.getName()), true);
                break;
            default:
                fail(event, "%s is not a valid argument!", args[0]);
        }
    }
}
