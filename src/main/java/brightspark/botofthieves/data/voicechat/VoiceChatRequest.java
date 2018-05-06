package brightspark.botofthieves.data.voicechat;

public class VoiceChatRequest
{
    private final long messageId, userId, channelId, guildId;
    private long startTime;
    private final boolean favouritesOnly;
    private final String description;

    public VoiceChatRequest(long messageId, long userId, long channelId, long guildId, boolean favouritesOnly, String description)
    {
        this.messageId = messageId;
        this.userId = userId;
        this.channelId = channelId;
        this.guildId = guildId;
        setStartTime();
        this.favouritesOnly = favouritesOnly;
        this.description = description;
    }

    public long getMessageId()
    {
        return messageId;
    }

    public long getUserId()
    {
        return userId;
    }

    public long getChannelId()
    {
        return channelId;
    }

    public long getGuildId()
    {
        return guildId;
    }

    public long getStartTime()
    {
        return startTime;
    }

    public void setStartTime()
    {
        startTime = System.currentTimeMillis();
    }

    public boolean isFavouritesOnly()
    {
        return favouritesOnly;
    }

    public String getDescription()
    {
        return description;
    }
}
