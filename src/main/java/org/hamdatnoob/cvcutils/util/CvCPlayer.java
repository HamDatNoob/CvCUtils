package org.hamdatnoob.cvcutils.util;

import org.hamdatnoob.cvcutils.enums.games.cvc.CvCIcons;
import lombok.Getter;
import lombok.Setter;

public class CvCPlayer {
    private @Getter @Setter String username;
    private @Getter @Setter String displayName; // name with prefix
    private @Getter @Setter String team;
    private @Getter @Setter int kills;
    private @Getter @Setter int deaths;
    private @Getter @Setter int diff; // kills - deaths
    private @Getter @Setter float kdr; // kill-death ratio
    private @Getter @Setter float hsr; // headshot rate
    private @Getter @Setter int hsk; // headshot kills
    private @Getter @Setter int score;
    private @Getter @Setter boolean alive;
    private @Getter @Setter boolean ingame;
    private @Getter @Setter boolean hasBomb;
    private @Getter @Setter String perms;
    private @Getter @Setter static int copsScore;
    private @Getter @Setter static int crimsScore;
    private @Getter @Setter static boolean invalid = false; // if the scoreboard has been reset, or if the player leaves the game the headshot percentages will be invalid, or in gungame and deathmatch kills and deaths will be invalid
    private @Getter @Setter static String map;
    private @Getter @Setter static String gamemode;


    public CvCPlayer(String username, String displayName, String team, int kills, int deaths, int diff, float kdr, float hsr, int hsk, int score, boolean alive, boolean ingame, boolean hasBomb) {
        this.username = username;
        this.displayName = displayName;
        this.team = team;
        this.kills = kills;
        this.deaths = deaths;
        this.diff = diff;
        this.kdr = kdr;
        this.hsr = hsr;
        this.hsk = hsk;
        this.score = score;
        this.alive = alive;
        this.ingame = ingame;
        this.hasBomb = hasBomb;
        this.perms = "offline";
    }

    public void addKill(boolean headshot) {
        kills++;
        diff++;

        if(headshot) hsk++;
        
        kdr = (float) kills / deaths;
        hsr = (float) hsk / kills;
    }

    public void addDeath(boolean setDead) {
        deaths++;
        diff--;
        kdr = (float) kills / deaths;

        if(setDead) alive = false;
    }

    public void addScore() {
        score++;
    }

    public void changeTeam(){
        if(team.equals("§3" + CvCIcons.COPS.getForwards())){
            team = "§4" + CvCIcons.CRIMS.getForwards();
        }else{
            team = "§3" + CvCIcons.COPS.getForwards();
        }
        displayName = team + " " + username;
    }

    public static int getScoreTotal(){
        return copsScore + crimsScore;
    }

    @Override
    public String toString() {
        return "\nUsername: " + username +
               "\nDisplay Name: " + displayName +
               "\nTeam: " + team +
               "\nKills: " + kills +
               "\nHeadshot Kills: " + hsk +
               "\nDeaths: " + deaths +
               "\nDiff: " + diff +
               "\nKDR: " + kdr +
               "\nHS%: " + hsr +
               "\nScore: " + score +
               "\nAlive: " + alive +
               "\nIngame: " + ingame +
               "\nHas Bomb: " + hasBomb +
               "\nPermission Level: " + perms;
    }
}