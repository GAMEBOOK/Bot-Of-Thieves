package brightspark.botofthieves.util;

import org.slf4j.event.Level;

import java.awt.*;

public enum LogLevel
{
    INFO(Level.INFO, Color.CYAN),
    ERROR(Level.ERROR, Color.RED),
    WARN(Level.WARN, Color.ORANGE),
    DEBUG(Level.DEBUG, Color.YELLOW),
    TRACE(Level.TRACE, Color.GREEN);

    public Level level;
    public Color colour;

    LogLevel(Level level, Color colour)
    {
        this.level = level;
        this.colour = colour;
    }

    @Override
    public String toString()
    {
        String name = name().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static LogLevel get(String name)
    {
        for(LogLevel level : values())
            if(level.name().equalsIgnoreCase(name))
                return level;
        return null;
    }

    public boolean isHigherThanOrEqualTo(String level)
    {
        LogLevel logLevel = get(level);
        if(logLevel == null) throw new RuntimeException("LogLevel '" + level + "' does not exist");
        return isHigherThanOrEqualTo(logLevel);
    }

    public boolean isHigherThanOrEqualTo(LogLevel level)
    {
        return ordinal() <= level.ordinal();
    }
}
