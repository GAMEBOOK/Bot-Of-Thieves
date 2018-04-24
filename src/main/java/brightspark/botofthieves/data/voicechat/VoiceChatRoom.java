package brightspark.botofthieves.data.voicechat;

import brightspark.botofthieves.BotOfThieves;
import brightspark.botofthieves.data.reputation.ReputationHandler;
import brightspark.botofthieves.util.Utils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.util.HashSet;
import java.util.Set;

public class VoiceChatRoom
{
    private final String name;
    private final Set<User> users = new HashSet<>(4);
    private final short maxUsers;
    private Long channelId;

    public VoiceChatRoom(Guild guild, User initialUser, short maxUsers)
    {
        this(initialUser, maxUsers);
        //Create voice channel
        channelId = guild.getController().createVoiceChannel(name).complete().getIdLong();
        BotOfThieves.LOG.info(String.format("Created voice channel %s (%s)", name, channelId));
    }

    //Only used in test command atm
    public VoiceChatRoom(User initialUser, short maxUsers)
    {
        name = initialUser.getName() + "'s Crew";
        users.add(initialUser);
        this.maxUsers = maxUsers;
    }

    public String getName()
    {
        return name;
    }

    public Set<User> getUsers()
    {
        return users;
    }

    public Long getChannelId()
    {
        return channelId;
    }

    public boolean addUser(User user)
    {
        return users.size() < maxUsers && users.add(user);
    }

    public void sendUserLeaveMessage(User user)
    {
        int[] i = new int[] {0};
        user.openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("We hope you enjoyed your game! Please provide some feedback on the players from your last game by selecting the appropriate emote:").queue();
            users.forEach(u -> {
                if(u.equals(user)) return;
                privateChannel.sendMessage(String.format("%s. %s", ++i[0], u.getName()))
                        .queue(message -> {
                            ReputationHandler.addDMRating(message.getIdLong(), u);
                            message.addReaction(Utils.EMOJI_GREEN_HEART).queue();
                            message.addReaction(Utils.EMOJI_NAME_BADGE).queue();
                        });
            });
        });
    }
}
