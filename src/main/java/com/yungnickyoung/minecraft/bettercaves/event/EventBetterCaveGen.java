package com.yungnickyoung.minecraft.bettercaves.event;

import com.yungnickyoung.minecraft.bettercaves.world.MapGenBetterCaves;
import com.yungnickyoung.minecraft.bettercaves.world.mineshaft.MapGenBetterMineshaft;
import com.yungnickyoung.minecraft.bettercaves.world.ravine.MapGenBetterRavine;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Replaces vanilla cave generation with Better Caves generation.
 * Should be registered to the {@code TERRAIN_GEN_BUS}.
 */
public class EventBetterCaveGen {
    /**
     * Replaces cave gen and mineshaft gen
     *
     * @param event Map generation event
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public void onInitMapGenEvent(InitMapGenEvent event) {
        // Replace cave gen with Better Caves
        if (
            (event.type == InitMapGenEvent.EventType.CAVE || event.type == InitMapGenEvent.EventType.NETHER_CAVE)
                && !event.originalGen.getClass().equals(MapGenBetterCaves.class)
        ) {
            event.newGen=new MapGenBetterCaves(event);
        }
        // Replace mineshaft gen with Better Caves
        else if (
            event.type == InitMapGenEvent.EventType.MINESHAFT
                && event.originalGen == event.newGen // only modify vanilla gen to allow other mods to modify mineshafts
        ) {
            event.newGen=new MapGenBetterMineshaft(event);
        }
        // Replace ravine gen with Better Caves
        else if (
            event.type == InitMapGenEvent.EventType.RAVINE
                && event.originalGen == event.newGen // only modify vanilla gen to allow other mods to modify ravines
        ) {
            event.newGen=new MapGenBetterRavine(event);
        }
    }
}
