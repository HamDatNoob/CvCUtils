package me.hamga.cvcutils.enums.cvc;

import me.hamga.cvcutils.config.CvCUtilsConfig;
import me.hamga.cvcutils.config.modules.DebugConfig;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Getter
public enum CvCIcons {

    // Weapons
    /** Forward: Kill feed | Backward: Held item tooltip */
    KNIFE("銌", "鉯"),
    /** Forward: Kill feed | Backward: Held item tooltip */
    PISTOL("銏", "鉠"),
    /** Forward: Kill feed | Backward: Held item tooltip */
    HANDGUN("銟", "銞"),
    /** Forward: Kill feed | Backward: Held item tooltip */
    MAGNUM("銎", "鉡"),
    /** Forward: Kill feed | Backward: Held item tooltip */
    SNIPER("銄銅", "鉪鉫"),
    /** Forward: Kill feed | Backward: Held item tooltip */
    BULLPUP("銔銕", "銒銓"),
    /** Forward: Kill feed | Backward: Held item tooltip */
    SMG("銍", "鉢"),
    /** Forward: Kill feed | Backward: Held item tooltip */
    RIFLE("銆銇", "鉨鉩"),
    /** Forward: Kill feed | Backward: Held item tooltip */
    CARBINE("銈銉", "鉦鉧"),
    /** Forward: Kill feed | Backward: Held item tooltip */
    SCOPED_RIFLE("銘銙", "銖銗"),
    /** Forward: Kill feed | Backward: Held item tooltip */
    SHOTGUN("銊銋", "鉤鉥"),
    /** Forward: Kill feed | Backward: Held item tooltip */
    AUTO_SHOTGUN("銜銝", "銚銛"),

    // Utility
    /** Held item and death icon */
    GRENADE("鉬", "銃"),
    /** Death to fire from a firebomb */
    DEATH_FIREBOMB("鉳"), // Maybe move this to general section? Placed here as firebomb is a utility
    /** Held Item Icon */
    FLASH_BANG("鉷"),
    /** Held Item Icon */
    SMOKE_GRENADE("鉸"),
    /** Held Item Icon */
    DECOY_GRENADE("鉺"),
    /** Held Item Icon */
    FIREBOMB("鉹"),

    // General
    /** Before teammate name tag (when on cops team), scoreboard, and game over title text */
    COPS("銐"),
    /** Before teammate name tag (when on crims team), scoreboard, and game over title text */
    CRIMS("銑"),
    /** Teammate health icon under name tag */
    HEALTH("銀"),
    /** Scoreboard icon. Grey (ChatColor.GRAY) when not purchased, green (ChatColor.GREEN) when purchased */
    HELMET("鉽", "鉿"), // NOTE: Haven't checked which is forward / backwards
    /** Scoreboard icon. Grey (ChatColor.GRAY) when not purchased, green (ChatColor.GREEN) when purchased */
    BODY_ARMOR("鉼", "鉾"), // NOTE: Haven't checked which is forward / backwards
    /** Death messages, held item tooltip, bomb timer, actionbar, tab, and teammate name tag (while they have the bomb in inventory) */
    C4_CHARGE("鉶"),
    /** Death to fall damage */
    FALL_DAMAGE("鉱鉲"),
    /** Kill final hit was a headshot */
    HEADSHOT("鉰"),

    // Unused
    WIRE_CUTTERS("鉻"),
    C4_UNABLE_PLANT("鉴"),
    C4_ABLE_PLANT("鉵"),
    DEATH_GRENADE_IMPACT("鉭鉮", "銁銂"),

    // External
    PERMS_OFFLINE("§c✖ "),
    PERMS_EXCEPTION("§8✖ "),
    PERMS_NONE("§a⬤ "),
    PERMS_REFEREE("§e✯ "),
    PERMS_DEVELOPER("§3✦ "),

    NONE("X");

    private final String forwards;
    private final String backwards;

    private static final Logger logger = LogManager.getLogger(CvCIcons.class.getSimpleName());

    CvCIcons(String forwards, String backwards) {
        this.forwards = forwards;
        this.backwards = backwards;
    }

    CvCIcons(String forwards) {
        this.forwards = forwards;
        this.backwards = null;
    }

