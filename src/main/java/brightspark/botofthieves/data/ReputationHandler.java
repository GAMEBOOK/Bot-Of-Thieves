package brightspark.botofthieves.data;

import net.dv8tion.jda.core.entities.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ReputationHandler
{
    private static final Logger LOG = LogManager.getLogger();
    private static final JsonHandler<Reputation> jsonHandler = new JsonHandler<>("reputation");

    private static Map<Long, Reputation> REPUTATION = new HashMap<>();

    static
    {
        Set<Reputation> set = jsonHandler.read();
        set.forEach(rep -> REPUTATION.put(rep.getUserId(), rep));
        LOG.info("Read " + REPUTATION.size() + " reputations from JSON file");
    }

    private static void putRepInternal(Reputation rep)
    {
        REPUTATION.put(rep.getUserId(), rep);
    }

    private static Reputation getRep(User user)
    {
        return REPUTATION.getOrDefault(user.getIdLong(), new Reputation(user));
    }

    public static Reputation addRep(User user, boolean isGood)
    {
        Reputation rep = getRep(user);
        rep.increase(isGood);
        putRepInternal(rep);
        return rep;
    }

    public static Reputation setRep(User user, boolean isGood, int amount)
    {
        Reputation rep = getRep(user);
        if(isGood)
            rep.setGood(amount);
        else
            rep.setBad(amount);
        putRepInternal(rep);
        return rep;
    }

    public static boolean ban(User user, long timeMillis)
    {
        Reputation rep = getRep(user);
        return rep.ban(timeMillis);
    }
}
