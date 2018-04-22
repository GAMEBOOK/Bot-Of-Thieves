package brightspark.botofthieves;

import brightspark.botofthieves.commands.CommandHello;
import brightspark.botofthieves.commands.CommandReputation;
import brightspark.botofthieves.commands.CommandStats;
import brightspark.botofthieves.util.Utils;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

public class BotOfThieves
{
    public static File RESOURCES_DIR = new File("src/main/resources");
    public static File CONFIG_FILE = new File("config.properties");
    public static File LOG4J_PROP_FILE = new File(RESOURCES_DIR, "log4j.properties");
    public static File DATA_DIR = new File("data");

    public static Logger LOG = LoggerFactory.getLogger(BotOfThieves.class);
    public static JDA JDA;
    public static EventWaiter WAITER = new EventWaiter();
    public static String PREFIX;
    public static Role ADMIN_ROLE;
    public static TextChannel LOG_CHANNEL;

    static
    {
        //Set the Log4J properties file
        try
        {
            System.setProperty("log4j.configuration", LOG4J_PROP_FILE.toURI().toURL().toString());
        }
        catch(MalformedURLException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String... args) throws LoginException
    {
        if(!DATA_DIR.exists() && !DATA_DIR.mkdir())
        {
            LOG.error("Error creating data directory! Exiting...");
            System.exit(0);
        }

        Config.read();

        PREFIX = Config.get("command_prefix", "!");

        try
        {
            JDA = new JDABuilder(AccountType.BOT)
                    .setToken(Config.get("token"))
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setGame(Game.playing("Loading..."))
                    .addEventListener(new CommandClientBuilder()
                            .setGame(Game.playing("Testing!"))
                            .setOwnerId(Config.get("owner_id"))
                            .setEmojis(Utils.EMOJI_SAILBOAT, Utils.EMOJI_ANCHOR, Utils.EMOJI_SKULL_CROSSBONES)
                            .setPrefix(PREFIX)
                            .addCommands(
                                    new CommandHello(),
                                    new CommandReputation(),
                                    new CommandStats()
                            ).build()
                    ).buildBlocking();
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }

        //Save incase any defaults were set
        Config.save();

        String logChannel = Config.get("log_channel_name");
        if(!logChannel.isEmpty())
            LOG_CHANNEL = Utils.findTextChannel(logChannel);

        String adminRole = Config.get("bot_admin_role");
        if(!adminRole.isEmpty())
        {
            List<Role> roles = JDA.getRolesByName(adminRole, false);
            if(roles.size() > 0)
                ADMIN_ROLE = roles.get(0);
        }

        LOG.info("Bot initialisation finished");
    }
}
