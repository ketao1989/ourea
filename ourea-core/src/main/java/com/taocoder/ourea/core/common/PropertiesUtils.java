/*
 * Copyright (c) 2015 taocoder.com. All Rights Reserved.
 */
package com.taocoder.ourea.core.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author tao.ke Date: 16/4/25 Time: 下午10:20
 */
public class PropertiesUtils {

    public static Properties load(String fileName) {

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        return load(is);
    }

    public static Properties load(InputStream is) {

        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
        return properties;
    }

}
