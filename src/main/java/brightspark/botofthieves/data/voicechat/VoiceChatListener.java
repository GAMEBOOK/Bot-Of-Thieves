package brightspark.botofthieves.data.voicechat;

import brightspark.botofthieves.util.Utils;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class VoiceChatListener extends ListenerAdapter
{
    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event)
    {
        User user = event.getUser();
        if(user.isBot()) return;
        VoiceChatRequest request = VoiceChatHandler.getRequest(user.getIdLong());
        if(request == null) return;
        VoiceChatRoom room = VoiceChatHandler.getRoom(user);
        TextChannel channel = event.getChannel();
        Guild guild = event.getGuild();
        if(room == null)
        {
            channel.sendMessage(Utils.createBotMessage(guild, user.getAsMention() + " the crew voice chat does not exist! The request has been cancelled.", true)).queue();
            VoiceChatHandler.removeRequest(request.getUserId());
            return;
        }

        //Try add user to the crew
        if(room.addUser(user))
        {
            VoiceChannel vc = guild.getVoiceChannelById(room.getChannelId());
            if(vc == null)
            {
                channel.sendMessage(Utils.createBotMessage(guild, user.getAsMention() + " the crew voice chat does not exist! The request has been cancelled.", true)).queue();
                VoiceChatHandler.removeRequest(request.getUserId());
                return;
            }

            Member member = guild.getMember(user);
            if(member.getVoiceState().inVoiceChannel())
            {
                //Move the member to the voice channel
                VoiceChatHandler.grantVoiceChannelMemberPerms(vc, member);
                guild.getController().moveVoiceMember(member, vc).queue();
            }
            else
                channel.sendMessage(Utils.createBotMessage(guild, user.getAsMention() + " you are not already in a voice channel. Please manually join the crew " + room.getName(), true)).queue();
        }
        else
        {
            channel.sendMessage(Utils.createBotMessage(guild, user.getAsMention() + " this crew is already full! The request has been cancelled.", true)).queue();
            VoiceChatHandler.removeRequest(request.getUserId());
        }
    }
}
