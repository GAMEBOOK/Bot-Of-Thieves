package brightspark.botofthieves.data.userdata;

import java.util.HashSet;
import java.util.Set;

public class UserList
{
    private final Long userId;
    private final Set<Long> list = new HashSet<>();

    public UserList(long userId)
    {
        this.userId = userId;
    }

    public long getUserId()
    {
        return userId;
    }

    public boolean addUser(long userId)
    {
        return list.add(userId);
    }

    public boolean removeUser(long userId)
    {
        return list.remove(userId);
    }

    public boolean hasUser(long userId)
    {
        return list.contains(userId);
    }
}
