package cn.tesseract.bettercaves.config.cavern;


import cn.tesseract.bettercaves.enums.RegionSize;

public class ConfigCaverns {
    public ConfigLiquidCavern liquidCavern = new ConfigLiquidCavern();

    public ConfigFlooredCavern flooredCavern = new ConfigFlooredCavern();
    public float cavernSpawnChance = 25;
    public RegionSize cavernRegionSize = RegionSize.Small;
    public float customRegionSize = 0.01f;
}
