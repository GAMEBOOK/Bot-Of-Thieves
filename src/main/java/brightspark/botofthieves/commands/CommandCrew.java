package brightspark.botofthieves.commands;

import brightspark.botofthieves.BotOfThieves;
import brightspark.botofthieves.data.voicechat.RepFilter;
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
        // !crew <maxCrewSize> [repFilter] <description>
        int length = args.length;
        Guild guild = event.getGuild();
        Member sender = event.getMember();
        User senderUser = event.getAuthor();

        if(length == 0)
        {
            //Reply with crew details
            //TODO: Add cooldown so users can't spam command?
            VoiceChatRoom room = VoiceChatHandler.getRoom(senderUser.getIdLong());
            if(room == null)
                replyError(event, "You have no active room. Please use the 'crew' command to create a new room.");
            else
                replyWithConsumer(event, Utils.createBotMessage(guild, String.format("%s is looking for a %s person crew!\nClick the Green Heart reaction to join them",
                        senderUser.getAsMention(), room.getMaxUsers()), false),
                        message -> VoiceChatHandler.addRequest(message.getIdLong(), senderUser.getIdLong(), room.getChannelId(),
                                guild.getIdLong(), room.isFavourites(), message.getContentDisplay()));
            return;
        }
        else
        {
            VoiceChatRoom room;
            switch(args[0].toLowerCase())
            {
                case "close":
                    //Try to close the user's current room
                    room = VoiceChatHandler.getRoom(senderUser.getIdLong());
                    if(room != null)
                    {
                        replySuccess(event, "Removed " + room.getName());
                        VoiceChatHandler.removeRoom(senderUser.getIdLong());
                    }
                    else
                        replyWarning(event, "You have no currently active crew");
                    return;
                case "cleanup":
                    //Cleans up any empty voice channels
                    if(!checkMemberPerms(sender))
                        //Don't bother giving a reply... just more clutter in the channel
                        return;
                    int[] count = new int[] {0};
                    BotOfThieves.setupVoiceChannels(guild);
                    BotOfThieves.VOICE_CHANNEL_CATEGORY.getVoiceChannels().stream()
                            .filter(channel -> channel.getName().endsWith("Crew") && channel.getMembers().size() == 0)
                            .forEach(channel -> {
                                channel.delete().queue();
                                count[0]++;
                            });
                    replySuccess(event, String.format("Removed %s empty voice channels", count[0]));
                    return;
                case "dmtest":
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
                    room = new VoiceChatRoom(guild, sender, (byte) 4);
                    users.forEach(room::addUser);
                    room.sendUserLeaveMessage(senderUser);
                    replySuccess(event, String.format("Sent DM to %s for the users %s", senderUser.getName(), users));
                    return;
                default:
                    if(VoiceChatHandler.userHasRequest(senderUser.getIdLong()))
                    {
                        replyWarning(event, String.format("%s can't create crew request - you still have an active request.\n" +
                                        "Use \"!crew\" to post your request again, or \"!crew close\" to close your crew.",
                                senderUser.getAsMention()));
                        return;
                    }
            }
        }

        byte maxSize;
        RepFilter filter = null;
        String desc = null;

        try
        {
            int sizeInt = Integer.parseInt(args[0]);
            if(sizeInt > 4)
            {
                replyError(event, "Crew size must be 4 or less!");
                return;
            }
            if(sizeInt < 1)
            {
                replyError(event, "Crew size must be larger than 0!");
                return;
            }
            maxSize = (byte) sizeInt;
        }
        catch(NumberFormatException e)
        {
            replyError(event, "Crew size isn't a number or is too big!");
            return;
        }

        if(length > 1)
            filter = RepFilter.parseString(args[1]);

        int descStart = filter == null ? 2 : 3;
        
        if(length >= descStart)
            desc = Utils.joinStrings(args, descStart);

        if(desc == null || desc.trim().isEmpty())
        {
            if(filter == null)
                desc = String.format("%s is looking for a %s person crew!", senderUser.getAsMention(), maxSize);
            else
                desc = String.format("%s is looking for a %s person crew with reputations of %s!",
                        senderUser.getAsMention(), maxSize, filter.toString());
        }

        //Create the room
        VoiceChatRoom room = VoiceChatHandler.createRoom(guild, sender, maxSize);
        //Send message and add reaction
        replyWithConsumer(event, Utils.createBotMessage(guild, desc + "\nClick the Green Heart reaction below to join them.", false),
                message -> {
            VoiceChatHandler.addRequest(message.getIdLong(), senderUser.getIdLong(), event.getChannel().getIdLong(),
                    guild.getIdLong(), false, "Test");
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
}
