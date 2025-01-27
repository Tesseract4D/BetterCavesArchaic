package cn.tesseract.bettercaves.config;

import cn.tesseract.bettercaves.config.cave.*;
import cn.tesseract.bettercaves.config.cavern.ConfigCaverns;
import cn.tesseract.bettercaves.config.ravine.ConfigRavine;

public class ConfigUndergroundGen {
    public ConfigCaves caves = new ConfigCaves();
    public ConfigCaverns caverns = new ConfigCaverns();
    public ConfigWaterRegions waterRegions = new ConfigWaterRegions();
    public ConfigRavine ravines = new ConfigRavine();
    public ConfigMisc miscellaneous = new ConfigMisc();
}
