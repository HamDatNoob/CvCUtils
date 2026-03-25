package org.hamdatnoob.cvcutils.command;

import cc.polyfrost.oneconfig.libs.universal.ChatColor;
import cc.polyfrost.oneconfig.libs.universal.UMinecraft;
import cc.polyfrost.oneconfig.utils.commands.annotations.*;
import cc.polyfrost.oneconfig.utils.hypixel.HypixelUtils;
import org.hamdatnoob.cvcutils.CvCUtils;
import org.hamdatnoob.cvcutils.enums.games.cvc.CvCIcons;
import org.hamdatnoob.cvcutils.enums.games.cvc.Gamemodes;
import org.hamdatnoob.cvcutils.handlers.cvc.playerlists.DeathmatchPlayerListHandler;
import org.hamdatnoob.cvcutils.handlers.cvc.playerlists.DefusalPlayerListHandler;
import org.hamdatnoob.cvcutils.handlers.cvc.playerlists.GungamePlayerListHandler;
import org.hamdatnoob.cvcutils.util.CvCPlayer;
import org.hamdatnoob.cvcutils.util.OSUtil;
import java.awt.Desktop;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.hamdatnoob.cvcutils.util.SortByKills;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Command(value = "exportstats", description = "Export stats from the previous game.", aliases = { "es", "export" })
public class ExportStatsCommand {
    private final Logger logger = LogManager.getLogger(getClass().getSimpleName());
    Desktop desktop;

