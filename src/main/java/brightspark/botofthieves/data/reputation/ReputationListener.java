package brightspark.botofthieves.data.reputation;

import brightspark.botofthieves.util.EmojiUtil;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class ReputationListener extends ListenerAdapter
{
    @Override
    public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent event)
    {
        if(event.getUser().isBot()) return;
        User user = ReputationHandler.getDMRatingUser(event.getMessageIdLong());
        if(user == null) return;
        String reactionName = event.getReactionEmote().getName();
        ReputationType type;
        if(EmojiUtil.GREEN_HEART.equals(reactionName))
            type = ReputationType.GOOD;
        else if(EmojiUtil.NAME_BADGE.equals(reactionName))
            type = ReputationType.BAD;
        else
            return;

        long messageID = event.getMessageIdLong();
        ReputationHandler.settleDMRating(messageID, type);
        //Remove reactions added by the bot
        event.getChannel().getMessageById(messageID).queue(
                message -> message.getReactions().forEach(
                        reaction -> reaction.removeReaction().queue()));
    }
}
