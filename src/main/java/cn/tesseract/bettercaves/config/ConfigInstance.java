package cn.tesseract.bettercaves.config;

public class ConfigInstance {
    public ConfigUndergroundGen caveSettings = new ConfigUndergroundGen();
    public ConfigBedrockGen bedrockSettings = new ConfigBedrockGen();
    public ConfigDebug debugSettings = new ConfigDebug();
    public int[] whitelistedDimensionIDs = {0};
    public boolean enableGlobalWhitelist = false;
}
