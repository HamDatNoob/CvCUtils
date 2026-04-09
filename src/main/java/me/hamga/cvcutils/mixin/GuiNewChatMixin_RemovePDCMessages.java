package me.hamga.cvcutils.mixin;

import me.hamga.cvcutils.handlers.cvc.playerlists.DefusalPlayerListHandler;
import me.hamga.cvcutils.util.DebugLogger;
import me.hamga.cvcutils.util.PDCPerms;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(GuiNewChat.class)
public class GuiNewChatMixin_RemovePDCMessages {
    @Inject(method = "printChatMessage", at = @At("HEAD"), cancellable = true)
    private void CvCUtils$onPrintChat(IChatComponent component, CallbackInfo ci) {
        String text = component.getUnformattedText();

        // Startup check if player is on podcrash
        if(PDCPerms.getIsOnPDC() == null){
            if(text.startsWith("§0[§r§l§bPLAY§r§0]§r§e Press")){
                PDCPerms.setIsOnPDC(true);

                PDCPerms.populatePerms();
            }else if(text.startsWith("Unknown command. Type \"help\" for help. ('pdc help')")){
                PDCPerms.setIsOnPDC(false);

                PDCPerms.setBlockingPDCMessages(false);
                DebugLogger.chat("Stopping blocking of PDC messages");
            }else{
                return;
            }

            ci.cancel();
            DebugLogger.log(text);
            DebugLogger.log("Player is on Podcrash: " + PDCPerms.getIsOnPDC());
        }

        if(PDCPerms.isBlockingPDCMessages()){
            if(text.startsWith("§0[§r§l§bPLAY§r§0]§r§f Resolving names...")){ // pdc group blocking 1
                ci.cancel();

            }else if(text.startsWith("§0[§r§l§bPLAY§r§0]§r§f User")){ // pdc io blocking
                String message = text.substring(23);
                String[] words = message.split(" ");

                String username =  words[1];
                boolean online = words[3].equals("online");

                DebugLogger.log("IOCheck: " + username + " - " + online);

                if(online){
                    if(PDCPerms.getDevelopers().contains(username)){
                        int i = DefusalPlayerListHandler.indexOfUsername(username);

                        DefusalPlayerListHandler.getCvcPlayers().get(i).setPerms("developer");
                    }else if(PDCPerms.getReferees().contains(username)){
                        int i = DefusalPlayerListHandler.indexOfUsername(username);

                        DefusalPlayerListHandler.getCvcPlayers().get(i).setPerms("referee");
                    }else{
                        int i = DefusalPlayerListHandler.indexOfUsername(username);

                        DefusalPlayerListHandler.getCvcPlayers().get(i).setPerms("none");
                    }
                }

                ci.cancel();

            }else if(text.startsWith("§0[§r§l§bPLAY§r§0]§r§f")){ // pdc group blocking 2
                String message = text.substring(23);

                if(PDCPerms.getPink().isEmpty()){
                    String[] users = message.split(", ");
                    for(String user : users){
                        PDCPerms.getPink().add(user);
                    }
                }else if(PDCPerms.getDevelopers().isEmpty()){
                    String[] users = message.split(", ");
                    for(String user : users){
                        PDCPerms.getDevelopers().add(user);
                    }
                }else if(PDCPerms.getReferees().isEmpty()){ // actual last thing that gets run
                    String[] users = message.split(", ");
                    for(String user : users){
                        PDCPerms.getReferees().add(user);
                    }

                    // vmin varmit same thing
                    for(String user : PDCPerms.getPink()){
                        PDCPerms.getReferees().add(user);
                    }

                    DebugLogger.log("PDC Developers: " + PDCPerms.getDevelopers());
                    DebugLogger.log("PDC Referees: " + PDCPerms.getReferees());

                    PDCPerms.setBlockingPDCMessages(false);
                    DebugLogger.chat("Stopping blocking of PDC messages");
                }

                ci.cancel();
            }
        }
    }
}