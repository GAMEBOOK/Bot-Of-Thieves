package brightspark.botofthieves.data.reputation;

import brightspark.botofthieves.util.EmojiUtil;
import brightspark.botofthieves.util.Utils;
import net.dv8tion.jda.core.entities.User;

import javax.annotation.Nonnull;

public class Reputation
{
    private long userId;
    private int good, bad;
    private Long banEnd;

    public Reputation(@Nonnull User user)
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
    public int getGood()
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
    public int getBad()
    {
        return bad;
    }

    /**
     * Gets the amount of reputation of the type specified
     */
    public int getType(@Nonnull ReputationType type)
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
    public int getTotal()
    {
        return good - bad;
    }

    /**
     * Increase the given type of reputation by 1 if not banned
     * Returns false if the user is banned and therefore no change is made
     */
    public boolean increase(@Nonnull ReputationType type)
    {
        return increase(type, 1);
    }

    /**
     * Increase the given type of reputation if not banned
     * Returns false if the user is banned and therefore no change is made
     */
    public boolean increase(@Nonnull ReputationType type, int amount)
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
    public void decrease(@Nonnull ReputationType type)
    {
        decrease(type, 1);
    }

    /**
     * Decreases the given type of reputation
     */
    public void decrease(@Nonnull ReputationType type, int amount)
    {
        if(type.isGood())
            good = Math.max(0, good - amount);
        else
            bad = Math.max(0, bad - amount);
    }

    /**
     * Gets a number between 0 and 1 representing the ratio of good to bad reputation earned
     */
    public float getRatio()
    {
        return (float) good / (float) (good + bad);
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
        long curTime = System.currentTimeMillis();
        if(banEnd != null && curTime > banEnd)
            banEnd = null;
        return banEnd == null ? null : banEnd - curTime;
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
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s %s %s %s %s %s",
                EmojiUtil.GREEN_HEART, good, EmojiUtil.NAME_BADGE, bad, EmojiUtil.ANCHOR, Math.round(getRatio() * 100)));
        Long ban = getBan();
        if(ban != null)
            sb.append("\n").append(String.format("Banned for %s", Utils.millisTimeToReadable(ban)));
        return sb.toString();
    }
}
