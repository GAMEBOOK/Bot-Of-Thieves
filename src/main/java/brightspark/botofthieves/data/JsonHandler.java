package brightspark.botofthieves.data;

import brightspark.botofthieves.BotOfThieves;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;

public class JsonHandler<T>
{
    private static final Logger LOG = LoggerFactory.getLogger(JsonHandler.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final File file;
    private final Type type;

    public JsonHandler(String fileName, TypeToken type)
    {
        this(new File(BotOfThieves.DATA_DIR, fileName + ".json"), type);
    }

    public JsonHandler(File file, TypeToken type)
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
        this.type = type.getType();
    }

    public Collection<T> read()
    {
        Collection<T> set = null;
        try(JsonReader reader = new JsonReader(new FileReader(file)))
        {
            set = GSON.fromJson(reader, type);
        }
        catch(IOException e)
        {
            LOG.error("Error reading from JSON file " + file.getAbsolutePath(), e);
        }
        return set == null ? new HashSet<>() : set;
    }

    public void write(Collection<T> values)
    {
        try(JsonWriter writer = new JsonWriter(new FileWriter(file)))
        {
            GSON.toJson(values, type, writer);
        }
        catch(IOException e)
        {
            LOG.error("Error writing to JSON file " + file.getAbsolutePath(), e);
        }
    }
}
