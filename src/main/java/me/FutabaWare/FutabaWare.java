package me.FutabaWare;

import me.FutabaWare.manager.*;
import me.FutabaWare.util.DiscordPresence;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

import static me.FutabaWare.FutabaWare.*;

@Mod(modid = MODID, name = MODNAME, version = MODVER)
public class FutabaWare {
    public static final String MODID = "futabaware";
    public static final String MODNAME = "FutabaWare";
    public static final String MODVER = "1.3";
    public static final Logger LOGGER = LogManager.getLogger("FutabaWare");
    public static CommandManager commandManager;
    public static FriendManager friendManager;
    public static ModuleManager moduleManager;
    public static PacketManager packetManager;
    public static TimerManager timerManager;
    public static ColorManager colorManager;
    public static HoleManager holeManager;
    public static InventoryManager inventoryManager;
    public static PotionManager potionManager;
    public static RotationManager rotationManager;
    public static PositionManager positionManager;
    public static SpeedManager speedManager;
    public static ReloadManager reloadManager;
    public static FileManager fileManager;
    public static ConfigManager configManager;
    public static ServerManager serverManager;
    public static EventManager eventManager;
    public static TextManager textManager;
    @Mod.Instance
    public static FutabaWare INSTANCE;
    private static boolean unloaded;

    static {
        unloaded = false;
    }

    public static void load() {
        LOGGER.info("\n\nLoading FutabaWare by or4acle");
        unloaded = false;
        if (reloadManager != null) {
            reloadManager.unload();
            reloadManager = null;
        }
        textManager = new TextManager();
        commandManager = new CommandManager();
        friendManager = new FriendManager();
        moduleManager = new ModuleManager();
        rotationManager = new RotationManager();
        timerManager = new TimerManager();
        packetManager = new PacketManager();
        eventManager = new EventManager();
        speedManager = new SpeedManager();
        potionManager = new PotionManager();
        inventoryManager = new InventoryManager();
        serverManager = new ServerManager();
        fileManager = new FileManager();
        colorManager = new ColorManager();
        positionManager = new PositionManager();
        configManager = new ConfigManager();
        holeManager = new HoleManager();
        LOGGER.info("Managers loaded.");
        moduleManager.init();
        LOGGER.info("Modules loaded.");
        configManager.init();
        eventManager.init();
        LOGGER.info("EventManager loaded.");
        textManager.init(true);
        moduleManager.onLoad();
        LOGGER.info("FutabaWare successfully loaded!\n");
        DiscordPresence.start();
    }

    public static void unload(boolean unload) {
        LOGGER.info("\n\nUnloading FutabaWare by or4acle");
        if (unload) {
            reloadManager = new ReloadManager();
            reloadManager.init(commandManager != null ? commandManager.getPrefix() : ".");
        }
        FutabaWare.onUnload();
        eventManager = null;
        friendManager = null;
        speedManager = null;
        holeManager = null;
        timerManager = null;
        positionManager = null;
        rotationManager = null;
        configManager = null;
        commandManager = null;
        colorManager = null;
        serverManager = null;
        fileManager = null;
        potionManager = null;
        inventoryManager = null;
        moduleManager = null;
        textManager = null;
        LOGGER.info("FutabaWare unloaded!\n");
        DiscordPresence.stop();
    }

    public static void reload() {
        FutabaWare.unload(false);
        FutabaWare.load();
    }

    public static void onUnload() {
        if (!unloaded) {
            // Verifica se os managers não são nulos antes de tentar desligá-los
            if (eventManager != null) {
                eventManager.onUnload();
            }
            if (moduleManager != null) {
                moduleManager.onUnload();
            }
            if (configManager != null && configManager.config != null) {
                configManager.saveConfig(FutabaWare.configManager.config.replaceFirst("FutabaWare/", ""));
            }
            if (moduleManager != null) {
                moduleManager.onUnloadPost();
            }
            unloaded = true;
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER.info("Futaba > IRL Women");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        Display.setTitle("FutabaWare v" + MODVER);
        FutabaWare.load();
        UpdateChecker.check(); // adiciona aqui
    }
}

