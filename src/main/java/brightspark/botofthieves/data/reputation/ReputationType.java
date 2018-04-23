package brightspark.botofthieves.data.reputation;

public enum ReputationType
{
    GOOD,
    BAD;

    public static ReputationType fromString(String string)
    {
        return string == null ? null :
                string.equalsIgnoreCase("g") || string.equalsIgnoreCase("good") ? GOOD :
                string.equalsIgnoreCase("b") || string.equalsIgnoreCase("bad") ? BAD :
                        null;
    }

    public boolean isGood()
    {
        return this == GOOD;
    }

    @Override
    public String toString()
    {
        return name().toLowerCase();
    }
}
