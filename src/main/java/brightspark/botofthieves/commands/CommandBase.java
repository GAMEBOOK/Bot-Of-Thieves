package brightspark.botofthieves.commands;

import com.jagrosh.jdautilities.command.Command;

public abstract class CommandBase extends Command
{
    public CommandBase(String name, String help, String... aliases)
    {
        this.name = name;
        this.help = help;
        if(aliases != null) this.aliases = aliases;
    }
}
