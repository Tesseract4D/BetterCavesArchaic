package cn.tesseract.mymodid;

import cn.tesseract.mycelium.asm.minecraft.HookLoader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

@Mod(modid = "mymodid", acceptedMinecraftVersions = "[1.7.10]", version = Tags.VERSION)
public class MyMod extends HookLoader {
    public static String greeting;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        syncConfig(e.getSuggestedConfigurationFile());
    }

    public static void syncConfig(File f) {
        Configuration cfg = new Configuration(f);
        greeting = cfg.getString("greeting", Configuration.CATEGORY_GENERAL, "Hello World", "How shall I greet?");
        if (cfg.hasChanged()) {
            cfg.save();
        }
    }

    @Override
    protected void registerHooks() {
        registerHookContainer(MinecraftHook.class.getName());
    }
}
