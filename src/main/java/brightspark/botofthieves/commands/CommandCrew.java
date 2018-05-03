package brightspark.botofthieves.commands;

import brightspark.botofthieves.BotOfThieves;
import brightspark.botofthieves.data.voicechat.VoiceChatHandler;
import brightspark.botofthieves.data.voicechat.VoiceChatRoom;
import brightspark.botofthieves.util.EmojiUtil;
import brightspark.botofthieves.util.Utils;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.*;

import java.util.HashSet;
import java.util.Set;

public class CommandCrew extends CommandBase
{
    public CommandCrew()
    {
        super("crew", "");
        setRemoveSentMessage();
    }

    @Override
    protected void doCommand(CommandEvent event, String... args)
    {
        if(args.length == 0)
        {
            //Reply with crew details
            VoiceChatRoom room = VoiceChatHandler.getRoom(event.getAuthor().getIdLong());
            if(room == null)
                replyError(event, "You have no active room. Please use the 'crew' command to create a new room.");
            else
                reply(event, null, String.format("%s is looking for a %s person crew\nClick the Green Heart reaction to join this crew",
                        event.getAuthor().getAsMention(), room.getMaxUsers()), false);
            return;
        }
        String arg0 = args[0].toLowerCase();
        Guild guild = event.getGuild();
        Member sender = event.getMember();
        if(arg0.equals("dmtest"))
        {
            if(args.length == 1)
            {
                replyError(event, "Provide at least 1 user as an argument");
                return;
            }
            //Send user the reputation DM
            Set<User> users = new HashSet<>();
            for(String arg : args)
            {
                User user = getUserFromString(guild, arg);
                if(user != null) users.add(user);
            }
            if(users.size() == 0)
            {
                replyError(event, "Couldn't parse any arguments as users");
                return;
            }
            VoiceChatRoom room = new VoiceChatRoom(guild, sender, Short.MAX_VALUE);
            users.forEach(room::addUser);
            room.sendUserLeaveMessage(event.getAuthor());
            replySuccess(event, String.format("Sent DM to %s for the users %s", event.getAuthor().getName(), users));
        }
        else if(arg0.equals("channeltest"))
        {
            if(VoiceChatHandler.userHasRequest(event.getAuthor().getIdLong()))
            {
                replyWarning(event, String.format("%s can't create crew request - you still have an active request", event.getAuthor().getAsMention()));
                return;
            }

            TextChannel channel = event.getTextChannel();

            VoiceChatRoom room = VoiceChatHandler.createRoom(guild, sender, (short) 4);
            MessageEmbed messageEmbed = Utils.createBotMessage(guild, String.format("Created %s", room.getName()),
                    "Click the Green Heart reaction to join this crew");
            //Send message and add reaction
            replyWithConsumer(event, messageEmbed, message -> {
                VoiceChatHandler.addRequest(message.getIdLong(), event.getAuthor().getIdLong(),
                        channel.getIdLong(), guild.getIdLong(), false);
                message.addReaction(EmojiUtil.GREEN_HEART.toString()).queue();
            });
            //Move the sender into the voice channel
            VoiceChannel voiceChannel = guild.getVoiceChannelById(room.getChannelId());
            if(voiceChannel != null)
            {
                if(sender.getVoiceState().inVoiceChannel())
                    //Move the member to the voice channel
                    guild.getController().moveVoiceMember(sender, voiceChannel).queue(success -> LOG.info("Moved " + sender.getEffectiveName() + " to voice channel"));
                else
                    replyWarning(event, String.format("%s you are not already in a voice channel. Please manually join the voice channel %s", sender.getAsMention(), room.getName()));
            }
        }
        else if(arg0.equals("cleanup"))
        {
            int[] count = new int[] {0};
            BotOfThieves.setupVoiceChannels(guild);
            BotOfThieves.VOICE_CHANNEL_CATEGORY.getVoiceChannels().stream()
                    .filter(channel -> channel.getName().endsWith("Crew") && channel.getMembers().size() == 0)
                    .forEach(channel -> {
                        channel.delete().queue();
                        count[0]++;
                    });
            replySuccess(event, String.format("Removed %s empty voice channels", count[0]));
        }
    }
}
