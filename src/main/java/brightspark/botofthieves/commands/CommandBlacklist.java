package brightspark.botofthieves.commands;

import com.jagrosh.jdautilities.command.CommandEvent;

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

    }
}
