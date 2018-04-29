package brightspark.botofthieves.data.voicechat;

public class VoiceChatRequest
{
    private final long messageId, userId, channelId, guildId, startTime;

    public VoiceChatRequest(long messageId, long userId, long channelId, long guildId)
    {
        this.messageId = messageId;
        this.userId = userId;
        this.channelId = channelId;
        this.guildId = guildId;
        startTime = System.currentTimeMillis();
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
}
