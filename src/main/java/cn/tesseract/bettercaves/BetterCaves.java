package cn.tesseract.bettercaves;

import cn.tesseract.bettercaves.config.BCSettings;
import cn.tesseract.bettercaves.config.ConfigBetterCaves;
import cn.tesseract.bettercaves.config.ConfigInstance;
import cn.tesseract.bettercaves.event.EventBetterCaveGen;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = BCSettings.MOD_ID, name = BCSettings.NAME, version = Tags.VERSION, dependencies = "required-after:mycelium@[2.3,)", acceptableRemoteVersions = "*")
public class BetterCaves {
    public static final Logger LOGGER = LogManager.getLogger(BCSettings.MOD_ID);
    public static ConfigBetterCaves config = new ConfigBetterCaves("bettercaves", ConfigInstance.class);
    public static File customConfigDir = new File(Loader.instance().getConfigDir(), "bettercaves");

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        /*try {
            String filePath = customConfigDir.getCanonicalPath();
            if (customConfigDir.mkdir())
                BetterCaves.LOGGER.info("Creating directory for dimension-specific Better Caves configs at " + filePath);
        } catch (IOException e) {
            BetterCaves.LOGGER.warn("ERROR creating Better Caves config directory.");
        }*/

        if (config.file.exists())
            config.read();
        else {
            config.instance = new ConfigInstance();
            config.save();
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.TERRAIN_GEN_BUS.register(new EventBetterCaveGen());
    }
}
