package brightspark.botofthieves.data.voicechat;

public class VoiceChatRequest
{
    private final long userId, channelId, guildId, startTime;

    public VoiceChatRequest(long userId, long channelId, long guildId)
    {
        this.userId = userId;
        this.channelId = channelId;
        this.guildId = guildId;
        startTime = System.currentTimeMillis();
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
}
