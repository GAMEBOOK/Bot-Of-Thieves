package brightspark.botofthieves.data.voicechat;

import brightspark.botofthieves.BotOfThieves;
import brightspark.botofthieves.Config;
import net.dv8tion.jda.core.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class VoiceChatHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(VoiceChatHandler.class);

    private static Map<String, VoiceChatRoom> ROOMS = new HashMap<>();

    public static VoiceChatRoom getRoom(User user)
    {
        return ROOMS.get(user.getName());
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
                            BotOfThieves.VOICE_CHANNEL_CATEGORY.createVoiceChannel(vcMain).queue();
                        else
                            guild.getController().createVoiceChannel(vcMain).queue();
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
}
