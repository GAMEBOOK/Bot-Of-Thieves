package brightspark.botofthieves;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;

public class Config
{
    private static Logger LOG = LogManager.getLogger();
    private static Map<String, String> CONFIG = new HashMap<>();

    private static void checkFile()
    {
        if(!BotOfThieves.CONFIG_FILE.exists())
        {
            try
            {
                if(!BotOfThieves.CONFIG_FILE.createNewFile())
                    LOG.error("Couldn't create config.properties");
                LOG.info("Created new config.properties");
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static String get(String key)
    {
        return get(key, "");
    }

    public static String get(String key, String defaultValue)
    {
        String value = CONFIG.putIfAbsent(key, defaultValue);
        return value == null ? defaultValue : value;
    }

    public static void read()
    {
        checkFile();

        Properties properties = new Properties();
        try(InputStream input = new FileInputStream(BotOfThieves.CONFIG_FILE))
        {
            //Load properties
            properties.load(input);
            properties.forEach((o, o2) ->
            {
                if(o instanceof String && o2 instanceof String)
                {
                    CONFIG.put((String) o, (String) o2);
                    LOG.debug("Loaded config -> K: " + o + ", V: " + o2);
                }
            });
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void save()
    {
        checkFile();

        Properties properties = new Properties();
        try(OutputStream output = new FileOutputStream(BotOfThieves.CONFIG_FILE))
        {
            //Set the properties
            CONFIG.forEach(properties::setProperty);
            //Save properties
            properties.store(output, null);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
