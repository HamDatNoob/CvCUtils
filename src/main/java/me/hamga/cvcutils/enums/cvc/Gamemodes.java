package me.hamga.cvcutils.enums.cvc;

import cc.polyfrost.oneconfig.utils.hypixel.LocrawInfo;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawUtil;
import me.hamga.cvcutils.util.CvCGame;
import me.hamga.cvcutils.util.CvCPlayer;
import lombok.Getter;

@Getter
public enum Gamemodes {
    DEFUSAL("normal"),
    DEATHMATCH("deathmatch"),
    GUNGAME("gungame"),
    CHALLENGE_DEFUSAL("normal_party"),
    CHALLENGE_DEATHMATCH("deathmatch_party");

    private final String gameType;

    Gamemodes(String gameType){
        this.gameType = gameType;
    }

    public static boolean isDefusal(){
        LocrawInfo locraw = LocrawUtil.INSTANCE.getLocrawInfo();
        if(locraw == null) return false;

        String gameType = locraw.getGameMode();

        if(gameType == null) return false;
        return gameType.equals(DEFUSAL.gameType) || gameType.equals(CHALLENGE_DEFUSAL.gameType);
    }

    public static boolean isChallengeMode(){
        LocrawInfo locraw = LocrawUtil.INSTANCE.getLocrawInfo();
        if(locraw == null) return false;

        String gameType = locraw.getGameMode();

        if(gameType == null) return false;
        return gameType.equals(CHALLENGE_DEFUSAL.gameType);
    }

    public static boolean isDeathmatch(){
        LocrawInfo locraw = LocrawUtil.INSTANCE.getLocrawInfo();
        if(locraw == null) return false;

        String gameType = locraw.getGameMode();

        if(gameType == null) return false;
        return gameType.equals(DEATHMATCH.gameType) || gameType.equals(CHALLENGE_DEATHMATCH.gameType);
    }

    public static boolean isGungame(){
        LocrawInfo locraw = LocrawUtil.INSTANCE.getLocrawInfo();
        if(locraw == null) return false;

        String gameType = locraw.getGameMode();

        if(gameType == null) return false;
        return gameType.equals(GUNGAME.gameType);
    }

    public static boolean wasDefusal(){
        String gameType = CvCGame.getGamemode();
        if(gameType == null) return false;
        return gameType.equals(DEFUSAL.gameType) || gameType.equals(CHALLENGE_DEFUSAL.gameType);
    }

    public static boolean wasDeathmatch(){
        String gameType = CvCGame.getGamemode();
        if(gameType == null) return false;
        return gameType.equals(DEATHMATCH.gameType) || gameType.equals(CHALLENGE_DEATHMATCH.gameType);
    }

    public static boolean wasGungame(){
        String gameType = CvCGame.getGamemode();
        if(gameType == null) return false;
        return gameType.equals(GUNGAME.gameType);
    }
}
