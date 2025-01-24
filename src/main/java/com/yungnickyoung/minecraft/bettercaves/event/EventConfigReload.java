package com.yungnickyoung.minecraft.bettercaves.event;

import com.yungnickyoung.minecraft.bettercaves.config.BCSettings;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Synchronize changes to the config.
 * Should be registered to the {@code EVENT_BUS} on the client side.
 */
public class EventConfigReload {
    /**
     * Keeps Better Caves config settings synchronized
     */
    @SubscribeEvent
    public void onConfigReload(ConfigChangedEvent.OnConfigChangedEvent event) {
        // Only mess with config syncing if it is this mod being changed
        if (BCSettings.MOD_ID.equals(event.modID))
            ConfigManager.sync(BCSettings.MOD_ID, Config.Type.INSTANCE);
    }


}
