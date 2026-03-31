package me.hamga.cvcutils.handlers.cvc;

import me.hamga.cvcutils.CvCUtils;
import me.hamga.cvcutils.config.CvCUtilsConfig;
import me.hamga.cvcutils.config.modules.DebugConfig;
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