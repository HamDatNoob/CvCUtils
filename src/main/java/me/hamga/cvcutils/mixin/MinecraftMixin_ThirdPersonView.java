package me.hamga.cvcutils.mixin;

import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import me.hamga.cvcutils.handlers.cvc.GameCheckHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin_ThirdPersonView {

    @Inject(method = "runTick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;thirdPersonView:I"))
    private void CvCUtils$overridePerspective(CallbackInfo ci) {
        if (GameCheckHandler.isInGameCvC()) {
            UMinecraft.getSettings().thirdPersonView = 0;
        }
    }

}