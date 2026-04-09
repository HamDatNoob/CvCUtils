package me.hamga.cvcutils.handlers.cvc.playerlists;

import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawInfo;
import cc.polyfrost.oneconfig.utils.hypixel.LocrawUtil;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.Getter;
import me.hamga.cvcutils.CvCUtils;
import me.hamga.cvcutils.enums.cvc.CvCIcons;
import me.hamga.cvcutils.enums.cvc.Gamemodes;
import me.hamga.cvcutils.hud.TextRenderer;
import me.hamga.cvcutils.util.*;
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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DefusalPlayerListHandler { // TODO: sometimes the initial playerlist isn't quite right but idk why, also on swapping sides the tab sometimes doesn't change properly
    private @Getter static final ArrayList<CvCPlayer> cvcPlayers = new ArrayList<>();
    private static boolean challengeMode = false;
    private static final TextRenderer textRenderer = new TextRenderer();


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
            textRenderer.text("§lName").translate(x + 33, y).render();
            textRenderer.text("§lK§r-§lD").translate(x + 173, y).render();
            textRenderer.text("§lDiff").translate(x + 204, y).render();
            textRenderer.text("§lKDR").translate(x + 234, y).render();
            textRenderer.text("§lHS%").translate(x + 266, y).color(invalidColor).render();

//            String cops = CvCGame.getCopsScore() + " - " + CvCIcons.COPS.getForwards() + " Cops";
//            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(cops, xMidpoint - (float) Minecraft.getMinecraft().fontRendererObj.getStringWidth(cops) / 2, y + 14, TextColor.darkAqua);
//            index++;

            teamCops.sort(new SortByKills());

            for (CvCPlayer player : teamCops) {
                drawStats(player, index, xMidpoint, yMidpoint, tabWidth, tabHeight, padding);
                index++;
            }

            String rounds = getRoundsLine();
            String copsLossBonus = getLossBonusLine(CvCGame.getCopsLossBonus(), true);
            String crimsLossBonus = getLossBonusLine(CvCGame.getCrimsLossBonus(), false);

            textRenderer.text(copsLossBonus).translate(xMidpoint + 125, y + 14 * index + 1).center().render();

            int centered = y + 14 * index + 7;
            textRenderer.text(rounds).translate(xMidpoint, centered).center().render();
            textRenderer.text("Loss Bonus").translate(xMidpoint + 125, centered).scale(0.75f).center().render();

            index++;

            textRenderer.text(crimsLossBonus).translate(xMidpoint + 125, y + 14 * index - 1).center().render();

            index++;

//            String crims = CvCGame.getCrimsScore() + " - " + CvCIcons.CRIMS.getForwards() + " Crims";
//            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(crims, xMidpoint - (float) Minecraft.getMinecraft().fontRendererObj.getStringWidth(crims) / 2, y + 14 * index, TextColor.darkRed);
//            index++;

            teamCrims.sort(new SortByKills());

            for (CvCPlayer player : teamCrims) {
                drawStats(player, index, xMidpoint, yMidpoint, tabWidth, tabHeight, padding);
                index++;
            }

            if(CvCPlayer.isInvalid()){
                textRenderer.text("Values may be wrong due to /reset or rejoining.").translate(xMidpoint, y + 14 * index).center().color(TextColor.red).render();
            }

            event.setCanceled(true); // kills normal tab
        }
    }

    @SubscribeEvent
    public void onChatReceive(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();

        if(message == null) return;
        if(message.startsWith("Found an in-progress") || message.startsWith("Sending you to") || message.equals("The game starts in 1 second!")){ // joining game; reset stats
            reset();
        }

        LocrawInfo locraw = LocrawUtil.INSTANCE.getLocrawInfo();
        if (HypixelUtils.INSTANCE.isHypixel() && LocrawUtil.INSTANCE.isInGame() && locraw != null && locraw.getGameType() == LocrawInfo.GameType.COPS_AND_CRIMS && Gamemodes.isDefusal()) {
            if (message.contains("§")) return; // ignore actionbar messages and player chat messages

            if (CvCIcons.containsDeathType(message)) { // death message
                if(cvcPlayers.isEmpty()) return;

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
            }else if(message.contains("won the round!")){ // round won message
                if(message.contains("Criminals")){ // crims won round
                    int lossBonus = CvCGame.getCopsLossBonus();
                    if(lossBonus != 4){
                        CvCGame.setCopsLossBonus(lossBonus + 1);
                    }
                }else{ // cops won round
                    int lossBonus = CvCGame.getCrimsLossBonus();
                    if(lossBonus != 4){
                        CvCGame.setCrimsLossBonus(lossBonus + 1);
                    }
                }
            }else if (message.contains(CvCIcons.COPS.getForwards() + "   Cops and Crims   " + CvCIcons.CRIMS.getForwards())) { // end game message
                int copsRoundsScoreboard = Integer.parseInt(message.substring(0, message.indexOf(CvCIcons.COPS.getForwards())).trim());
                int crimsRoundsScoreboard = Integer.parseInt(message.substring(message.indexOf(CvCIcons.CRIMS.getForwards()) + 1).trim());

                handleScore(null, true,  copsRoundsScoreboard, crimsRoundsScoreboard);
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
                    challengeMode = sidebar.get(8).contains("[P]");

                    roundTimer = sidebar.get(6);
                } else {
                    return;
                }
            } else if (sidebar.size() > 12) {
                challengeMode = sidebar.get(12).contains("[P]");

                roundTimer = sidebar.get(10);
            } else {
                return;
            }

            CvCGame.setGamemode(locraw.getGameMode()); // sets up the map and gamemode for the export
            CvCGame.setMap(locraw.getMapName());

            NetHandlerPlayClient netHandler = Minecraft.getMinecraft().thePlayer.sendQueue;
            ArrayList<NetworkPlayerInfo> netPlayers = new ArrayList<>(netHandler.getPlayerInfoMap());

            if (netPlayers.get(0).getPlayerTeam() == null || netPlayers.get(0).getGameProfile() == null) { // game hasn't started yet
//                DebugLogger.log("Resetting stats");
//
//                reset();
                return;
            }

            boolean hasDeadPlayers = cvcPlayers.isEmpty(); // cvcPlayers is only empty at the beginning of the game, otherwise false

            for (CvCPlayer player : cvcPlayers) {
                if (!player.isAlive() && player.isIngame() && !hasDeadPlayers) {
                    hasDeadPlayers = true;
                }

                player.setHasBomb(playerHasBomb(player.getUsername()));
            }

            if (roundTimer.contains("Objective: §a02:") && hasDeadPlayers) { // NEW ROUND
                for (CvCPlayer player : cvcPlayers) {
                    if (!playerIsIngame(player.getUsername())) { // if player is not in game
                        player.setAlive(false);
                        player.setIngame(false);
                    } else { // if player is in game
                        player.setAlive(true);
                        player.setIngame(true);
                    }
                }

                DebugLogger.log("cvcPlayers size: " + cvcPlayers.size());
                DebugLogger.log(CvCGame.string());

                DebugLogger.log("chal mode: " + challengeMode);

                String score = Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName(); // gets score of game from sidebar title

                if(score.isEmpty()) return;

                handleScore(score, false, 0, 0);

                if(challengeMode){
                    if(CvCGame.getScoreTotal() == 11 || CvCGame.getScoreTotal() == 22){
                        swapSides();
                    }
                }else{
                    if(CvCGame.getScoreTotal() == 4 || CvCGame.getScoreTotal() == 8){
                        swapSides();
                    }
                }


                for(CvCPlayer p : cvcPlayers){
                    DebugLogger.log(p.toString());
                }

                netHandler = Minecraft.getMinecraft().thePlayer.sendQueue;
                netPlayers = new ArrayList<>(netHandler.getPlayerInfoMap());

                // hacky but i got no better ideas
                if(PDCPerms.getIsOnPDC() != null && PDCPerms.getIsOnPDC()){
                    PDCPerms.setBlockingPDCMessages(true);
                    DebugLogger.chat("Starting blocking of PDC messages");
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ignored) {}

                        DebugLogger.chat("Stopping blocking of PDC messages");
                        PDCPerms.setBlockingPDCMessages(false);
                    }).start();
                }

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
            case "referee":
                icon = CvCIcons.PERMS_REFEREE.getForwards();
                break;
            case "developer":
                icon = CvCIcons.PERMS_DEVELOPER.getForwards();
                break;
            default:
                icon = CvCIcons.PERMS_OFFLINE.getForwards();
                break;
        }

        int invalidColor = (CvCPlayer.isInvalid()) ? TextColor.red : TextColor.white; // if invalid show hsr as red instead of white

        Gui.drawRect(x, yMidpoint - tabHeight + (12 * i) + (2 * i) + padding, xMidpoint + tabWidth - padding, yMidpoint - tabHeight + (12 * i + 4) + (2 * i) + 12, TextColor.tabColor);
        textRenderer.text(ADL + name + hasBomb).translate(x + 2, y).render();
        textRenderer.text(icon).translate(x + 156, y).render();
        textRenderer.text(killsAndDeaths).translate(kdx + 174, y).render();
        textRenderer.text(diff).translate(x + 206, y).render();
        textRenderer.text(kdr).translate(x + 234, y).render();
        textRenderer.text(hsr).translate(x + 264, y).color(invalidColor).render();
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

        // check pdc for perm level
        if(PDCPerms.getIsOnPDC() != null && PDCPerms.getIsOnPDC()) {
            PDCPerms.onlineCheck(username);
        }

        CvCPlayer player = new CvCPlayer(username, displayName, team, kills, deaths, kills - deaths, kdr, 0f, 0, 0, false, true, false);
        cvcPlayers.add(player);

        if(CvCGame.getScoreTotal() > 0 && username.equals(CvCUtils.INSTANCE.getUsername())){
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

    public void handleScore(String score, boolean gameEnded, int copsEnd, int crimsEnd){
        int oldCopsScore = CvCGame.getCopsScore();
        int oldCrimsScore = CvCGame.getCrimsScore();
        int newCopsScore = (gameEnded) ? copsEnd : Integer.parseInt(score.substring(2, score.indexOf(CvCIcons.COPS.getForwards())).trim());
        int newCrimsScore = (gameEnded) ? crimsEnd : Integer.parseInt(score.substring(score.indexOf(CvCIcons.CRIMS.getForwards()) + 1).trim());

        boolean copsWonRound = oldCopsScore != newCopsScore;
        int lastRoundNumber = newCopsScore + newCrimsScore;
        int currentRoundNumber = lastRoundNumber + 1;

        if(lastRoundNumber != 0){
            if(copsWonRound){
                CvCGame.getRounds()[lastRoundNumber - 1] = "Cops";
            }else{
                CvCGame.getRounds()[lastRoundNumber - 1] = "Crims";
            }
        }

        CvCGame.getRounds()[currentRoundNumber - 1] = "Current";

        // this happens twice?
        CvCGame.setCopsScore(newCopsScore);  // sets cops rounds to the scoreboard at the end of round
        CvCGame.setCrimsScore(newCrimsScore); // does the same for crims

        DebugLogger.log(Arrays.toString(CvCGame.getRounds()));
    }

    private static @NotNull String getRoundsLine(){
        StringBuilder roundsLine = new StringBuilder();
        String[] rounds = CvCGame.getRounds();
        for(int i = 0; i < rounds.length; i++) {
            if(challengeMode){
                if(i == 11 || i == 22){
                    roundsLine.append(" §8| ");
                }
            }else{
                if(i == 10) return roundsLine.toString();

                if(i == 4 || i == 9){
                    roundsLine.append(" §8| ");
                }
            }

            switch(rounds[i]){
                case "None":
                    roundsLine.append("§8-");
                    break;

                case "Cops":
                    roundsLine.append("§3-");
                    break;

                case "Crims":
                    roundsLine.append("§4-");
                    break;

                case "Current":
                    roundsLine.append("§7-");
                    break;
            }
        }

        return roundsLine.toString();
    }

    private static @NotNull String getLossBonusLine(int lossBonus, boolean copsTeam){
        StringBuilder lossBonusLine = new StringBuilder();

        if(copsTeam && lossBonus != 0){
            lossBonusLine.append("§3");
        }else if(lossBonus != 0){
            lossBonusLine.append("§4");
        }

        for(int i = 0; i < lossBonus; i++){
            lossBonusLine.append("-");
        }

        if(lossBonus == 4){
            return lossBonusLine.toString();
        }

        lossBonusLine.append("§8");

        for(int i = lossBonus; i < 4; i++){
            lossBonusLine.append("-");
        }

        return lossBonusLine.toString();
    }

    private static void swapSides(){
        CvCGame.setCopsLossBonus(0);
        CvCGame.setCrimsLossBonus(0);
    }

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
        CvCGame.reset();
        CvCPlayer.setInvalid(false);
    }

    public static String getPlayerTeam(String username){
        int i = indexOfUsername(username);
        if(i == -1) return null;

        return cvcPlayers.get(i).getTeam();
    }
}

