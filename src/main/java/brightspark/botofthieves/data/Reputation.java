package brightspark.botofthieves.data;

import net.dv8tion.jda.core.entities.User;

public class Reputation
{
    private long userId;
    private int good, bad;
    private Long banEnd;

    public Reputation(User user)
    {
        userId = user.getIdLong();
    }

    public void setGood(int num)
    {
        good = num;
    }

    public int getGood()
    {
        return good;
    }

    public void setBad(int num)
    {
        bad = num;
    }

    public int getBad()
    {
        return bad;
    }

    public void increase(boolean isGood)
    {
        increase(isGood, 1);
    }

    public void increase(boolean isGood, int amount)
    {
        if(isGood)
            good += amount;
        else
            bad += amount;
    }

    public void decrease(boolean isGood)
    {
        decrease(isGood, 1);
    }

    public void decrease(boolean isGood, int amount)
    {
        increase(isGood, -amount);
    }

    /**
     * Gets a number between 0 and 1 representing the ratio of good to bad reputation earned
     */
    public float getRatio()
    {
        return (float) (good + bad) / (float) good;
    }

    /**
     * Sets the time to ban the user from positive reputation till
     */
    public boolean ban(long timeMillis)
    {
        if(banEnd != null) return false;
        banEnd = timeMillis;
        return true;
    }

    public Long getBan()
    {
        return banEnd;
    }

    public boolean removeBan()
    {
        boolean hasBan = banEnd != null;
        banEnd = null;
        return hasBan;
    }

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
        return String.format("\uD83D\uDC9A %s \uD83D\uDC80 %s \u2796 %s", good, bad, getRatio());
    }
}
