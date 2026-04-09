package me.hamga.cvcutils.util;

import me.hamga.cvcutils.CvCUtils;
import me.hamga.cvcutils.config.CvCUtilsConfig;

public class DebugLogger {
    public static void chat(String message) {
        if(CvCUtilsConfig.developerDebug) {
            CvCUtils.INSTANCE.sendMessage(message);
        }
    }

    public static void log(String message) {
        if(CvCUtilsConfig.developerDebug) {
            CvCUtils.INSTANCE.getLogger().info(message);
        }
    }
}
