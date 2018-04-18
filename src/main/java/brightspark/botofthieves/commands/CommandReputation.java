package brightspark.botofthieves.commands;

import com.jagrosh.jdautilities.command.CommandEvent;

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
        //TODO: Reputation command
    }
}
