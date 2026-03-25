package org.hamdatnoob.cvcutils.mixin;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.libs.universal.ChatColor;
import cc.polyfrost.oneconfig.libs.universal.UChat;
import cc.polyfrost.oneconfig.libs.universal.wrappers.UPlayer;
import cc.polyfrost.oneconfig.utils.Notifications;
import org.hamdatnoob.cvcutils.config.CvCUtilsConfig;
import org.hamdatnoob.cvcutils.config.modules.DebugConfig;
import org.hamdatnoob.cvcutils.handlers.cvc.GameCheckHandler;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderManager.class)
public class RenderManagerMixin_DebugBoundingBox {

    @Shadow
    private boolean debugBoundingBox;

    /**
     * Cancel the Vanilla rendering.
     */
    @Inject(method = "renderDebugBoundingBox", at = @At(value = "HEAD"), cancellable = true)
    private void CvCUtils$cancelVanillaHitboxes(CallbackInfo ci) {
        if (GameCheckHandler.isInCvC()) {
            ci.cancel();
        }
    }

    /**
     * Notify the user when they toggle debug hitboxes, but don't have any displays enabled in the {@link CvCUtilsConfig}
     */
    @Inject(method = "setDebugBoundingBox", at = @At("HEAD"))
    private void CvCUtils$notifyDebugHitbox(boolean debugBoundingBoxIn, CallbackInfo ci) {
        if (CvCUtilsConfig.developerDebug && DebugConfig.debugHitboxToggled) {
            UChat.chat(ChatColor.YELLOW.plus("[DEBUG] ") + ChatColor.GRAY.plus(String.format("Bounding Box : %s", (debugBoundingBoxIn ? ChatColor.GREEN.plus("Enabled") : ChatColor.RED.plus("Disabled")))));
        }
        if (CvCUtils$shouldNotifyDebugHitbox() && debugBoundingBoxIn && GameCheckHandler.isInCvC()) {
            Notifications.INSTANCE.send("CvCUtils", "No hitbox display enabled in the config.\n/CvCUtils to bring up the config menu.", 5000f);
        }
    }

    /**
     * Overrides the original hitbox rendering method from - {@link RenderManager#}
     * <p>
     * Uses the following to render a hitbox - {@link RenderManager#doRenderEntity}
     */
    @Inject(method = "doRenderEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/Render;doRender(Lnet/minecraft/entity/Entity;DDDFF)V", shift = At.Shift.AFTER))
    private void CvCUtils$overrideDebugHitboxRendering(Entity entityIn, double x, double y, double z, float entityYaw, float partialTicks, boolean p_147939_10_, CallbackInfoReturnable<Boolean> cir) {

        if (!this.CvCUtils$shouldRenderDebugBoundingBox(entityIn) || entityIn.isInvisible()) {
            return;
        }

        if (debugBoundingBox && GameCheckHandler.isInCvC() && CvCUtils$shouldRenderDebugBoundingBox(entityIn)) {
            GlStateManager.depthMask(false);
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.disableBlend();
            AxisAlignedBB axisalignedbb = entityIn.getEntityBoundingBox();
            GL11.glLineWidth(CvCUtilsConfig.hitboxWidth);
            AxisAlignedBB axisalignedbb1 = new AxisAlignedBB(axisalignedbb.minX - entityIn.posX + x, axisalignedbb.minY - entityIn.posY + y, axisalignedbb.minZ - entityIn.posZ + z, axisalignedbb.maxX - entityIn.posX + x, axisalignedbb.maxY - entityIn.posY + y, axisalignedbb.maxZ - entityIn.posZ + z);
            OneColor color = CvCUtils$getOneColor(entityIn);
            RenderGlobal.drawOutlinedBoundingBox(axisalignedbb1, color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
        }
    }

    @Unique
    private static OneColor CvCUtils$getOneColor(Entity entityIn) {
        Map<Class<? extends Entity>, OneColor> colorMap = new HashMap<>();

        if (entityIn instanceof EntityPlayer && entityIn != UPlayer.getPlayer()) {
            colorMap.put(EntityPlayer.class, CvCUtilsConfig.hitboxPlayers ? CvCUtilsConfig.hitboxPlayersColor : null);
        }
        colorMap.put(Minecraft.getMinecraft().thePlayer.getClass(), CvCUtilsConfig.hitboxSelf ? CvCUtilsConfig.hitboxSelfColor : null);
        colorMap.put(EntityItem.class, CvCUtilsConfig.hitboxDroppedItems ? CvCUtilsConfig.hitboxDroppedItemsColor : null);
        colorMap.put(EntityPainting.class, CvCUtilsConfig.hitboxPainting ? CvCUtilsConfig.hitboxPaintingColor : null);
        colorMap.put(EntityItemFrame.class, CvCUtilsConfig.hitboxItemFrame ? CvCUtilsConfig.hitboxItemFrameColor : null);

        for (Map.Entry<Class<? extends Entity>, OneColor> entry : colorMap.entrySet()) {
            if (entry.getKey().isInstance(entityIn) && entry.getValue() != null) {
                return entry.getValue();
            }
        }

        // Default color if no match is found
        return new OneColor(255, 255, 255, 255);
    }

    @Unique
    private boolean CvCUtils$shouldRenderDebugBoundingBox(Entity entityIn) {
        Map<Class<? extends Entity>, Boolean> renderMap = new HashMap<>();

        // This is for other players and has a check for yourself, so you are not within the scope
        renderMap.put(EntityPlayer.class, CvCUtilsConfig.hitboxPlayers && entityIn instanceof EntityPlayer && entityIn != UPlayer.getPlayer());
        // Only checks for your own player
        renderMap.put(Minecraft.getMinecraft().thePlayer.getClass(), CvCUtilsConfig.hitboxSelf && entityIn == Minecraft.getMinecraft().thePlayer);
        // Any item that is on the ground
        renderMap.put(EntityItem.class, CvCUtilsConfig.hitboxDroppedItems && entityIn instanceof EntityItem);
        // Hanging paintings
        renderMap.put(EntityPainting.class, CvCUtilsConfig.hitboxPainting && entityIn instanceof EntityPainting);
        // Hanging item frames
        renderMap.put(EntityItemFrame.class, CvCUtilsConfig.hitboxItemFrame && entityIn instanceof EntityItemFrame);

        for (Map.Entry<Class<? extends Entity>, Boolean> entry : renderMap.entrySet()) {
            if (entry.getKey().isInstance(entityIn) && entry.getValue()) {
                return true;
            }
        }

        // Default: return false if no match is found
        return false;
    }

    @Unique
    private boolean CvCUtils$shouldNotifyDebugHitbox() {
        // Map of hitbox config values
        Map<String, Boolean> configMap = new HashMap<>();
        configMap.put("hitboxPlayers", CvCUtilsConfig.hitboxPlayers);
        configMap.put("hitboxSelf", CvCUtilsConfig.hitboxSelf);
        configMap.put("hitboxDroppedItems", CvCUtilsConfig.hitboxDroppedItems);
        configMap.put("hitboxItemFrame", CvCUtilsConfig.hitboxItemFrame);
        configMap.put("hitboxPainting", CvCUtilsConfig.hitboxPainting);

        // Check if all hitbox config values are false
        return configMap.values().stream().noneMatch(value -> value);
    }

}