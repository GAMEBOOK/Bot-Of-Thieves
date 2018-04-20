package brightspark.botofthieves.data;

public class ReputationChangeResult
{
    private final Reputation reputation;
    private final boolean success;

    public ReputationChangeResult(Reputation reputation, boolean success)
    {
        this.reputation = reputation;
        this.success = success;
    }

    public Reputation getReputation()
    {
        return reputation;
    }

    public boolean successful()
    {
        return success;
    }
}
