package brightspark.botofthieves.data;

import com.google.gson.reflect.TypeToken;
import com.sun.istack.internal.NotNull;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReputationHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(ReputationHandler.class);
    private static final JsonHandler<Reputation> jsonHandler = new JsonHandler<>("reputation", new TypeToken<Reputation>(){});
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private static Map<Long, Reputation> REPUTATION = new HashMap<>();

    static
    {
        Collection<Reputation> set = jsonHandler.read();
        set.forEach(rep -> REPUTATION.put(rep.getUserId(), rep));
        LOG.info("Read " + REPUTATION.size() + " reputations from JSON file");

        //Setup a thread which saves the reputation to file every 5 mins
        executor.scheduleAtFixedRate(() -> {
            Collection<Reputation> values = new HashSet<>(REPUTATION.values());
            LOG.debug("Writing %s reputations to file", values.size());
            jsonHandler.write(values);
            }, 5, 5, TimeUnit.MINUTES);
    }

    private static void putRep(Reputation rep)
    {
        REPUTATION.put(rep.getUserId(), rep);
    }

    public static int forceSave()
    {
        Collection<Reputation> values = new HashSet<>(REPUTATION.values());
        jsonHandler.write(values);
        return values.size();
    }

    public static Reputation getRep(User user)
    {
        return REPUTATION.getOrDefault(user.getIdLong(), new Reputation(user));
    }

    /**
     * Tries to add 1 reputation for the user and returns the result
     */
    public static ReputationChangeResult addRep(User user, @NotNull ReputationType type)
    {
        return addRep(user, type, 1);
    }

    /**
     * Tries to add 1 reputation for the user and returns the result
     */
    public static ReputationChangeResult addRep(User user, @NotNull ReputationType type, long amount)
    {
        Reputation rep = getRep(user);
        boolean success = rep.increase(type, amount);
        if(success) putRep(rep);
        return new ReputationChangeResult(rep, success);
    }

    /**
     * Deducts 1 reputation from the user and returns the reputation
     */
    public static Reputation subRep(User user, @NotNull ReputationType type)
    {
        return subRep(user, type, 1);
    }

    /**
     * Deducts 1 reputation from the user and returns the reputation
     */
    public static Reputation subRep(User user, @NotNull ReputationType type, long amount)
    {
        Reputation rep = getRep(user);
        rep.decrease(type, amount);
        putRep(rep);
        return rep;
    }

    /**
     * Sets the reputation for the user and returns the reputation after the change
     */
    public static Reputation setRep(User user, @NotNull ReputationType type, int amount)
    {
        Reputation rep = getRep(user);
        if(type.isGood())
            rep.setGood(amount);
        else
            rep.setBad(amount);
        putRep(rep);
        return rep;
    }

    /**
     * Bans a user from receiving good reputation for the specified amount of time
     */
    public static boolean ban(User user, long time, TimeUnit timeUnit)
    {
        return ban(user, timeUnit.toMillis(time));
    }

    /**
     * Bans a user from receiving good reputation for the specified amount of time
     */
    public static boolean ban(User user, long timeMillis)
    {
        Reputation rep = getRep(user);
        boolean success = rep.ban(timeMillis);
        putRep(rep);
        return success;
    }

    public static boolean removeBan(User user)
    {
        Reputation rep = getRep(user);
        boolean success = rep.removeBan();
        putRep(rep);
        return success;
    }
}
