package me.hamga.cvcutils.util;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

public class CvCGame {
    private static @Getter @Setter int copsScore;
    private static @Getter @Setter int crimsScore;
    private static @Getter @Setter String map;
    private static @Getter @Setter String gamemode;
    private static final @Getter String[] rounds = new String[23];
    private static @Getter @Setter int copsLossBonus;
    private static @Getter @Setter int crimsLossBonus;

    static {
        copsScore = 0;
        crimsScore = 0;
        map = "";
        gamemode = "";
        copsLossBonus = 0;
        crimsLossBonus = 0;

        Arrays.fill(rounds, "None");
    }

    public static int getScoreTotal(){
        return copsScore + crimsScore;
    }

    public static void reset(){
        copsScore = 0;
        crimsScore = 0;
        map = "";
        gamemode = "";
        copsLossBonus = 0;
        crimsLossBonus = 0;

        Arrays.fill(rounds, "None");
    }

    public static String string(){
        return "CvCGame{" +
                "copsScore=" + copsScore +
                ", crimsScore=" + crimsScore +
                ", map='" + map + '\'' +
                ", gamemode='" + gamemode + '\'' +
                ", rounds=" + Arrays.toString(rounds) +
                ", copsLossBonus=" + copsLossBonus +
                ", crimsLossBonus=" + crimsLossBonus +
                '}';
    }
}
