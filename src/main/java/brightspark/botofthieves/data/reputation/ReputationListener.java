package brightspark.botofthieves.data.reputation;

import brightspark.botofthieves.util.Utils;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReputationListener extends ListenerAdapter
{
    private static final Logger LOG = LoggerFactory.getLogger(ReputationListener.class);

    @Override
    public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent event)
    {
        if(event.getUser().isBot()) return;
        User user = ReputationHandler.getDMRatingUser(event.getMessageIdLong());
        if(user == null) return;
        String reactionName = event.getReactionEmote().getName();
        ReputationType type;
        if(reactionName.equals(Utils.EMOJI_GREEN_HEART))
            type = ReputationType.GOOD;
        else if(reactionName.equals(Utils.EMOJI_NAME_BADGE))
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
