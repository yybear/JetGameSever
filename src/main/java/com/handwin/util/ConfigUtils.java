package com.handwin.util;

import java.util.Properties;

/**
 * User: qgan(qgan@v5.cn)
 * Date: 14-6-16 上午11:19
 */
public class ConfigUtils {
    private static Properties appConfig;

    public static String getString(String key) {
        return appConfig.getProperty(key);
    }

    public void setAppConfig(Properties appConfig) {
        this.appConfig = appConfig;
    }
}
