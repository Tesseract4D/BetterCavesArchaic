package com.yungnickyoung.minecraft.bettercaves.config.cave;

import com.yungnickyoung.minecraft.bettercaves.enums.RegionSize;

public class ConfigCaves {
    public ConfigCubicCave cubicCave = new ConfigCubicCave();

    public ConfigSimplexCave simplexCave = new ConfigSimplexCave();

    public ConfigSurfaceCave surfaceCave = new ConfigSurfaceCave();

    public ConfigVanillaCave vanillaCave = new ConfigVanillaCave();

    public float caveSpawnChance = 100;

    public RegionSize caveRegionSize = RegionSize.Small;

    public float customRegionSize = 0.008f;
}
