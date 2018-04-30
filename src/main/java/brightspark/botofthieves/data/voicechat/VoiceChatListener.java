package brightspark.botofthieves.data.voicechat;

import brightspark.botofthieves.data.userdata.UserDataHandler;
import brightspark.botofthieves.util.EmojiUtil;
import brightspark.botofthieves.util.Utils;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class VoiceChatListener extends ListenerAdapter
{
    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event)
    {
        User user = event.getUser();
        String reactionName = event.getReactionEmote().getName();
        if(user.isBot() || (!EmojiUtil.GREEN_HEART.equals(reactionName) && !EmojiUtil.STAR.equals(reactionName))) return;
        long messageId = event.getMessageIdLong();
        VoiceChatRequest request = VoiceChatHandler.getRequest(messageId);
        if(request == null) return;
        TextChannel channel = event.getChannel();
        Guild guild = event.getGuild();
        VoiceChatRoom room = VoiceChatHandler.getRoom(request.getUserId());

        if(room == null)
        {
            channel.sendMessage(Utils.createBotMessage(guild, user.getAsMention() + " the crew voice chat does not exist! The request has been cancelled.", false)).queue();
            VoiceChatHandler.removeRequest(messageId);
            return;
        }

        if(request.isFavouritesOnly())
        {
            if(!EmojiUtil.STAR.equals(reactionName))
                return;
            else if(!UserDataHandler.isFavourite(request.getUserId(), user.getIdLong()))
            {
                channel.sendMessage(Utils.createBotMessage(guild, user.getAsMention() + " this crew is for favourites only!", false)).queue();
                return;
            }
        }
        else
        {
            if(!EmojiUtil.GREEN_HEART.equals(reactionName))
                return;
            else if(!UserDataHandler.isBlacklisted(request.getUserId(), user.getIdLong()))
            {
                user.openPrivateChannel().queue(c -> c.sendMessage("You are blacklisted from " + room.getName()).queue());
                return;
            }
        }

        //Try add user to the crew
        if(room.addUser(user))
        {
            VoiceChannel vc = guild.getVoiceChannelById(room.getChannelId());
            if(vc == null)
            {
                channel.sendMessage(Utils.createBotMessage(guild, user.getAsMention() + " the crew voice chat does not exist! The request has been cancelled.", false)).queue();
                VoiceChatHandler.removeRequest(messageId);
                return;
            }

            Member member = guild.getMember(user);
            VoiceChatHandler.grantVoiceChannelMemberPerms(vc, member);
            if(member.getVoiceState().inVoiceChannel())
                //Move the member to the voice channel
                guild.getController().moveVoiceMember(member, vc).queue();
            else
                channel.sendMessage(Utils.createBotMessage(guild, user.getAsMention() + " you are not already in a voice channel. Please manually join the crew " + room.getName(), false)).queue();
        }
        else
        {
            channel.sendMessage(Utils.createBotMessage(guild, user.getAsMention() + " this crew is already full! The request has been cancelled.", false)).queue();
            VoiceChatHandler.removeRequest(messageId);
        }
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event)
    {
        VoiceChannel voiceChannel = event.getChannelLeft();
        VoiceChatRoom room = VoiceChatHandler.getRoomByChannel(voiceChannel.getIdLong());
        if(room == null) return;

        User user = event.getMember().getUser();
        room.sendUserLeaveMessage(user);
        room.removeUser(user);

        if(room.getUsers().size() == 0)
            VoiceChatHandler.removeRoom(room.getUserId());
        else
            VoiceChatHandler.setRoom(room);
    }
}
