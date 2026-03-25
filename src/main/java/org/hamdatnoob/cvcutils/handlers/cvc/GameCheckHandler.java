package org.hamdatnoob.cvcutils.handlers.cvc;

import cc.polyfrost.oneconfig.utils.hypixel.*;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawInfo.GameType;

/**
 * Generic game type check methods
 * <p>
 * TODO: Store locraw so it can be used across the mod instead of always
 * having to do LocrawInfo locraw = ...
 */
public class GameCheckHandler {

    /**
     * Will only return true if you are
     * <p>
     * - Playing on Hypixel
     * <p>
     * - Are within a CvC game (not lobby)
     * <p>
     * - Locraw is not null (Lowcraw takes a second or 2 normally to load and store itself)
     * <p>
     * - GameType is CvC
     * <p>
     */
    public static boolean isInGameCvC() {
        LocrawInfo locraw = LocrawUtil.INSTANCE.getLocrawInfo();
        return HypixelUtils.INSTANCE.isHypixel() && LocrawUtil.INSTANCE.isInGame() && locraw != null && (locraw.getGameType() == GameType.COPS_AND_CRIMS);
    }

    /**
     * Will only return true if you are
     * <p>
     * - Playing on Hypixel
     * <p>
     * - Are within a CvC lobby (lobby or game)
     * <p>
     * - Locraw is not null (Lowcraw takes a second or 2 normally to load and store itself)
     * <p>
     * - GameType is CvC
     * <p>
     */
    public static boolean isInCvC() {
        LocrawInfo locraw = LocrawUtil.INSTANCE.getLocrawInfo();
        return HypixelUtils.INSTANCE.isHypixel() && locraw != null && (locraw.getGameType() == GameType.COPS_AND_CRIMS);
    }

    /**
     * Will only return true if you are
     * <p>
     * - Playing on Hypixel
     * <p>
     * - Are within a Replay server
     * <p>
     * - Locraw is not null (Lowcraw takes a second or 2 normally to load and store itself)
     * <p>
     * - GameType is Replay
     * <p>
     */
    public static boolean isInReplay() {
        LocrawInfo locraw = LocrawUtil.INSTANCE.getLocrawInfo();
        return HypixelUtils.INSTANCE.isHypixel() && locraw != null && (locraw.getGameType() == GameType.REPLAY);
    }

}