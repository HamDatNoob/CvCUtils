package org.hamdatnoob.cvcutils.handlers.cvc;

import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * Currently these methods of setting the thirdPersonView has an incompatibility with BehindYouV3 by Polyfrost. BYV3 modifies both the thirdPersonView and fov on usage with their mod. This causes screen shake and/or fov breaking and setting to 0 when this mod force sets back the thirdPersonView. Both issues come from BYV3 setting the fov within their mod from what I have seen.
 * <p>
 * Methods still prevent F5ing, though in future BYV3 updates these issues may go away on their own as BYV3 is still being actively worked on.
 * <a href="https://github.com/Polyfrost/BehindYouV3">BehindYouV3 repository</a>
 */
public class PerspectiveCheckHandler {


    // Checks once upon the WorldEvent.Load event firing if your perspective should be modified
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        int viewMode = UMinecraft.getSettings().thirdPersonView;
        if (GameCheckHandler.isInGameCvC()) {
            if (viewMode != 0) {
                UMinecraft.getSettings().thirdPersonView = 0;
                UMinecraft.getSettings().saveOptions();
            }
        }
    }

    // Ideally you'd want this to run as frequent as possible, though may take a hit at performance
    // Will need testing in the future.
    // Checks every tick if your perspective should be modified to attempt preventing F5ing.
    @SubscribeEvent
    public void onTickEvent(TickEvent event) {
        int viewMode = UMinecraft.getSettings().thirdPersonView;
        if (GameCheckHandler.isInGameCvC()) {
            if (viewMode != 0) {
                UMinecraft.getSettings().thirdPersonView = 0;
                UMinecraft.getSettings().saveOptions();
            }
        }
    }

}