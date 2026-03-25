package org.hamdatnoob.cvcutils.mixin;

import org.hamdatnoob.cvcutils.CvCUtils;
import java.util.List;
import net.minecraft.client.gui.GuiOverlayDebug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GuiOverlayDebug.class)
public class GuiOverlayDebugMixin_ShowCvCUtilsVersion {

    /**
     * Adds the CvCUtils version to the debug info list (on the right)
     *
     * @param list The list minecraft uses for debug info
     * @return The modified list with CvCUtils version added.
     */
    @ModifyVariable(method = "getDebugInfoRight", at = @At(value = "INVOKE", target = "Ljava/util/List;addAll(Ljava/util/Collection;)Z", shift = At.Shift.AFTER))
    private List<String> CvCUtils$showCvCUtilsVersion(List<String> list) {
        list.add("CvCUtils v" + CvCUtils.MODVERSION);
        return list;
    }

}