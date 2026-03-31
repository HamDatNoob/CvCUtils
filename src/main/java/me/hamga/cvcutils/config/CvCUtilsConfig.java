package me.hamga.cvcutils.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.*;
import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.core.OneKeyBind;
import cc.polyfrost.oneconfig.config.data.*;
import me.hamga.cvcutils.CvCUtils;
import me.hamga.cvcutils.config.modules.DebugConfig;
import java.util.Arrays;

/**
 * The main Config entrypoint that extends the Config type and inits the config options. See <a href="https://docs.polyfrost.cc/oneconfig/config/adding-options">this link</a> for more config Options
 */
public class CvCUtilsConfig extends Config {

    // Developer
    @Info(
        text = "99% of what this category does will not be helpful for you.",
        type = InfoType.INFO, size = 2,
        category = "Developer"

    )
    private String uselessDeveloper01;

    @Switch(
        name = "Debug",
        description = "Allows for logging of information that is *useless* for normal users",
        category = "Developer", size = 2
    )
    public static boolean developerDebug = false;

    @Page(
        name = "Debug Options",
        location = PageLocation.BOTTOM,
        category = "Developer"
    )
    public static DebugConfig debugConfig = new DebugConfig();

    @KeyBind(
        name = "Test Screenshot",
        description = "Pressing the supplied key will take a screenshot of your game",
        category = "Developer"
    )
    public static OneKeyBind developerScreenshot = new OneKeyBind();

    // HUD
    @Info(
        text = "This HUD is always on, toggling enable will not work!",
        type = InfoType.WARNING, size = 2,
        category = "HUD",
        subcategory = "Username Display"
    )
    private String uselessHud01;

    // TODO: @Page(...) configuration, category based customization
    // Hitbox Configuration
    @Info(
        text = "Passive/Hostile entities not included.",
        type = InfoType.INFO, size = 2,
        category = "Hitbox"
    )
    private String uselessHitbox01;

    @Slider(
        name = "Hitbox Width",
        category = "Hitbox",
        instant = true,
        min = 1, max = 5, step = 1
    )
    public static int hitboxWidth = 1;

    @Switch(
        name = "Player Hitbox",
        category = "Hitbox"
    )
    public static boolean hitboxPlayers = true;

    @Color(
        name = "Player Hitbox Color",
        category = "Hitbox",
        allowAlpha = false
    )
    public static OneColor hitboxPlayersColor = new OneColor(255, 255, 255, 255);

    @Switch(
        name = "Self Hitbox",
        category = "Hitbox"
    )
    public static boolean hitboxSelf = false;

    @Color(
        name = "Self Hitbox Color",
        category = "Hitbox",
        allowAlpha = false
    )
    public static OneColor hitboxSelfColor = new OneColor(255, 255, 255, 255);

    @Switch(
        name = "Dropped Items Hitbox",
        category = "Hitbox"
    )
    public static boolean hitboxDroppedItems = true;

    @Color(
        name = "Dropped Items Hitbox Color",
        category = "Hitbox",
        allowAlpha = false
    )
    public static OneColor hitboxDroppedItemsColor = new OneColor(255, 255, 255, 255);

    @Switch(
        name = "Painting Hitbox",
        category = "Hitbox"
    )
    public static boolean hitboxPainting = false;

    @Color(
        name = "Painting Hitbox Color",
        category = "Hitbox",
        allowAlpha = false
    )
    public static OneColor hitboxPaintingColor = new OneColor(255, 255, 255, 255);

    @Switch(
        name = "Item Frame Hitbox",
        category = "Hitbox"
    )
    public static boolean hitboxItemFrame = false;

    @Color(
        name = "Item Frame Hitbox Color",
        category = "Hitbox",
        allowAlpha = false
    )
    public static OneColor hitboxItemFrameColor = new OneColor(255, 255, 255, 255);

    @Switch(
            name = "Enable Waypoints",
            category = "Waypoints"
    )
    public static boolean enableWaypoints = true;

    @KeyBind(
            name = "Place Waypoint",
            category = "Waypoints"
    )
    public static OneKeyBind placeWaypointKeybind = new OneKeyBind(true, 2); // middle mouse button

    @Checkbox(
            name = "Nametag",
            category = "Waypoints"
    )
    public static boolean waypointNametagVisible = true;

    @Checkbox(
            name = "Distance",
            category = "Waypoints"
    )
    public static boolean waypointDistanceVisible = true;

    @Checkbox(
            name = "Beacon Outline",
            category = "Waypoints"
    )
    public static boolean waypointOutlineVisible = true;

    @Checkbox(
            name = "Beacon Beam",
            category = "Waypoints"
    )
    public static boolean waypointBeaconVisible = false;

    @Checkbox(
            name = "Waypoint Visible Through Walls",
            category = "Waypoints"
    )
    public static boolean waypointVisibleThruWalls = false;

    @Slider(
            name = "Waypoint Despawn Delay",
            min = 5,
            max = 31,
            step = 5,
            category = "Waypoints"
    )
    public static int waypointDespawnDelay = 15;

    public CvCUtilsConfig() {
        // TODO: revisit once oneconfig fixes the booleans
        super(new Mod(CvCUtils.MODNAME, ModType.HYPIXEL), CvCUtils.MODID + ".json", true, false);
        initialize();

        try {
            Arrays.asList(
                "debugShutdown", "debugIPC", "debugSoundEvents", "debugHitboxToggled",
                "debugCvCIcon", "debugCvCIconAny", "debugACChunkSpam", "debugPacketSend",
                "debugPacketReceive"
            ).forEach(property -> addDependency("Debug Options." + property, "developerDebug"));

            Arrays.asList(
                "hitboxPlayersColor", "hitboxSelfColor", "hitboxDroppedItemsColor",
                "hitboxPaintingColor", "hitboxItemFrameColor"
            ).forEach(property -> {
                String propertyNameWithoutColor = property.replaceAll("Color$", "");
                addDependency(property, propertyNameWithoutColor);
            });

            Arrays.asList(
                    "waypointBeaconVisible", "placeWaypointKeybind", "waypointNametagVisible",
                    "waypointDistanceVisible", "waypointOutlineVisible", "waypointVisibleThruWalls",
                    "waypointDespawnDelay"
            ).forEach(property -> addDependency(property, "enableWaypoints"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}