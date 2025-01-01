package com.insider.helper;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonReader {
    private final JSONObject locators;

    public JsonReader() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("locators.json")) {
            if (inputStream == null) {
                throw new RuntimeException("Cannot find locators.json in test resources");
            }
            JSONTokener tokener = new JSONTokener(inputStream);
            locators = new JSONObject(tokener);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load locators.json from test resources", e);
        }
    }

    public String getLocatorType(String elementName) {
        return locators.getJSONObject(elementName).getString("type");
    }

    public String getLocatorValue(String elementName) {
        return locators.getJSONObject(elementName).getString("value");
    }
}
