package com.knu.service.login.manager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesManager {

    private final Properties properties;

    public PropertiesManager(String fileName) throws IOException {
        String appConfigPath = Thread.currentThread().getContextClassLoader().getResource("").getPath() + fileName;

        properties = new Properties();

        properties.load(new FileInputStream(appConfigPath));
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}