    @Main
    private void handleDefault() {
        if (!HypixelUtils.INSTANCE.isHypixel()) {
            CvCUtils.getINSTANCE().sendMessage(ChatColor.GRAY.plus("/exportstats is for Hypixel"));
            return;
        }

        if (CvCPlayer.getMap() == null) {
            CvCUtils.getINSTANCE().sendMessage(ChatColor.GRAY.plus("No stats to export."));
            return;
        }

        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM.dd.yy-HH.mm.ss");
            String currentDate = dateFormat.format(new Date());

            if (!OSUtil.getStatsDirectory().exists()) {
                OSUtil.createModDirectory();
            }

            File stats = new File(OSUtil.getStatsDirectory(), "CvC-" + currentDate + ".txt");

            try {
                if (!stats.createNewFile()) {
                    logger.warn("Stats file has already been made. Path: {}", stats.getAbsolutePath());
                    if (!Desktop.isDesktopSupported()) {
                        UMinecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(String.format("%s %s ", ChatColor.BLUE.plus("CvCUtils"), ChatColor.DARK_GRAY.plus("\u00bb")))
                            .appendSibling(new ChatComponentText(ChatColor.RED.plus("Stats file has already been made. ")))
                            .appendSibling(new ChatComponentText(ChatColor.GRAY.plus("[Couldn't automatically open file]"))
                                .setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new ChatComponentText(ChatColor.RED.plus("Unsupported Environment")))))));
                        return;
                    }

                    CvCUtils.getINSTANCE().sendMessage(ChatColor.RED.plus("Stats file has already been made."));
                    desktop.open(stats);
                    return;
                }
            } catch (IOException e) {
                logger.error("Error creating stats file: {}", e.getMessage(), e);
                fallbackExportFile();
                return;
            }

            logger.info("File created: {} - Path: {}", stats.getName(), stats.getAbsolutePath());

            FileWriter writer = new FileWriter(stats);
            writer.write(format());
            writer.close();

            if (!Desktop.isDesktopSupported()) {
                UMinecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(String.format("%s %s ", ChatColor.BLUE.plus("CvCUtils"), ChatColor.DARK_GRAY.plus("\u00bb")))
                    .appendSibling(new ChatComponentText(ChatColor.YELLOW.plus("Exported Stats. ")))
                    .appendSibling(new ChatComponentText(ChatColor.GRAY.plus("[Couldn't automatically open file]"))
                        .setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentText(ChatColor.RED.plus("Unsupported Environment")))))));
                return;
            }

            CvCUtils.getINSTANCE().sendMessage(ChatColor.YELLOW.plus("Exported Stats."));
            desktop.open(stats);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private String format(){
        if(Gamemodes.wasDefusal()){
            return formatDefusal(DefusalPlayerListHandler.exportStats());
        }else if(Gamemodes.wasDeathmatch()){
            return formatDeathmatch(DeathmatchPlayerListHandler.exportStats());
        }else if(Gamemodes.wasGungame()){
            return formatGungame(GungamePlayerListHandler.exportStats());
        }else{
            return "";
        }
    }

    private String formatDefusal(ArrayList<CvCPlayer> cvcPlayers) {
        StringBuilder str = new StringBuilder("```\nCops vs Crims - ").append(CvCPlayer.getMap());
        str.append("\nA ").append(CvCPlayer.getCopsScore()).append("\n");

        ArrayList<CvCPlayer> teamCops = new ArrayList<>();
        ArrayList<CvCPlayer> teamCrims = new ArrayList<>();

        for (CvCPlayer player : cvcPlayers) {
            if (player.getTeam().equals("§3" + CvCIcons.COPS.getForwards())) {
                teamCops.add(player);
            } else {
                teamCrims.add(player);
            }
        }

        teamCops.sort(new SortByKills());
        appendPlayerInfoDefusal(str, teamCops);

        str.append("\nB ").append(CvCPlayer.getCrimsScore()).append("\n");

        teamCrims.sort(new SortByKills());
        appendPlayerInfoDefusal(str, teamCrims);

        str.append("```");

        return str.toString();
    }

    private void appendPlayerInfoDefusal(StringBuilder str, ArrayList<CvCPlayer> players) {
        for (CvCPlayer player : players) {
            String name = player.getUsername();
            int kills = player.getKills();
            int deaths = player.getDeaths();
            int _diff = player.getDiff();
            float _kdr = player.getKdr();
            float _hsr = player.getHsr();
            double hsrFloor = Math.floor(Math.log10(_hsr * 100));

            String diff = (_diff > 0) ? "+" + _diff : (_diff == 0) ? "+" + 0 : String.valueOf(_diff);
            String kdr = (Float.isInfinite(_kdr)) ? "Inf" : (_kdr == 0) ? "0.00" : (Math.floor(Math.log10(_kdr * 100)) < 3) ? String.format("%.2f", _kdr) : String.format("%.1f", _kdr);
            String hsr = (_hsr == 0) ? "0.00%" : (hsrFloor == 2) ? String.format("%.0f", _hsr * 100) + "%" : (hsrFloor == 1) ? String.format("%.1f", _hsr * 100) + "%" : String.format("%.2f", _hsr * 100) + "%";

            str.append(name).append(" ").append(kills).append("-").append(deaths).append(" ").append(diff).append(" ").append(hsr).append(" ").append(kdr);

            str.append("\n");
        }
    }

    private String formatDeathmatch(ArrayList<CvCPlayer> cvcPlayers) {
        StringBuilder str = new StringBuilder("```\nCops vs Crims - ").append(CvCPlayer.getMap());
        str.append("\nA ").append(CvCPlayer.getCopsScore()).append("\n");

        ArrayList<CvCPlayer> teamCops = new ArrayList<>();
        ArrayList<CvCPlayer> teamCrims = new ArrayList<>();

        for (CvCPlayer player : cvcPlayers) {
            if (player.getTeam().equals("§3" + CvCIcons.COPS.getForwards())) {
                teamCops.add(player);
            } else {
                teamCrims.add(player);
            }
        }

        teamCops.sort(new SortByKills());
        appendPlayerInfoDeathmatch(str, teamCops);

        str.append("\nB ").append(CvCPlayer.getCrimsScore()).append("\n");

        teamCrims.sort(new SortByKills());
        appendPlayerInfoDeathmatch(str, teamCrims);

        str.append("```");

        return str.toString();
    }

    private void appendPlayerInfoDeathmatch(StringBuilder str, ArrayList<CvCPlayer> players) {
        for (CvCPlayer player : players) {
            String name = player.getUsername();
            int kills = player.getKills();
            int deaths = player.getDeaths();
            int _diff = player.getDiff();
            float _kdr = player.getKdr();
            float _hsr = player.getHsr();
            double hsrFloor = Math.floor(Math.log10(_hsr * 100));
            int score = player.getScore();

            String diff = (_diff > 0) ? "+" + _diff : (_diff == 0) ? "+" + 0 : String.valueOf(_diff);
            String kdr = (Float.isInfinite(_kdr)) ? "Inf" : (_kdr == 0) ? "0.00" : (Math.floor(Math.log10(_kdr * 100)) < 3) ? String.format("%.2f", _kdr) : String.format("%.1f", _kdr);
            String hsr = (_hsr == 0) ? "0.00%" : (hsrFloor == 2) ? String.format("%.0f", _hsr * 100) + "%" : (hsrFloor == 1) ? String.format("%.1f", _hsr * 100) + "%" : String.format("%.2f", _hsr * 100) + "%";

            str.append(name).append(" ").append(kills).append("-").append(deaths).append(" ").append(diff).append(" ").append(hsr).append(" ").append(kdr).append(" ").append(score);

            str.append("\n");
        }
    }

    private String formatGungame(ArrayList<CvCPlayer> cvcPlayers) {
        StringBuilder str = new StringBuilder("```\nCops vs Crims - ").append(CvCPlayer.getMap());

        cvcPlayers.sort(new SortByKills());
        appendPlayerInfoGungame(str, cvcPlayers);

        str.append("```");

        return str.toString();
    }

    private void appendPlayerInfoGungame(StringBuilder str, ArrayList<CvCPlayer> players) {
        for (CvCPlayer player : players) {
            String name = player.getUsername();
            int kills = player.getKills();
            int deaths = player.getDeaths();
            int _diff = player.getDiff();
            float _kdr = player.getKdr();
            float _hsr = player.getHsr();
            double hsrFloor = Math.floor(Math.log10(_hsr * 100));
            int score = player.getScore();

            String diff = (_diff > 0) ? "+" + _diff : (_diff == 0) ? "+" + 0 : String.valueOf(_diff);
            String kdr = (Float.isInfinite(_kdr)) ? "Inf" : (_kdr == 0) ? "0.00" : (Math.floor(Math.log10(_kdr * 100)) < 3) ? String.format("%.2f", _kdr) : String.format("%.1f", _kdr);
            String hsr = (_hsr == 0) ? "0.00%" : (hsrFloor == 2) ? String.format("%.0f", _hsr * 100) + "%" : (hsrFloor == 1) ? String.format("%.1f", _hsr * 100) + "%" : String.format("%.2f", _hsr * 100) + "%";

            str.append(name).append(" ").append(kills).append("-").append(deaths).append(" ").append(diff).append(" ").append(hsr).append(" ").append(kdr).append(" ").append(score);

            str.append("\n");
        }
    }


    @SubCommand(description = "Automatically used when the normal export stats has an issue.")
    private void fallback() {
        if (!HypixelUtils.INSTANCE.isHypixel()) {
            CvCUtils.getINSTANCE().sendMessage(ChatColor.GRAY.plus("/exportstats fallback is for Hypixel"));
            return;
        }

        if (CvCPlayer.getMap() == null) {
            CvCUtils.getINSTANCE().sendMessage(ChatColor.GRAY.plus("No stats to export."));
            return;
        }

        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }

        fallbackExportFile();
    }

    private void fallbackExportFile() {
        File stats = new File("CvC-StatsFallback.txt");

        try {
            if (!stats.createNewFile()) {
                CvCUtils.getINSTANCE().sendMessage(ChatColor.RED.plus("Fallback stats file already exists, overwriting old file."));
                logger.warn("Fallback stats file already exists. Path: {}", stats.getAbsolutePath());
            }

            FileWriter writer = new FileWriter(stats);
            writer.write(format());
            writer.close();

            if (!Desktop.isDesktopSupported()) {
                UMinecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(String.format("%s %s ", ChatColor.BLUE.plus("CvCUtils"), ChatColor.DARK_GRAY.plus("\u00bb")))
                    .appendSibling(new ChatComponentText(ChatColor.YELLOW.plus("Exported Stats. ")))
                    .appendSibling(new ChatComponentText(ChatColor.GRAY.plus("[Fallback] "))
                        .setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentText(ChatColor.RED.plus("If fallback happened automatically, an error has occurred.\n" + ChatColor.RED.plus("Report this to a developer if so.")))))))
                    .appendSibling(new ChatComponentText(ChatColor.GRAY.plus("[Couldn't automatically open file]"))
                        .setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentText(ChatColor.RED.plus("Unsupported Environment")))))));
                logger.error("Desktop not supported, can't automatically open stats file.");
                return;
            }

            UMinecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(String.format("%s %s ", ChatColor.BLUE.plus("CvCUtils"), ChatColor.DARK_GRAY.plus("\u00bb")))
                .appendSibling(new ChatComponentText(ChatColor.YELLOW.plus("Exported Stats. ")))
                .appendSibling(new ChatComponentText(ChatColor.GRAY.plus("[Fallback]"))
                    .setChatStyle(new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ChatComponentText(ChatColor.RED.plus("If fallback happened automatically, an error has occurred.\n" + ChatColor.RED.plus("Report this to a developer if so."))))))));
            desktop.open(stats);
        } catch (IOException e) {
            logger.error(e);
        }
    }
}