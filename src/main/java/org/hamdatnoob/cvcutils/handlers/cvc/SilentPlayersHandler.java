package org.hamdatnoob.cvcutils.handlers.cvc;

import org.hamdatnoob.cvcutils.CvCUtils;
import org.hamdatnoob.cvcutils.config.CvCUtilsConfig;
import org.hamdatnoob.cvcutils.config.modules.DebugConfig;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Handles checking and removing sounds which are a competitive advantage within CvC
 */
public class SilentPlayersHandler {

    @SubscribeEvent
    public void onSoundPlay(PlaySoundEvent event) {
        if (CvCUtilsConfig.developerDebug && DebugConfig.debugSoundEvents) {
            CvCUtils.getINSTANCE().getLogger().info("Sound event: " + event.name);
        }
        if (GameCheckHandler.isInGameCvC()) {
            if (event.name.startsWith("step.") || event.name.startsWith("dig.")) {
                event.result = null;
            }
        }
    }

}