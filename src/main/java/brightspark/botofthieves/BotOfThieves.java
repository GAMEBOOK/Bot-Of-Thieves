package brightspark.botofthieves;

import brightspark.botofthieves.commands.CommandCrew;
import brightspark.botofthieves.commands.CommandHello;
import brightspark.botofthieves.commands.CommandReputation;
import brightspark.botofthieves.commands.CommandStats;
import brightspark.botofthieves.data.reputation.ReputationListener;
import brightspark.botofthieves.util.Utils;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class BotOfThieves
{
    public static File RESOURCES_DIR = new File("src/main/resources");
    public static File CONFIG_FILE = new File("config.properties");
    public static File LOG4J_PROP_FILE = new File(RESOURCES_DIR, "log4j.properties");
    public static File DATA_DIR = new File("data");

    public static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
    public static final Logger LOG = LoggerFactory.getLogger(BotOfThieves.class);
    public static JDA JDA;
    public static EventWaiter WAITER = new EventWaiter();
    public static String PREFIX;
    public static TextChannel LOG_CHANNEL;
    public static Role ADMIN_ROLE;
    public static Category VOICE_CHANNEL_CATEGORY;
    public static VoiceChannel VOICE_CHANNEL_MAIN;

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
                    .addEventListener(
                            new CommandClientBuilder()
                            .setGame(Game.playing("Testing!"))
                            .setOwnerId(Config.get("owner_id"))
                            .setEmojis(Utils.EMOJI_SAILBOAT, Utils.EMOJI_ANCHOR, Utils.EMOJI_SKULL_CROSSBONES)
                            .setPrefix(PREFIX)
                            .addCommands(
                                    new CommandHello(),
                                    new CommandReputation(),
                                    new CommandStats(),
                                    new CommandCrew()
                            ).build(),
                            new ReputationListener()
                    ).buildBlocking();
        }
        catch(InterruptedException e)
        {
            e.printStackTrace();
        }

        //Save incase any defaults were set
        Config.save();

        String logChannel = Config.get("log_channel");
        if(!logChannel.isEmpty())
        {
            LOG_CHANNEL = Utils.findTextChannel(logChannel);
            if(LOG_CHANNEL != null)
                LOG.info(String.format("Set %s (%s) as the bot logging channel", LOG_CHANNEL.getName(), LOG_CHANNEL.getIdLong()));
        }
        else
            LOG.warn("Bot log channel not set");

        String adminRole = Config.get("bot_admin_role");
        if(!adminRole.isEmpty())
        {
            ADMIN_ROLE = Utils.findRole(adminRole);
            if(ADMIN_ROLE != null)
                LOG.info(String.format("Set %s (%s) as the bot admin role", ADMIN_ROLE.getName(), ADMIN_ROLE.getIdLong()));
        }
        else
            LOG.warn("Bot admin role not set");

        String vcCategory = Config.get("voice_channel_category");
        if(!vcCategory.isEmpty())
        {
            VOICE_CHANNEL_CATEGORY = Utils.findCategory(vcCategory);
            if(VOICE_CHANNEL_CATEGORY != null)
                LOG.info(String.format("Set %s (%s) as the voice chat category", VOICE_CHANNEL_CATEGORY.getName(), VOICE_CHANNEL_CATEGORY.getIdLong()));
        }
        else
            LOG.warn("Voice chat category not set");

        String mainVcChannel = Config.get("voice_channel_main");
        if(!mainVcChannel.isEmpty())
        {
            VOICE_CHANNEL_MAIN = Utils.findVoiceChannel(mainVcChannel);
            if(VOICE_CHANNEL_MAIN != null)
                LOG.info(String.format("Set %s (%s) as the main voice chat channel", VOICE_CHANNEL_MAIN.getName(), VOICE_CHANNEL_MAIN.getIdLong()));
        }
        else
            LOG.warn("Main voice channel not set");

        LOG.info("Bot initialisation finished");
    }
}
