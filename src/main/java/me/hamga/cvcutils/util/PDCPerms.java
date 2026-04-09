package me.hamga.cvcutils.util;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;

public class PDCPerms {
    private @Getter static final ArrayList<String> pink = new ArrayList<>(); // RAHHHH
    private @Getter static final ArrayList<String> developers = new ArrayList<>();
    private @Getter static final ArrayList<String> referees = new ArrayList<>();
    private @Getter @Setter static boolean blockingPDCMessages = true;
    private @Getter @Setter static Boolean isOnPDC = null;

    // check if the current user is online on podcrash
    public static void verifyPDC(){
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/pdc help");
    }

    // populate the arraylists with the elevated players
    public static void populatePerms(){
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/pdc group listuser pink"); // fuck u vmin
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/pdc group listuser developers");
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/pdc group listuser mods");
    }

    // check if another player is on podcrash
    public static void onlineCheck(String username){
        Minecraft.getMinecraft().thePlayer.sendChatMessage("/pdc io " + username);
    }
}
