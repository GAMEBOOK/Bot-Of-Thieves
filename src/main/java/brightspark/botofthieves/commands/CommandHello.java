package brightspark.botofthieves.commands;

import com.jagrosh.jdautilities.command.CommandEvent;

public class CommandHello extends CommandBase
{
    public CommandHello()
    {
        super("hello", "Replied \"Hello\" back to you");
    }

    @Override
    protected void execute(CommandEvent event)
    {
        event.reply("Arrr Matey!");
    }
}
