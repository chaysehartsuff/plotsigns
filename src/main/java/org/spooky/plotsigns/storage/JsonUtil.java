package org.spooky.plotsigns.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    public static <T> void writeToJsonFile(List<T> objectList, String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filePath);

        // Ensure parent directories exist
        file.getParentFile().mkdirs();

        try {
            mapper.writeValue(file, objectList);
            System.out.println("Data written to JSON file at: " + file.getPath());
        } catch (IOException e) {
            System.err.println("Error writing data to file: " + e.getMessage());
        }
    }

    // Generic method to read any list of objects from JSON
    public static <T> List<T> readFromJsonFile(String filePath, TypeReference<List<T>> typeReference) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File(filePath), typeReference);
        } catch (IOException e) {
            System.err.println("Error reading data from file: " + e.getMessage());
            return null; // return null or an empty list in case of an error
        }
    }
}
