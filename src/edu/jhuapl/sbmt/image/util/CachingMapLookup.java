package edu.jhuapl.sbmt.image.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.ParseException;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.ImmutableMap;

public abstract class CachingMapLookup
{
    private final Map<String, ImmutableMap<String, String>> mapMap;

    public CachingMapLookup()
    {
        super();

        this.mapMap = new TreeMap<>();
    }

    public String lookUp(String mapKey, String itemKey)
    {
        ImmutableMap<String, String> map;
        synchronized (this.mapMap)
        {
            map = mapMap.get(mapKey);
            if (map == null)
            {
                map = loadMap(mapKey);
                mapMap.put(mapKey, map);
            }
        }

        return map.get(itemKey);
    }

    protected ImmutableMap<String, String> loadMap(String mapKey)
    {
        try
        {
            File mapFile = getFile(mapKey);
            return readFile(mapFile);
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }

        return ImmutableMap.of();
    }

    protected abstract File getFile(String mapKey);

    protected ImmutableMap<String, String> readFile(File mapFile)
    {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        try (BufferedReader reader = new BufferedReader(new FileReader(mapFile)))
        {
            while (reader.ready())
            {
                String wholeLine = reader.readLine();
                String[] line = wholeLine.split("\\s*,\\s*");
                if (line[0].equals(wholeLine))
                {
                    line = wholeLine.split("\\s\\s*");
                }
                if (line.length < 2)
                    throw new ParseException(String.format("Cannot parse line \"%s\" to get key-value pair", wholeLine), line[0].length());
                String key = line[line.length - 1];
                String value = line[0];

                builder.put(key, value);
            }
        }
        catch (FileNotFoundException e)
        {
            System.err.println(e.getMessage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return builder.build();
    }

}
