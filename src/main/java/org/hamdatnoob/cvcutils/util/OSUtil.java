package org.hamdatnoob.cvcutils.util;

import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import org.hamdatnoob.cvcutils.CvCUtils;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.stream.Stream;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OSUtil {

    private static final Logger logger = LogManager.getLogger(OSUtil.class.getSimpleName());
    private static final String currentOS = System.getProperty("os.name").toLowerCase();
    public static boolean isWindows = currentOS.contains("windows");
    public static boolean isMac = currentOS.contains("mac");
    public static boolean isLinux = currentOS.contains("linux");
    public static boolean isHeadless = GraphicsEnvironment.isHeadless();

    /** The .minecraft folder for your current running instance */
    public static @Getter File minecraftDirectory = new File(UMinecraft.getMinecraft().mcDataDir.toURI());
    /** The mods folder that is within the current instance .minecraft folder */
    public static @Getter File modsDirectory = new File(minecraftDirectory, "mods");
    /** Mod specific directory for current running instance */
    public static @Getter File CvCUtilsDirectory = new File(UMinecraft.getMinecraft().mcDataDir, CvCUtils.MODNAME);
    /** Used as the storage location for exported game stat files. */
    public static @Getter File statsDirectory = new File(CvCUtilsDirectory, "stats");
    public static @Getter File screenshotsDirectory = new File(CvCUtilsDirectory, "screenshots");


    public static void createModDirectory() {
        Stream.of(CvCUtilsDirectory, statsDirectory, screenshotsDirectory).forEach(OSUtil::createDirectory);
    }

    private static void createDirectory(File directory) {
        if (!directory.exists()) {
            try {
                boolean created = directory.mkdirs();
                if (created) {
                    logger.info("Created Directory: {}", directory.getAbsolutePath());
                } else {
                    logger.warn("Failed to create directory: {}", directory.getAbsolutePath());
                }
            } catch (SecurityException e) {
                logger.error("Exception occurred while creating directory: {}", directory.getAbsolutePath(), e);
            }
        }
    }

}