package brightspark.botofthieves.commands;

import brightspark.botofthieves.BotOfThieves;
import brightspark.botofthieves.data.reputation.ReputationHandler;
import brightspark.botofthieves.data.userdata.UserDataHandler;
import brightspark.botofthieves.util.Utils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.MessageEmbed;

public class CommandSave extends CommandBase
{
    public CommandSave()
    {
        super("forcesave", "Force saves all data to the JSON files");
        if(BotOfThieves.ADMIN_ROLE != null)
            requiredRole = BotOfThieves.ADMIN_ROLE.getName();
    }

    @Override
    protected void doCommand(CommandEvent event, String... args)
    {
        int repSaved = ReputationHandler.forceSave();
        int favSaved = UserDataHandler.forceSaveFavourites();
        int blSaved = UserDataHandler.forceSaveBlacklists();
        MessageEmbed message = Utils.createBotMessageTemplate(event.getGuild())
                .setTitle("Force saved data:")
                .addField("Reputation", String.valueOf(repSaved), true)
                .addField("Favourites", String.valueOf(favSaved), true)
                .addField("Blacklists", String.valueOf(blSaved), true)
                .build();
        reply(event, message);
    }
}