    /**
     * Checks if the given message contains icons in a specific direction.
     *
     * @param message   The message to check for icon direction.
     * @param direction A function to extract the direction from CvCIcons.
     * @return True if the message contains icons in the specified direction, false otherwise.
     */
    public static boolean containsDirection(@NotNull String message, Function<CvCIcons, String> direction) {
        List<CvCIcons> matchedIcons = Arrays.stream(CvCIcons.values())
            .filter(icon -> direction.apply(icon) != null && message.contains(direction.apply(icon)))
            .collect(Collectors.toList());

        if (!matchedIcons.isEmpty() && CvCUtilsConfig.developerDebug && DebugConfig.debugCvCIcon) {
            List<String> matchedDirections = matchedIcons.stream()
                .map(icon -> icon + "(" + direction.apply(icon) + ")")
                .collect(Collectors.toList());
            logger.info("Matched icon(s): " + String.join(", ", matchedDirections));
        }

        return !matchedIcons.isEmpty();
    }

    /**
     * Checks if the provided message contains any forward key contained within the {@link CvCIcons} enum.
     *
     * @param message The message to check for the presence of forward keys.
     * @return true if the message contains any forward key, otherwise false.
     */
    public static boolean containsForwards(@NotNull String message) {
        return containsDirection(message, CvCIcons::getForwards);
    }

    /**
     * Checks if the provided message contains any backward key contained within the {@link CvCIcons} enum.
     *
     * @param message The message to check for the presence of backward keys.
     * @return true if the message contains any backward key, otherwise false.
     */
    public static boolean containsBackwards(@NotNull String message) {
        return containsDirection(message, CvCIcons::getBackwards);
    }

    /**
     * Checks if the provided message contains any key in any direction contained within the {@link CvCIcons} enum.
     *
     * @param message The message to check for the presence of keys in any direction.
     * @return true if the message contains any key in any direction, otherwise false.
     */
    public static boolean containsAnyDirection(@NotNull String message) {
        Function<CvCIcons, List<String>> anyDirection = icon -> {
            List<String> directions = new ArrayList<>();
            if (icon.forwards != null && message.contains(icon.forwards)) {
                directions.add(icon.forwards);
            }
            if (icon.backwards != null && message.contains(icon.backwards)) {
                directions.add(icon.backwards);
            }
            return directions;
        };

        List<CvCIcons> matchedIcons = Arrays.stream(CvCIcons.values())
            .filter(icon -> !anyDirection.apply(icon).isEmpty())
            .collect(Collectors.toList());

        if (!matchedIcons.isEmpty() && CvCUtilsConfig.developerDebug && DebugConfig.debugCvCIconAny) {
            List<String> logEntries = matchedIcons.stream()
                .map(icon -> {
                    List<String> directions = anyDirection.apply(icon);
                    return icon + "(" + String.join(", ", directions) + ")";
                })
                .collect(Collectors.toList());
            logger.info("Matched icon(s): " + String.join(", ", logEntries));
        }

        return !matchedIcons.isEmpty();
    }

    /**
     * Checks if message contains a death type (guns, grenades, c4, fall damage)
     *
     * @param message The message to search
     * @return boolean of if the message contained a death type
     */
    public static boolean containsDeathType(String message) {
        for (CvCIcons value : CvCIcons.values()) {
            int ordinal = value.ordinal();
            if ((ordinal < 14 || ordinal == 23 || ordinal == 24) && message.contains(value.getForwards())) {
                return true;
            }
        }
        return false;
    }

    /**
     * TODO: Make a method for a specific enum pair
     * <p>
     * Returns a formatted string containing enum name, forward key, and backward key (if not null) pairs, with each pair on a new line.
     * <p>
     * Method used just for a display considering the icons require a resource pack to view.
     *
     * @return Formatted string with enum name, forward key, and optional backward key pairs.
     */
    public static String getEnumPairs() {
        StringBuilder result = new StringBuilder();
        for (CvCIcons icon : CvCIcons.values()) {
            result.append(String.format("[%s] %s", icon.name(), icon.forwards));
            if (icon.backwards != null) {
                result.append(String.format(" : %s", icon.backwards));
            }
            result.append("\n");
        }
        return result.toString();
    }

}
