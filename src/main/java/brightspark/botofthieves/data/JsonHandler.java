package brightspark.botofthieves.data;

import brightspark.botofthieves.BotOfThieves;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Set;

public class JsonHandler<T>
{
    private static final Logger LOG = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File file;
    private final Type type;

    public JsonHandler(String fileName)
    {
        this(new File(BotOfThieves.DATA_DIR, fileName + ".json"));
    }

    public JsonHandler(File file)
    {
        this.file = file;
        try
        {
            if(!file.exists() && file.createNewFile())
                LOG.info("Created new file " + file.getName());
        }
        catch(IOException e)
        {
            LOG.error("Error creating file " + file.getAbsolutePath(), e);
        }
        this.type = new TypeToken<Set<T>>(){}.getType();
    }

    public Set<T> read()
    {
        Set<T> set = null;
        try(JsonReader reader = new JsonReader(new FileReader(file)))
        {
            set = GSON.fromJson(reader, type);
        }
        catch(IOException e)
        {
            LOG.error("Error reading from JSON file " + file.getAbsolutePath(), e);
        }
        return set;
    }

    public void write(Set<T> set)
    {
        try(JsonWriter writer = new JsonWriter(new FileWriter(file)))
        {
            GSON.toJson(set, type, writer);
        }
        catch(IOException e)
        {
            LOG.error("Error writing to JSON file " + file.getAbsolutePath(), e);
        }
    }
}
