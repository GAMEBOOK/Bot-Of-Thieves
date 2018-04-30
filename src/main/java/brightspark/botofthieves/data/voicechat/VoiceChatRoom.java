package brightspark.botofthieves.data.voicechat;

import brightspark.botofthieves.BotOfThieves;
import brightspark.botofthieves.data.reputation.ReputationHandler;
import brightspark.botofthieves.util.EmojiUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VoiceChatRoom
{
    private final long userId;
    private final String name;
    private final Set<User> users = new HashSet<>(4);
    private final Map<User, Set<User>> usersSeen = new HashMap<>();
    private final short maxUsers;
    private Long channelId;

    public VoiceChatRoom(Guild guild, Member initialMember, short maxUsers)
    {
        VoiceChannel channel = VoiceChatHandler.createVoiceChannel(guild, initialMember);
        userId = initialMember.getUser().getIdLong();
        name = channel.getName();
        channelId = channel.getIdLong();
        users.add(initialMember.getUser());
        this.maxUsers = maxUsers;
        BotOfThieves.LOG.info(String.format("Created voice channel %s (%s)", name, channelId));
    }

    public long getUserId()
    {
        return userId;
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

    public short getMaxUsers()
    {
        return maxUsers;
    }

    public boolean addUser(User user)
    {
        addUserToSeen(user);
        users.add(user);
        return users.size() <= maxUsers;
    }

    public void removeUser(User user)
    {
        users.remove(user);
    }

    private void addUserToSeen(User user)
    {
        //Add new user as seen to all other users
        users.forEach(u -> {
            Set<User> seen = usersSeen.getOrDefault(u, new HashSet<>());
            seen.add(user);
            usersSeen.put(u, seen);
        });
        //Add all other users as seen to new user
        usersSeen.put(user, new HashSet<>(users));
    }

    public void sendUserLeaveMessage(User user)
    {
        Set<User> seen = usersSeen.get(user);
        if(seen == null || seen.isEmpty()) return;
        int[] i = new int[] {0};
        user.openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("We hope you enjoyed your game! Please provide some feedback on the players from your last game by selecting the appropriate emote:").queue();
            seen.forEach(u -> {
                if(u.equals(user)) return;
                privateChannel.sendMessage(String.format("%s. %s", ++i[0], u.getName()))
                        .queue(message -> {
                            ReputationHandler.addDMRating(message.getIdLong(), u);
                            message.addReaction(EmojiUtil.GREEN_HEART.toString()).queue();
                            message.addReaction(EmojiUtil.NAME_BADGE.toString()).queue();
                        });
            });
        });
        usersSeen.remove(user);
    }
}
