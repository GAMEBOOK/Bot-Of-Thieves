package brightspark.botofthieves.data.voicechat;

import brightspark.botofthieves.BotOfThieves;
import brightspark.botofthieves.Config;
import brightspark.botofthieves.util.Utils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VoiceChatHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(VoiceChatHandler.class);

    private static final int REQUEST_TIMEOUT;

    //Long is the userId
    private static final Map<Long, VoiceChatRoom> ROOMS = new HashMap<>();
    //Long is the messageId
    private static final Map<Long, VoiceChatRequest> REQUESTS = new HashMap<>();

    static
    {
        //Get request timeout from config
        String timeout = Config.get("vc_request_timeout_mins", "10");
        int timeoutNum;
        try
        {
            timeoutNum = Integer.parseInt(timeout);
        }
        catch(NumberFormatException e)
        {
            timeoutNum = 10;
        }
        REQUEST_TIMEOUT = timeoutNum;

        //Setup a thread to check if requests have timed out
        BotOfThieves.EXECUTOR.scheduleAtFixedRate(() -> {
            synchronized(REQUESTS)
            {
                long curTime = System.currentTimeMillis();
                Iterator<Map.Entry<Long, VoiceChatRequest>> iter = REQUESTS.entrySet().iterator();
                while(iter.hasNext())
                {
                    Map.Entry<Long, VoiceChatRequest> entry = iter.next();
                    VoiceChatRequest request = entry.getValue();
                    if(request.getStartTime() + REQUEST_TIMEOUT > curTime)
                    {
                        Guild guild = BotOfThieves.JDA.getGuildById(request.getGuildId());
                        TextChannel channel = guild.getTextChannelById(request.getChannelId());
                        if(channel != null)
                        {
                            User user = BotOfThieves.JDA.getUserById(request.getUserId());
                            LOG.info(String.format("Timed out crew request from user %s (%s)", user.getName(), user.getIdLong()));
                            channel.sendMessage(Utils.createBotMessage(guild, user.getAsMention() + " your crew request has been too long without a response. You request has been cancelled - please request again if necessary.", false)).queue();
                        }
                        iter.remove();
                    }
                }
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    // <<<< ROOMS >>>>

    public static VoiceChatRoom getRoom(long userId)
    {
        return ROOMS.get(userId);
    }

    public static void grantVoiceChannelMemberPerms(Channel channel, Member member)
    {
        //Allow the member voice channel permissions for the voice channel
        PermissionOverride perms = channel.getPermissionOverride(member);
        if(perms == null)
            perms = channel.createPermissionOverride(member).complete();
        perms.getManager().grant(Permission.ALL_VOICE_PERMISSIONS).complete();
    }

    public static VoiceChatRoom createRoom(Guild guild, Member member, short maxUsers)
    {
        VoiceChatRoom room = getRoom(member.getUser().getIdLong());
        if(room != null)
        {
            //Move users out of old room and give them the reputation DM
            VoiceChatRoom finalRoom = room;
            room.getUsers().forEach(u -> {
                VoiceChannel channel = guild.getVoiceChannelById(finalRoom.getChannelId());
                //Move VC members from old room
                if(BotOfThieves.VOICE_CHANNEL_MAIN != null)
                    guild.getController().moveVoiceMember(member, BotOfThieves.VOICE_CHANNEL_MAIN).complete();
                else
                    channel.delete().queue();
                finalRoom.sendUserLeaveMessage(u);
            });
            ROOMS.remove(member.getUser().getIdLong());
        }

        //Create new room
        room = new VoiceChatRoom(guild, member, maxUsers);
        ROOMS.put(member.getUser().getIdLong(), room);
        return room;
    }

    public static VoiceChannel createVoiceChannel(Guild guild, Member member)
    {
        BotOfThieves.setupVoiceChannels(guild);
        String channelName = member.getEffectiveName() + "'s Crew";
        Channel channel = BotOfThieves.VOICE_CHANNEL_CATEGORY.createVoiceChannel(channelName).complete();
        BotOfThieves.initVoiceChannelPerms(guild, channel);
        grantVoiceChannelMemberPerms(channel, member);
        return (VoiceChannel) channel;
    }

    // <<<< REQUESTS >>>>

    /**
     * Tries to add a new crew request.
     * Returns false if the user already has an active request.
     */
    public static boolean addRequest(long messageId, long userId, long channelId, long guildId, boolean favouritesOnly)
    {
        return REQUESTS.putIfAbsent(messageId, new VoiceChatRequest(messageId, userId, channelId, guildId, favouritesOnly)) == null;
    }

    /**
     * Tries to remove the request for the user.
     * Returns false if there is no active request for the user.
     */
    public static boolean removeRequest(long messageId)
    {
        return REQUESTS.remove(messageId) != null;
    }

    public static VoiceChatRequest getRequest(long messageId)
    {
        return REQUESTS.get(messageId);
    }

    public static boolean userHasRequest(long userId)
    {
        return REQUESTS.values().stream().anyMatch(request -> request.getUserId() == userId);
    }
}
