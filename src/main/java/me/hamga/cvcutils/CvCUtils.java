package me.hamga.cvcutils;

import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.events.event.InitializationEvent;
import cc.polyfrost.oneconfig.events.event.PreShutdownEvent;
import cc.polyfrost.oneconfig.libs.eventbus.Subscribe;
import cc.polyfrost.oneconfig.libs.universal.ChatColor;
import cc.polyfrost.oneconfig.libs.universal.UChat;
import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import me.hamga.cvcutils.command.ExportStatsCommand;
import me.hamga.cvcutils.command.ResetCommand;
import me.hamga.cvcutils.handlers.cvc.PerspectiveCheckHandler;
import me.hamga.cvcutils.handlers.cvc.SilentPlayersHandler;
import me.hamga.cvcutils.command.*;
import me.hamga.cvcutils.config.CvCUtilsConfig;
import me.hamga.cvcutils.config.modules.DebugConfig;
import me.hamga.cvcutils.handlers.cvc.*;
import me.hamga.cvcutils.handlers.cvc.playerlists.*;
import me.hamga.cvcutils.handlers.minecraft.*;
import me.hamga.cvcutils.util.OSUtil;
import java.util.*;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The entrypoint of the mod that initializes it.
 *
 * @see Mod
 * @see InitializationEvent
 */
@Mod(modid = CvCUtils.MODID, name = CvCUtils.MODNAME, version = CvCUtils.MODVERSION)
public class CvCUtils {

    public static final @Getter String MODID = "@MODID@";
    public static final @Getter String MODNAME = "@MODNAME@";
    public static final @Getter String MODVERSION = "@MODVER@";
    public static final @Getter String DISCAPPCLIENTID = "@DISCAPPCLIENTID@";
    // Sets the variables from `gradle.properties`. See the `blossom` config in `build.gradle.kts`.
    @Mod.Instance(MODID)
    public static @Getter CvCUtils INSTANCE; // Adds the instance of the mod, so we can access other variables.
    private @Getter CvCUtilsConfig config;
    private final @Getter Logger logger = LogManager.getLogger(MODID);
    private final @Getter String username = Minecraft.getSessionInfo().get("X-Minecraft-Username");
    private final @Getter String uuid = Minecraft.getSessionInfo().get("X-Minecraft-UUID");
    private final @Getter String avatar = "https://mc-heads.net/avatar/" + uuid;

    @Mod.EventHandler
    public void onFMLPreInitialization(FMLPreInitializationEvent event) {
        if (!OSUtil.getModsDirectory().exists() || !OSUtil.getStatsDirectory().exists()) {
            OSUtil.createModDirectory();
        }
        WindowBarHandler.setTitleBar();
    }

    @Mod.EventHandler
    public void onFMLInitialization(FMLInitializationEvent event) {
        config = new CvCUtilsConfig();
        registerHandlers();
    }

    @Subscribe
    public void onPreShutdown(PreShutdownEvent event) {
        if (CvCUtilsConfig.developerDebug && DebugConfig.debugShutdown) {
            logger.info("The game is shutting down!");
            // TODO:
            //  For the future; Log cleanup and stuff.
        }
    }

    /**
     * @param message The message you want displayed to the end user
     */
    public void sendMessage(String message) {
        UChat.chat((ChatColor.BLUE.plus(MODNAME) + ChatColor.DARK_GRAY.plus(" \u00bb ") + ChatColor.RESET) + ChatColor.Companion.translateAlternateColorCodes('&', message));
    }

    // Just so that the top of the class isn't bloated with these
    public void registerHandlers() {
        // Commands
        registerCommands(
                new ExportStatsCommand(), new ResetCommand()

        );

        // Events
        registerEvents(this,
            // Development


            // CVC
            new PerspectiveCheckHandler(), new SilentPlayersHandler(),

            // Playerlists
            new DefusalPlayerListHandler(), new DeathmatchPlayerListHandler(), new GungamePlayerListHandler(),

            // Other
            new StartupHandler(), new ShutdownHandler(), new JoinServerHandler()

        );
    }

    // TODO: Once OneConfig supports their own events, this might begin to cause issues of events
    //  being registered using separate methods that allow registering of the same event.
    //  Required for now (Could just make a separate method) but does need to be revisited
    //  in the future if issues arise such as events running twice.
    private void registerEvents(Object... events) {
        Arrays.stream(events).forEach(event -> {
            MinecraftForge.EVENT_BUS.register(event);
            EventManager.INSTANCE.register(event);
        });
    }

    private void registerCommands(Object... commands) {
        Arrays.stream(commands).forEach(CommandManager.INSTANCE::registerCommand);
    }

}