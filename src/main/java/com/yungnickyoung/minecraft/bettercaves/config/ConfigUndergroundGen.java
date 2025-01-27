package com.yungnickyoung.minecraft.bettercaves.config;

import com.yungnickyoung.minecraft.bettercaves.config.cave.*;
import com.yungnickyoung.minecraft.bettercaves.config.cavern.ConfigCaverns;
import com.yungnickyoung.minecraft.bettercaves.config.ravine.ConfigRavine;

public class ConfigUndergroundGen {
    public ConfigCaves caves = new ConfigCaves();
    public ConfigCaverns caverns = new ConfigCaverns();
    public ConfigWaterRegions waterRegions = new ConfigWaterRegions();
    public ConfigRavine ravines = new ConfigRavine();
    public ConfigMisc miscellaneous = new ConfigMisc();
}
