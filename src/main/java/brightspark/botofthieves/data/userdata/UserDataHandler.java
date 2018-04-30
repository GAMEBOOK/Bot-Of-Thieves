package brightspark.botofthieves.data.userdata;

import brightspark.botofthieves.BotOfThieves;
import brightspark.botofthieves.data.JsonHandler;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class UserDataHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(UserDataHandler.class);
    private static final JsonHandler<UserList> jsonHandlerFavourites = new JsonHandler<>("favourites", new TypeToken<Collection<UserList>>(){});
    private static final JsonHandler<UserList> jsonHandlerBlacklist = new JsonHandler<>("blacklist", new TypeToken<Collection<UserList>>(){});

    private static final Map<Long, UserList> FAVOURITES = new HashMap<>();
    private static final Map<Long, UserList> BLACKLISTS = new HashMap<>();

    static
    {
        Collection<UserList> set = jsonHandlerFavourites.read();
        set.forEach(rep -> FAVOURITES.put(rep.getUserId(), rep));
        LOG.info("Read " + FAVOURITES.size() + " favourites from JSON file");

        set = jsonHandlerBlacklist.read();
        set.forEach(rep -> BLACKLISTS.put(rep.getUserId(), rep));
        LOG.info("Read " + BLACKLISTS.size() + " blacklists from JSON file");

        //Setup a thread which saves to files every 5 mins
        BotOfThieves.EXECUTOR.scheduleAtFixedRate(() -> {
            Collection<UserList> values = new HashSet<>(FAVOURITES.values());
            LOG.debug("Writing %s favourites to file", values.size());
            jsonHandlerFavourites.write(values);

            values = new HashSet<>(BLACKLISTS.values());
            LOG.debug("Writing %s blacklists to file", values.size());
            jsonHandlerBlacklist.write(values);
        }, 5, 5, TimeUnit.MINUTES);
    }

    // <<<< FAVOURITES >>>>

    public static int forceSaveFavourites()
    {
        Collection<UserList> values = new HashSet<>(FAVOURITES.values());
        if(values.size() > 0) jsonHandlerFavourites.write(values);
        return values.size();
    }

    private static void putFavourites(UserList list)
    {
        FAVOURITES.put(list.getUserId(), list);
    }

    private static UserList getFavouritesInternal(long userId)
    {
        return FAVOURITES.getOrDefault(userId, new UserList(userId));
    }

    public static Set<Long> getFavourites(long userId)
    {
        return getFavouritesInternal(userId).getList();
    }

    public static boolean isFavourite(long userId, long otherUser)
    {
        UserList list = getFavouritesInternal(userId);
        return list.hasUser(otherUser);
    }

    public static boolean addToFavourites(long userId, long otherUser)
    {
        UserList list = getFavouritesInternal(userId);
        boolean success = list.addUser(otherUser);
        if(success) putFavourites(list);
        return success;
    }

    public static boolean removeFromFavourites(long userId, long otherUser)
    {
        UserList list = getFavouritesInternal(userId);
        boolean success = list.removeUser(otherUser);
        if(success) putFavourites(list);
        return success;
    }

    // <<<< BLACKLISTS >>>>

    public static int forceSaveBlacklists()
    {
        Collection<UserList> values = new HashSet<>(BLACKLISTS.values());
        if(values.size() > 0) jsonHandlerBlacklist.write(values);
        return values.size();
    }

    private static void putBlacklist(UserList list)
    {
        BLACKLISTS.put(list.getUserId(), list);
    }

    private static UserList getBlacklistInternal(long userId)
    {
        return BLACKLISTS.getOrDefault(userId, new UserList(userId));
    }

    public static Set<Long> getBlacklist(long userId)
    {
        return getBlacklistInternal(userId).getList();
    }

    public static boolean isBlacklisted(long userId, long otherUser)
    {
        UserList list = getBlacklistInternal(userId);
        return list.hasUser(otherUser);
    }

    public static boolean addToBlacklist(long userId, long otherUser)
    {
        UserList list = getBlacklistInternal(userId);
        boolean success = list.addUser(otherUser);
        if(success) putBlacklist(list);
        return success;
    }

    public static boolean removeFromBlacklist(long userId, long otherUser)
    {
        UserList list = getBlacklistInternal(userId);
        boolean success = list.removeUser(otherUser);
        if(success) putBlacklist(list);
        return success;
    }
}
