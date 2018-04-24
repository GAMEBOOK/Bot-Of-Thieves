package brightspark.botofthieves.commands;

import brightspark.botofthieves.data.voicechat.VoiceChatHandler;
import brightspark.botofthieves.data.voicechat.VoiceChatRoom;
import brightspark.botofthieves.util.Utils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

import java.util.HashSet;
import java.util.Set;

public class CommandCrew extends CommandBase
{
    public CommandCrew()
    {
        super("crew", "");
    }

    @Override
    protected void doCommand(CommandEvent event, String... args)
    {
        String arg0 = args[0].toLowerCase();
        if(arg0.equals("dmtest"))
        {
            if(args.length == 1)
            {
                reply(event, "Provide at least 1 user as an argument", true);
                return;
            }
            //Send user the reputation DM
            Set<User> users = new HashSet<>();
            for(String arg : args)
            {
                Member member = getMemberFromString(event, arg);
                if(member != null) users.add(member.getUser());
            }
            if(users.size() == 0)
            {
                reply(event, "Couldn't parse any arguments to users", true);
                return;
            }
            VoiceChatRoom room = new VoiceChatRoom(event.getAuthor(), Short.MAX_VALUE);
            users.forEach(room::addUser);
            room.sendUserLeaveMessage(event.getAuthor());
            reply(event, String.format("Sent DM to %s for the users %s", event.getAuthor().getName(), users), true);
        }
        else if(arg0.equals("channeltest"))
        {
            VoiceChatRoom room = VoiceChatHandler.createRoom(event.getGuild(), event.getMember(), (short) 4);
            MessageEmbed messageEmbed = Utils.createBotMessage(event.getGuild(), String.format("Created voice channel '%s' (%s)", room.getName(), room.getChannelId()),
                    "Click the Green Heart reaction to join this crew");
            //Send message and add reaction
            //TODO: Track message ID to add members who react to the VC channel
            event.getChannel().sendMessage(messageEmbed).queue(message -> message.addReaction(Utils.EMOJI_GREEN_HEART).queue());
        }
    }
}
