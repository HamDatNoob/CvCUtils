package me.hamga.cvcutils.handlers.minecraft;

import me.hamga.cvcutils.CvCUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

public class WindowBarHandler {

    private static final Logger logger = LogManager.getLogger(WindowBarHandler.class.getSimpleName());

    // Could use CvCUtilsConfig#developerDebug here but not necessary.
    public static void setTitleBar() {
        logger.info("Attempting to set custom window title...");
        try {
            Display.setTitle(CvCUtils.getMODNAME() + " v" + CvCUtils.getMODVERSION());
            logger.info("Window title set successfully!");
        } catch (Exception e) {
            logger.error("An exception occurred trying to set the window title!", e);
        }
    }

}