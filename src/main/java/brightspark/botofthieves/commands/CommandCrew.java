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
    }

    @Override
    protected void doCommand(CommandEvent event, String... args)
    {
        if(args.length == 0)
        {
            //Reply with crew details
            VoiceChatRoom room = VoiceChatHandler.getRoom(event.getAuthor().getIdLong());
            if(room == null)
                reply(event, "You have no active room. Please use the 'crew' command to create a new room.", true);
            else
                reply(event, String.format("%s is looking for a %s person crew", event.getAuthor().getAsMention(), room.getMaxUsers()),
                        "Click the Green Heart reaction to join this crew");
            return;
        }
        String arg0 = args[0].toLowerCase();
        Guild guild = event.getGuild();
        Member sender = event.getMember();
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
                User user = getUserFromString(guild, arg);
                if(user != null) users.add(user);
            }
            if(users.size() == 0)
            {
                reply(event, "Couldn't parse any arguments to users", true);
                return;
            }
            VoiceChatRoom room = new VoiceChatRoom(guild, sender, Short.MAX_VALUE);
            users.forEach(room::addUser);
            room.sendUserLeaveMessage(event.getAuthor());
            reply(event, String.format("Sent DM to %s for the users %s", event.getAuthor().getName(), users), true);
        }
        else if(arg0.equals("channeltest"))
        {
            if(VoiceChatHandler.userHasRequest(event.getAuthor().getIdLong()))
            {
                reply(event, String.format("%s can't create crew request - you still have an active request", event.getAuthor().getAsMention()), false);
                return;
            }

            TextChannel channel = event.getTextChannel();

            VoiceChatRoom room = VoiceChatHandler.createRoom(guild, sender, (short) 4);
            MessageEmbed messageEmbed = Utils.createBotMessage(guild, String.format("Created %s", room.getName()),
                    "Click the Green Heart reaction to join this crew");
            //Send message and add reaction
            channel.sendMessage(messageEmbed).queue(message -> {
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
                    channel.sendMessage(Utils.createBotMessage(guild, sender.getAsMention() + " you are not already in a voice channel. Please manually join the voice channel " + room.getName(), false)).queue();
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
            reply(event, String.format("Removed %s empty voice channels", count[0]), true);
        }
    }
}
