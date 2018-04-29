package brightspark.botofthieves.data.voicechat;

public class VoiceChatRequest
{
    private final long messageId, userId, channelId, guildId, startTime;
    private final boolean favouritesOnly;

    public VoiceChatRequest(long messageId, long userId, long channelId, long guildId, boolean favouritesOnly)
    {
        this.messageId = messageId;
        this.userId = userId;
        this.channelId = channelId;
        this.guildId = guildId;
        startTime = System.currentTimeMillis();
        this.favouritesOnly = favouritesOnly;
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

    public boolean isFavouritesOnly()
    {
        return favouritesOnly;
    }
}
