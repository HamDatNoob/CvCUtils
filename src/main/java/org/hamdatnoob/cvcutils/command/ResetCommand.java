package org.hamdatnoob.cvcutils.command;

import cc.polyfrost.oneconfig.libs.universal.ChatColor;
import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils;
import org.hamdatnoob.cvcutils.CvCUtils;
import org.hamdatnoob.cvcutils.handlers.cvc.playerlists.DeathmatchPlayerListHandler;
import org.hamdatnoob.cvcutils.handlers.cvc.playerlists.DefusalPlayerListHandler;
import org.hamdatnoob.cvcutils.handlers.cvc.playerlists.GungamePlayerListHandler;

@Command(value = "reset", description = "Resets the player list")
public class ResetCommand {
    @Main
    private void handleDefault() {
        if (!HypixelUtils.INSTANCE.isHypixel()) {
            CvCUtils.getINSTANCE().sendMessage(ChatColor.GRAY.plus("/reset is for Hypixel"));
            return;
        }

        DefusalPlayerListHandler.reset();
        DeathmatchPlayerListHandler.reset();
        GungamePlayerListHandler.reset();

        CvCUtils.getINSTANCE().sendMessage(ChatColor.GRAY.plus("Reset stored data."));
    }
}