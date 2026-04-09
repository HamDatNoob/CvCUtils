package me.hamga.cvcutils.handlers.minecraft;

import me.hamga.cvcutils.CvCUtils;
import me.hamga.cvcutils.util.PDCPerms;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

public class WindowBarHandler {

    private static final Logger logger = LogManager.getLogger(WindowBarHandler.class.getSimpleName());

    // Could use CvCUtilsConfig#developerDebug here but not necessary.
    public static void setTitleBar() {
        logger.info("Attempting to set custom window title...");
        try {
            String str;
            if(PDCPerms.getIsOnPDC()) {
                str = "Podcrash Play b2.0.0 with " + CvCUtils.getMODNAME() + " v" + CvCUtils.getMODVERSION();
            }else{
                str = CvCUtils.getMODNAME() + " v" + CvCUtils.getMODVERSION();
            }

            Display.setTitle(str);
            logger.info("Window title set successfully!");
        } catch (Exception e) {
            logger.error("An exception occurred trying to set the window title!", e);
        }
    }

}