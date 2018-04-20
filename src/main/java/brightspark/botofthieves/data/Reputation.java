package brightspark.botofthieves.data;

import brightspark.botofthieves.util.Utils;
import com.sun.istack.internal.NotNull;
import net.dv8tion.jda.core.entities.User;

public class Reputation
{
    private long userId;
    private long good, bad;
    private Long banEnd;

    public Reputation(@NotNull User user)
    {
        userId = user.getIdLong();
    }

    /**
     * Sets the amount of good reputation
     */
    public void setGood(int num)
    {
        good = num;
    }

    /**
     * Gets the amount of good reputation
     */
    public long getGood()
    {
        return good;
    }

    /**
     * Sets the amount of bad reputation
     */
    public void setBad(int num)
    {
        bad = num;
    }

    /**
     * Gets the amount of bad reputation
     */
    public long getBad()
    {
        return bad;
    }

    /**
     * Gets the amount of reputation of the type specified
     */
    public long getType(@NotNull ReputationType type)
    {
        switch(type)
        {
            case GOOD:  return getGood();
            case BAD:   return getBad();
            default:    throw new IllegalArgumentException("Unhandled ReputationType '" + type + "'");
        }
    }

    /**
     * Gets the total reputation (good - bad)
     */
    public long getTotal()
    {
        return good - bad;
    }

    /**
     * Increase the given type of reputation by 1 if not banned
     * Returns false if the user is banned and therefore no change is made
     */
    public boolean increase(@NotNull ReputationType type)
    {
        return increase(type, 1);
    }

    /**
     * Increase the given type of reputation if not banned
     * Returns false if the user is banned and therefore no change is made
     */
    public boolean increase(@NotNull ReputationType type, long amount)
    {
        if(type.isGood())
        {
            if(getBan() != null) return false;
            good += amount;
            return true;
        }
        bad += amount;
        return true;
    }

    /**
     * Decreases the given type of reputation by 1
     */
    public void decrease(@NotNull ReputationType type)
    {
        decrease(type, 1);
    }

    /**
     * Decreases the given type of reputation
     */
    public void decrease(@NotNull ReputationType type, long amount)
    {
        if(type.isGood())
            good = Math.max(0, good - amount);
        else
            bad = Math.max(0, bad - amount);
    }

    /**
     * Gets a number between 0 and 1 representing the ratio of good to bad reputation earned
     */
    public double getRatio()
    {
        return (double) (good + bad) / (double) good;
    }

    /**
     * Sets the amount of time to ban the user from positive reputation for
     * Returns false if a ban was already active
     */
    public boolean ban(long timeMillis)
    {
        if(getBan() != null) return false;
        banEnd = System.currentTimeMillis() + timeMillis;
        return true;
    }

    /**
     * Validates the current ban and then returns it
     */
    public Long getBan()
    {
        if(banEnd != null && System.currentTimeMillis() > banEnd)
            banEnd = null;
        return banEnd;
    }

    /**
     * Removes the ban if one exists and returns if there was a ban
     */
    public boolean removeBan()
    {
        boolean hasBan = getBan() != null;
        banEnd = null;
        return hasBan;
    }

    /**
     * Gets the user ID this reputation is for
     */
    public long getUserId()
    {
        return userId;
    }

    /**
     * Returns a String representing this reputation with emojis
     */
    public String getText()
    {
        //Green Heart, Skull and Crossbones, Minus Sign
        return String.format("%s %s %s %s %s %s",
                Utils.EMOJI_GREEN_HEART, good, Utils.EMOJI_NAME_BADGE, bad, Utils.EMOJI_ANCHOR, Math.round(getRatio() * 100));
    }
}
