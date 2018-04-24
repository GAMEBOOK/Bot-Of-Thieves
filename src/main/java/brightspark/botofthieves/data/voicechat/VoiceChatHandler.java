package brightspark.botofthieves.data.voicechat;

import brightspark.botofthieves.Config;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class VoiceChatHandler
{
    private static final Logger LOG = LoggerFactory.getLogger(VoiceChatHandler.class);
    private static final String CHAT_CATEGORY = Config.get("voice_chat_category", "voice chat");

    private static Map<String, VoiceChatRoom> ROOMS = new HashMap<>();

    public VoiceChatRoom getRoom(User user)
    {
        return ROOMS.get(user.getName());
    }

    public VoiceChatRoom createRoom(User user, short maxUsers)
    {
        VoiceChatRoom room = getRoom(user);
        if(room != null)
        {
            //Remove old room
            //TODO: Kick users out of old room and give them the reputation DM
            ROOMS.remove(user.getName());
        }
        //room = new VoiceChatRoom()

        //TEMP
        return null;
    }
}
