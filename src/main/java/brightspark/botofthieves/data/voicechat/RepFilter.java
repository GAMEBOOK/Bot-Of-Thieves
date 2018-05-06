package brightspark.botofthieves.data.voicechat;

import brightspark.botofthieves.data.reputation.Reputation;
import brightspark.botofthieves.data.reputation.ReputationHandler;
import net.dv8tion.jda.core.entities.User;

public class RepFilter
{
    public enum FilterType
    {
        GREATER('+'),
        LESS('-');

        private final char c;

        FilterType(char c)
        {
            this.c = c;
        }

        public static FilterType get(char c)
        {
            for(FilterType type : values())
                if(type.c == c)
                    return type;
            return null;
        }

        public boolean check(int num1, int num2)
        {
            switch(c)
            {
                case '+':   return num1 >= num2;
                case '-':   return num1 <= num2;
                default:    return false;
            }
        }

        @Override
        public String toString()
        {
            return String.valueOf(c);
        }
    }

    private final FilterType type;
    private final int reputation;

    private RepFilter(FilterType type, int reputation)
    {
        this.type = type;
        this.reputation = reputation;
    }

    public static RepFilter parseString(String filter)
    {
        if(!filter.matches("^\\d+[+-]$"))
            return null;
        int length = filter.length();
        FilterType type = FilterType.get(filter.charAt(length - 1));
        int reputation = Integer.parseInt(filter.substring(0, length - 1));
        return new RepFilter(type, reputation);
    }

    public FilterType getType()
    {
        return type;
    }

    public int getReputation()
    {
        return reputation;
    }

    public boolean filter(User user)
    {
        return filter(ReputationHandler.getRep(user));
    }

    public boolean filter(Reputation userRep)
    {
        return type.check((int) (userRep.getRatio() * 100), reputation);
    }

    @Override
    public String toString()
    {
        return reputation + type.toString();
    }
}
