package me.hamga.cvcutils.command;

import cc.polyfrost.oneconfig.libs.universal.ChatColor;
import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils;
import me.hamga.cvcutils.CvCUtils;
import me.hamga.cvcutils.handlers.cvc.playerlists.DeathmatchPlayerListHandler;
import me.hamga.cvcutils.handlers.cvc.playerlists.DefusalPlayerListHandler;
import me.hamga.cvcutils.handlers.cvc.playerlists.GungamePlayerListHandler;
import me.hamga.cvcutils.util.PDCPerms;

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