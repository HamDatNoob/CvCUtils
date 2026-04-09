package me.hamga.cvcutils.handlers.minecraft;

import me.hamga.cvcutils.util.DebugLogger;
import me.hamga.cvcutils.util.PDCPerms;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class JoinServerHandler {
    private static boolean hasRun = false;

    @SubscribeEvent
    public void onJoinServer(EntityJoinWorldEvent event) {
        if(event.world.isRemote && event.entity != Minecraft.getMinecraft().thePlayer) return; // entity is not the current user or has run before

        if(!hasRun && !Minecraft.getMinecraft().isSingleplayer()){
            PDCPerms.setBlockingPDCMessages(true);
            DebugLogger.chat("Starting blocking of PDC messages");

            PDCPerms.verifyPDC();

            hasRun = true;
        }
    }
}
