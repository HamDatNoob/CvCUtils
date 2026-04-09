package me.hamga.cvcutils.config.modules;

import cc.polyfrost.oneconfig.config.annotations.Switch;

public class DebugConfig {

    @Switch(
        name = "Shutdown Logging",
        description = "Logging for when the game is about to shut down."
    )
    public static boolean debugShutdown = false;

    @Switch(
        name = "Sound Event Logging",
        description = "Logs every sound event name (Will be a lot of logs)."
    )
    public static boolean debugSoundEvents = false;

    @Switch(
        name = "Debug Hitboxes Toggled",
        description = "Logs when the debug hitbox has been toggled."
    )
    public static boolean debugHitboxToggled = true;

    @Switch(
        name = "Log CvC Icons (Direction: Forwards & Backwards)",
        description = "If a chat message was received that contains a CvC icon, log the icon(s) that were found.",
        category = "Chat"
    )
    public static boolean debugCvCIcon = false;

    @Switch(
        name = "Log CvC Icons (Direction: Any)",
        description = "If a chat message was received that contains a CvC icon, log the icon(s) that were found.",
        category = "Chat"
    )
    public static boolean debugCvCIconAny = false;

    @Switch(
        name = "Chunk Spam Logging (Only for yourself for now)",
        description = "Outputs a chat message of the detected user with the amount of spam that occurred.",
        category = "AntiCheat"
    )
    public static boolean debugACChunkSpam = true;

    // TODO: Need to make these hidden or completely remove in the future
    //  Near completely useless, just here to see responses for client <-> server
    // -----------------
    @Switch(
        name = "Log Send Packets",
        description = "Logs the packet being sent to the server.",
        category = "Developer"
    )
    public static boolean debugPacketSend = false;

    @Switch(
        name = "Log Receive Packets",
        description = "Logs the packet being received to the client.",
        category = "Developer"
    )
    public static boolean debugPacketReceive = false;
    // -----------------

}