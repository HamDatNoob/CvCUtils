package org.hamdatnoob.cvcutils.handlers.cvc.playerlists;

import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawInfo;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawUtil;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.hamdatnoob.cvcutils.CvCUtils;
import org.hamdatnoob.cvcutils.enums.games.cvc.CvCIcons;
import org.hamdatnoob.cvcutils.enums.games.cvc.Gamemodes;
import org.hamdatnoob.cvcutils.util.CvCPlayer;
import org.hamdatnoob.cvcutils.util.SortByKills;
import org.hamdatnoob.cvcutils.util.TextColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DefusalPlayerListHandler { // TODO: sometimes the initial playerlist isn't quite right but idk why, also on swapping sides the tab sometimes doesn't change properly
    private static final ArrayList<CvCPlayer> cvcPlayers = new ArrayList<>();

    @SubscribeEvent
    public void onOverlayRender(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST && HypixelUtils.INSTANCE.isHypixel() && Gamemodes.wasDefusal()) {
            if (cvcPlayers.isEmpty()) {
                return;
            }

            int invalidLine = (CvCPlayer.isInvalid()) ? 1 : 0; // if invalid add an extra line to tab for the invalid explanation

            ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
            int xMidpoint = res.getScaledWidth() / 2;
            int yMidpoint = res.getScaledHeight() / 2;
            int padding = 4;
            int tabWidth = 150;
            int tabHeight = ((cvcPlayers.size() + 3 + invalidLine) * 14 + padding) / 2 + 1;
            int index = 1;
            int x = xMidpoint - tabWidth + padding;
            int y = yMidpoint - tabHeight + padding + 2;

            ArrayList<CvCPlayer> teamCops = new ArrayList<>();
            ArrayList<CvCPlayer> teamCrims = new ArrayList<>();
            for (CvCPlayer player : cvcPlayers) {
                if (player.getTeam().equals("§3" + CvCIcons.COPS.getForwards())) {
                    teamCops.add(player);
                } else {
                    teamCrims.add(player);
                }
            }

            int invalidColor = (CvCPlayer.isInvalid()) ? TextColor.red : TextColor.white; // if invalid show hsr as red instead of white

            Gui.drawRect(xMidpoint - tabWidth, yMidpoint - tabHeight, xMidpoint + tabWidth, yMidpoint + tabHeight, TextColor.tabColor);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("§lName", x + 33, y, TextColor.white);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("§lK§r-§lD", x + 173, y, TextColor.white);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("§lDiff", x + 204, y, TextColor.white);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("§lKDR", x + 234, y, TextColor.white);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("§lHS%", x + 266, y, invalidColor);

            String cops = CvCPlayer.getCopsScore() + " - " + CvCIcons.COPS.getForwards() + " Cops";
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(cops, xMidpoint - (float) Minecraft.getMinecraft().fontRendererObj.getStringWidth(cops) / 2, y + 14, TextColor.darkAqua);
            index++;

            teamCops.sort(new SortByKills());

            for (CvCPlayer player : teamCops) {
                drawStats(player, index, xMidpoint, yMidpoint, tabWidth, tabHeight, padding);
                index++;
            }

            String crims = CvCPlayer.getCrimsScore() + " - " + CvCIcons.CRIMS.getForwards() + " Crims";
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(crims, xMidpoint - (float) Minecraft.getMinecraft().fontRendererObj.getStringWidth(crims) / 2, y + 14 * index, TextColor.darkRed);
            index++;

            teamCrims.sort(new SortByKills());

            for (CvCPlayer player : teamCrims) {
                drawStats(player, index, xMidpoint, yMidpoint, tabWidth, tabHeight, padding);
                index++;
            }

            if(CvCPlayer.isInvalid()){
                String text = "Values may be wrong due to /reset or rejoining.";
                Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(text, xMidpoint - (float) Minecraft.getMinecraft().fontRendererObj.getStringWidth(text) / 2, y + 14 * index, TextColor.red);
            }
            event.setCanceled(true); // kills normal tab
        }
    }

    @SubscribeEvent
    public void onChatReceive(ClientChatReceivedEvent event) {
        LocrawInfo locraw = LocrawUtil.INSTANCE.getLocrawInfo();
        if (HypixelUtils.INSTANCE.isHypixel() && LocrawUtil.INSTANCE.isInGame() && locraw != null && locraw.getGameType() == LocrawInfo.GameType.COPS_AND_CRIMS && Gamemodes.isDefusal()) {
            String message = event.message.getUnformattedText();

            if (message.contains("§")) return; // ignore actionbar messages

            if (CvCIcons.containsDeathType(message)) {
                String[] messageParts = message.split(" ");
                if (messageParts.length == 3) { // normal kills
                    String kill = messageParts[0];
                    String weapon = messageParts[1];
                    String death = messageParts[2];
                    int killIndex = indexOfUsername(kill);
                    int deathIndex = indexOfUsername(death);

                    cvcPlayers.get(killIndex).addKill(weapon.contains(CvCIcons.HEADSHOT.getForwards()));

                    cvcPlayers.get(deathIndex).addDeath(true);
                } else if (messageParts.length == 2) { // playerless kills (fall damage, c4, self-molly, etc.)
                    String weapon = messageParts[0];
                    String death = messageParts[1];
                    int deathIndex = indexOfUsername(death);

                    if (weapon.equals(CvCIcons.PISTOL.getForwards())) {
                        return;
                    }

                    cvcPlayers.get(deathIndex).addDeath(true);
                }
            } else if (message.contains(CvCIcons.COPS.getForwards() + "   Cops and Crims   " + CvCIcons.CRIMS.getForwards())) {
                int copsRoundsScoreboard = Integer.parseInt(message.substring(0, message.indexOf(CvCIcons.COPS.getForwards())).trim());
                int crimsRoundsScoreboard = Integer.parseInt(message.substring(message.indexOf(CvCIcons.CRIMS.getForwards()) + 1).trim());
                CvCPlayer.setCopsScore(copsRoundsScoreboard);
                CvCPlayer.setCrimsScore(crimsRoundsScoreboard);
            }
        }
    }

    @SubscribeEvent
    public void onTickEvent(TickEvent event) {
        LocrawInfo locraw = LocrawUtil.INSTANCE.getLocrawInfo();
        if (HypixelUtils.INSTANCE.isHypixel() && LocrawUtil.INSTANCE.isInGame() && locraw != null && locraw.getGameType() == LocrawInfo.GameType.COPS_AND_CRIMS && Gamemodes.isDefusal()) {
            List<String> sidebar = getSidebarLines();
            String roundTimer;

            if (sidebar.size() < 10) { // make sure the sidebar has fully loaded
                if (sidebar.size() == 9) {
                    roundTimer = sidebar.get(6);
                } else {
                    return;
                }
            } else if (sidebar.size() > 10) {
                roundTimer = sidebar.get(10);
            } else {
                return;
            }

            CvCPlayer.setGamemode(locraw.getGameMode()); // sets up the map and gamemode for the export
            CvCPlayer.setMap(locraw.getMapName());

            NetHandlerPlayClient netHandler = Minecraft.getMinecraft().thePlayer.sendQueue;
            ArrayList<NetworkPlayerInfo> netPlayers = new ArrayList<>(netHandler.getPlayerInfoMap());

            if (netPlayers.get(0).getPlayerTeam() == null || netPlayers.get(0).getGameProfile() == null) { // game hasn't started yet
                reset();
                return;
            }

            boolean hasDeadPlayers = cvcPlayers.isEmpty(); // only true at the beginning of the game, otherwise false

            for (CvCPlayer player : cvcPlayers) {
                if (!player.isAlive() && player.isIngame() && !hasDeadPlayers) {
                    hasDeadPlayers = true;
                }

                player.setHasBomb(playerHasBomb(player.getUsername()));
            }

            if (roundTimer.contains("Objective: §a02:") && hasDeadPlayers) {
                for (CvCPlayer player : cvcPlayers) {
                    if (!playerIsIngame(player.getUsername())) { // if player is not in game
                        player.setAlive(false);
                        player.setIngame(false);
                    } else { // if player is in game
                        player.setAlive(true);
                        player.setIngame(true);
                    }
                }

                CvCUtils.getINSTANCE().getLogger().info("cvcPlayers size: " + cvcPlayers.size());

                String score = Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName(); // gets score of game from sidebar title

                if(score.isEmpty()) return;

                CvCPlayer.setCopsScore(Integer.parseInt(score.substring(2, score.indexOf(CvCIcons.COPS.getForwards())).trim()));  // sets cops rounds to the scoreboard at the end of round
                CvCPlayer.setCrimsScore(Integer.parseInt(score.substring(score.indexOf(CvCIcons.CRIMS.getForwards()) + 1).trim())); // does the same for crims

                for(CvCPlayer p : cvcPlayers){
                    CvCUtils.getINSTANCE().getLogger().info(p);
                }

                netHandler = Minecraft.getMinecraft().thePlayer.sendQueue;
                netPlayers = new ArrayList<>(netHandler.getPlayerInfoMap());

                for (NetworkPlayerInfo playerInfo : netPlayers) {
                    if (indexOfUsername(playerInfo.getGameProfile().getName()) == -1) { // player not in cvcPlayers
                        registerNewPlayer(playerInfo);
                    }

                    checkStats(playerInfo);
                }
            }
        }
    }

    /**
     * Draws the playerList to the screen
     *
     * @param player    The CvCPlayer to draw the stats for
     * @param i         The index of the player, for offset purposes
     * @param xMidpoint Midpoint of X axis, relative to ScreenResolution
     * @param yMidpoint Midpoint of Y axis, relative to ScreenResolution
     * @param tabWidth  Width of the player list box
     * @param tabHeight Height of the player list box
     * @param padding   Padding around the edge of the player list box
     */
    public void drawStats(CvCPlayer player, int i, int xMidpoint, int yMidpoint, int tabWidth, int tabHeight, int padding) {
        String name = player.getDisplayName();
        int x = xMidpoint - tabWidth + padding;
        int y = yMidpoint - tabHeight + padding + 14 * i + 2;

        String ADL = "§7[A] "; // alive / dead / left
        if (!player.isAlive()) {
            ADL = "§7[D] ";
            name = "§7§o" + name.substring(2);
        }

        if (!player.isIngame()) {
            ADL = "§7[L] ";
            name = "§8§o" + name.substring(2);
        }

        String hasBomb = "";
        if (player.isHasBomb()) {
            hasBomb = " §e" + CvCIcons.C4_CHARGE.getForwards();
        }

        String killsAndDeaths = player.getKills() + "-" + player.getDeaths();
        int kdx = x;
        if (player.getKills() >= 10) {
            kdx -= 6;
        }

        String diff;
        if (player.getDiff() > 0) {
            diff = "§2+" + player.getDiff();
        } else if (player.getDiff() == 0) {
            diff = "±" + 0;
        } else {
            diff = "§c" + player.getDiff();
        }

        String kdr;
        if (Float.isInfinite(player.getKdr())) {
            kdr = "Inf";
        } else if (player.getKdr() == 0) {
            kdr = "0.00";
        } else if (Math.floor(Math.log10(player.getKdr() * 100)) < 3) {
            kdr = String.format("%.2f", player.getKdr());
        } else {
            kdr = String.format("%.1f", player.getKdr());
        }

        String hsr;
        if (player.getHsr() == 0) {
            hsr = "0.00%";
        } else if (Math.floor(Math.log10(player.getHsr() * 100)) == 2) {
            hsr = String.format("%.0f", player.getHsr() * 100) + "%";
        } else if (Math.floor(Math.log10(player.getHsr() * 100)) == 1) {
            hsr = String.format("%.1f", player.getHsr() * 100) + "%";
        } else {
            hsr = String.format("%.2f", player.getHsr() * 100) + "%";
        }

        String icon;
        switch (player.getPerms()) {
            case "none":
                icon = CvCIcons.PERMS_NONE.getForwards();
                break;
            case "exception":
                icon = CvCIcons.PERMS_EXCEPTION.getForwards();
                break;
            case "screenshot":
                icon = CvCIcons.PERMS_SCREENSHOT.getForwards();
                break;
            case "full":
                icon = CvCIcons.PERMS_FULL.getForwards();
                break;
            default:
                icon = CvCIcons.PERMS_OFFLINE.getForwards();
                break;
        }

        int invalidColor = (CvCPlayer.isInvalid()) ? TextColor.red : TextColor.white; // if invalid show hsr as red instead of white

        Gui.drawRect(x, yMidpoint - tabHeight + (12 * i) + (2 * i) + padding, xMidpoint + tabWidth - padding, yMidpoint - tabHeight + (12 * i + 4) + (2 * i) + 12, TextColor.tabColor);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(ADL + name + hasBomb, x + 2, y, TextColor.white);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(icon, x + 156, y, TextColor.white);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(killsAndDeaths, kdx + 174, y, TextColor.white);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(diff, x + 206, y, TextColor.white);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(kdr, x + 234, y, TextColor.white);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(hsr, x + 264, y, invalidColor);
    }

    /**
     * Adds a new player to the cvcPlayers ArrayList
     *
     * @param playerInfo NetworkPlayerInfo of the user that joined the game
     */
    public void registerNewPlayer(NetworkPlayerInfo playerInfo) {
        String username = playerInfo.getGameProfile().getName();
        String tabName = Minecraft.getMinecraft().ingameGUI.getTabList().getPlayerName(playerInfo);
        String[] tabNameParts = tabName.split(" ");
        String team = tabNameParts[0];
        String displayName = team + " " + username;

        int kills;
        int deaths;
        if (tabNameParts.length == 5) {
            String[] stats = tabNameParts[4].split("-");
            kills = Integer.parseInt(stats[0].substring(3));
            deaths = Integer.parseInt(stats[1].substring(0, stats[1].length() - 1));
        } else if (tabNameParts.length == 3) {
            String[] stats = tabNameParts[2].split("-");
            kills = Integer.parseInt(stats[0].substring(3));
            deaths = Integer.parseInt(stats[1].substring(0, stats[1].length() - 1));
        } else {
            return;
        }

        float kdr;
        if (deaths == 0) {
            kdr = (float) kills;
        } else {
            kdr = (float) kills / deaths;
        }

        CvCPlayer player = new CvCPlayer(username, displayName, team, kills, deaths, kills - deaths, kdr, 0f, 0, 0, false, true, false);
        cvcPlayers.add(player);

        if(CvCPlayer.getScoreTotal() > 0 && username.equals(CvCUtils.INSTANCE.getUsername())){
            CvCPlayer.setInvalid(true);
        }
        //System.out.println(player);
    }

    public void checkStats(NetworkPlayerInfo playerInfo) {
        String username = playerInfo.getGameProfile().getName();
        String tabName = Minecraft.getMinecraft().ingameGUI.getTabList().getPlayerName(playerInfo);
        String[] tabNameParts = tabName.split(" ");
        String team = tabNameParts[0];
        String displayName = team + " " + username;

        int i = indexOfUsername(username);
        if(i == -1) return;

        int kills;
        int deaths;
        if (tabNameParts.length == 5) {
            String[] stats = tabNameParts[4].split("-");
            kills = Integer.parseInt(stats[0].substring(3));
            deaths = Integer.parseInt(stats[1].substring(0, stats[1].length() - 1));
        } else if (tabNameParts.length == 3) {
            String[] stats = tabNameParts[2].split("-");
            kills = Integer.parseInt(stats[0].substring(3));
            deaths = Integer.parseInt(stats[1].substring(0, stats[1].length() - 1));
        } else {
            return;
        }

        float kdr;
        if (deaths == 0) {
            kdr = (float) kills;
        } else {
            kdr = (float) kills / deaths;
        }
        
        float hsr;
        if (kills == 0){
            hsr = 0f;
        }else{
            hsr = (float) cvcPlayers.get(i).getHsk() / kills;
        }

        int oldKills = cvcPlayers.get(i).getKills();
        int oldDeaths = cvcPlayers.get(i).getDeaths();

        cvcPlayers.get(i).setTeam(team);
        cvcPlayers.get(i).setDisplayName(displayName);
        cvcPlayers.get(i).setKills(kills);
        cvcPlayers.get(i).setDeaths(deaths);
        cvcPlayers.get(i).setDiff(kills - deaths);
        cvcPlayers.get(i).setKdr(kdr);
        cvcPlayers.get(i).setHsr(hsr);

        if(oldKills != cvcPlayers.get(i).getKills() || oldDeaths != cvcPlayers.get(i).getDeaths()){
            CvCPlayer.setInvalid(true); // kills or deaths changed without a chat message, probably because of rejoining a game
        }
        //System.out.println(player);
    }

