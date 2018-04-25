package brightspark.botofthieves.data.voicechat;

import brightspark.botofthieves.BotOfThieves;
import brightspark.botofthieves.Config;
import brightspark.botofthieves.util.Utils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class VoiceChatHandler
{
    private static final int REQUEST_TIMEOUT;

    private static final Map<String, VoiceChatRoom> ROOMS = new HashMap<>();
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
                            channel.sendMessage(Utils.createBotMessage(guild, user.getAsMention() + " your crew request has been too long without a response. You request has been cancelled - please request again if necessary.", true)).queue();
                        }
                        iter.remove();
                    }
                }
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    // <<<< ROOMS >>>>

    public static VoiceChatRoom getRoom(User user)
    {
        return ROOMS.get(user.getName());
    }

    private static void initVoiceChannelPerms(Guild guild, Channel channel, Member member)
    {
        //Deny @everyone voice channel permissions from the voice channel
        channel.getPermissionOverride(guild.getPublicRole()).getManager().deny(Permission.ALL_VOICE_PERMISSIONS).complete();
        grantVoiceChannelMemberPerms(channel, member);
    }

    public static void grantVoiceChannelMemberPerms(Channel channel, Member member)
    {
        //Allow the member voice channel permissions for the voice channel
        channel.createPermissionOverride(member).complete()
                .getManager().grant(Permission.ALL_VOICE_PERMISSIONS).complete();
    }

    public static VoiceChatRoom createRoom(Guild guild, Member member, short maxUsers)
    {
        VoiceChatRoom room = getRoom(member.getUser());
        if(room != null)
        {
            //Move users out of old room and give them the reputation DM
            VoiceChatRoom finalRoom = room;
            room.getUsers().forEach(u -> {
                VoiceChannel channel = guild.getVoiceChannelById(finalRoom.getChannelId());

                //Create the main VC and category if they don't exist
                if(BotOfThieves.VOICE_CHANNEL_MAIN == null)
                {
                    if(BotOfThieves.VOICE_CHANNEL_CATEGORY == null)
                    {
                        String vcCategory = Config.get("voice_channel_category");
                        if(!vcCategory.isEmpty())
                            BotOfThieves.VOICE_CHANNEL_CATEGORY = (Category) guild.getController().createCategory(vcCategory).complete();
                    }

                    String vcMain = Config.get("voice_channel_main");
                    if(!vcMain.isEmpty())
                    {
                        if(BotOfThieves.VOICE_CHANNEL_CATEGORY != null)
                            BotOfThieves.VOICE_CHANNEL_CATEGORY.createVoiceChannel(vcMain).queue(c ->
                                    initVoiceChannelPerms(guild, c, member));
                        else
                            guild.getController().createVoiceChannel(vcMain).queue(c ->
                                    initVoiceChannelPerms(guild, c, member));
                    }
                }

                //Move VC members
                if(BotOfThieves.VOICE_CHANNEL_MAIN != null)
                    guild.getController().moveVoiceMember(member, BotOfThieves.VOICE_CHANNEL_MAIN).queue();
                else
                    channel.delete().queue();
                finalRoom.sendUserLeaveMessage(u);
            });
            ROOMS.remove(member.getUser().getName());
        }

        //Create new room
        room = new VoiceChatRoom(guild, member.getUser(), maxUsers);
        ROOMS.put(member.getUser().getName(), room);
        return room;
    }

    // <<<< REQUESTS >>>>

    /**
     * Tries to add a new crew request.
     * Returns false if the user already has an active request.
     */
    public static boolean addRequest(long userId, long channelId, long guildId)
    {
        return REQUESTS.putIfAbsent(userId, new VoiceChatRequest(userId, channelId, guildId)) == null;
    }

    /**
     * Tries to remove the request for the user.
     * Returns false if there is no active request for the user.
     */
    public static boolean removeRequest(long userId)
    {
        return REQUESTS.remove(userId) != null;
    }

    public static VoiceChatRequest getRequest(long userId)
    {
        return REQUESTS.get(userId);
    }
}
