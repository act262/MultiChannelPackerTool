package com.micro.mcpt.util;

import java.io.*;
import java.util.Properties;

/**
 * @author act262@gmail.com
 */
public class ConfigUtil {
    public static final String FILE_NAME = "config.prop";

    /**
     * 获取Properties对象
     *
     * @throws IOException
     */
    public static Properties getProperties() throws IOException {
        File file = new File(FILE_NAME);
        if (!file.exists())
            file.createNewFile();

        Properties properties = new Properties();
        InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
        properties.load(inputStream);
        return properties;
    }

    private static Properties properties;

    /**
     * 保存指定　key-value
     */
    public static void put(String key, String value) {
        if (properties == null) {
            try {
                properties = getProperties();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        properties.setProperty(key, value);

        try {
            OutputStream outputStream = new FileOutputStream(FILE_NAME);
            properties.store(outputStream, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取指定key的value
     */
    public static String get(String key) {
        if (properties == null) {
            try {
                properties = getProperties();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties.getProperty(key);
    }
}