//    /**
//     * Resets the team and displayName variables in the CvCPlayer object
//     *
//     * @param playerInfo The player to reset
//     */
//    public void checkSides(NetworkPlayerInfo playerInfo) {
//        String username = playerInfo.getGameProfile().getName();
//        String tabName = Minecraft.getMinecraft().ingameGUI.getTabList().getPlayerName(playerInfo);
//        String team = tabName.split(" ")[0];
//        String displayName = team + " " + username;
//
//        int i = indexOfUsername(username);
//
//        if (i == -1) {
//            return;
//        }
//
//        cvcPlayers.get(i).setDisplayName(displayName);
//        cvcPlayers.get(i).setTeam(team);
//    }

//    public void swapSides(){
//        // checks if it is time to swap sides
//        if((!Gamemodes.isChallengeMode() && CvCPlayer.getScoreTotal() == 4) || (!Gamemodes.isChallengeMode() && CvCPlayer.getScoreTotal() == 8) || CvCPlayer.getScoreTotal() == 11 || CvCPlayer.getScoreTotal() == 22) {
//            NetHandlerPlayClient netHandler = Minecraft.getMinecraft().thePlayer.sendQueue;
//            ArrayList<NetworkPlayerInfo> netPlayers = new ArrayList<>(netHandler.getPlayerInfoMap());
//
//            for(NetworkPlayerInfo playerInfo : netPlayers){
//                String username = playerInfo.getGameProfile().getName();
//                String tabName = Minecraft.getMinecraft().ingameGUI.getTabList().getPlayerName(playerInfo);
//                String[] tabNameParts = tabName.split(" ");
//                String team = tabNameParts[0];
//                String displayName = team + " " + username;
//
//                int i = indexOfUsername(username);
//                if(i == -1) continue;
//
//                cvcPlayers.get(i).setTeam(team);
//                cvcPlayers.get(i).setDisplayName(displayName);
//            }
//        }
//    }

    /**
     * Finds the index of a certain username in the cvcPlayers ArrayList.
     *
     * @param username The username to look for
     * @return Index of the player in cvcPlayers OR if player is not found return -1
     */
    public static int indexOfUsername(String username) {
        int index = 0;
        for (CvCPlayer player : cvcPlayers) {
            if (player.getUsername().equals(username)) {
                return index;
            }
            index++;
        }

        return -1;
    }

    /**
     * Looks in the tablist to find the player with the c4
     *
     * @param username The player's username to check for
     * @return boolean based on if the player has the bomb or not
     */
    public boolean playerHasBomb(String username) {
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().thePlayer.sendQueue;
        ArrayList<NetworkPlayerInfo> netPlayers = new ArrayList<>(netHandler.getPlayerInfoMap());

        for (NetworkPlayerInfo playerInfo : netPlayers) {
            String tabName = Minecraft.getMinecraft().ingameGUI.getTabList().getPlayerName(playerInfo);
            String[] tabNameParts = tabName.split(" ");

            if (tabNameParts.length == 5 && tabNameParts[1].equals(username)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Looks for a player in the current NetworkPlayerInfo by username
     *
     * @param username The player's username to check for
     * @return boolean based on if the player is ingame or not
     */
    public boolean playerIsIngame(String username) {
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().thePlayer.sendQueue;
        ArrayList<NetworkPlayerInfo> netPlayers = new ArrayList<>(netHandler.getPlayerInfoMap());

        for (NetworkPlayerInfo playerInfo : netPlayers) {
            if (username.equals(playerInfo.getGameProfile().getName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Grabs data from the current sidebar
     *
     * @return List of strings of the lines on the scoreboard, in a random but consistent order
     */
    public List<String> getSidebarLines() {
        List<String> lines = new ArrayList<>();
        if (Minecraft.getMinecraft().theWorld == null) {
            return lines;
        }
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        if (scoreboard == null) {
            return lines;
        }

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) {
            return lines;
        }

        Collection<Score> scores = scoreboard.getSortedScores(objective);
        List<Score> list = scores.stream().filter(input -> input != null && input.getPlayerName() != null && !input.getPlayerName().startsWith("#")).collect(Collectors.toList());

        if (list.size() > 15) {
            scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
        } else {
            scores = list;
        }

        for (Score score : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
        }

        return lines;
    }

    public static ArrayList<CvCPlayer> exportStats() {
        return cvcPlayers;
    }

    public static void reset() {
        cvcPlayers.clear();
        CvCPlayer.setCopsScore(0);
        CvCPlayer.setCrimsScore(0);
        CvCPlayer.setInvalid(false);
    }

    public static String getPlayerTeam(String username){
        int i = indexOfUsername(username);
        if(i == -1) return null;

        return cvcPlayers.get(i).getTeam();
    }
}

