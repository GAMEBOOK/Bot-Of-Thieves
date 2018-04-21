package brightspark.botofthieves.commands;

import com.jagrosh.jdautilities.command.CommandEvent;

public class CommandHello extends CommandBase
{
    public CommandHello()
    {
        super("hello", "Replied \"Hello\" back to you");
    }

    @Override
    protected void doCommand(CommandEvent event, String... args)
    {
        event.reply("Arrr Matey!");
    }
}
