package cn.tesseract.bettercaves.config.io;

import cn.tesseract.bettercaves.BetterCaves;
import cn.tesseract.bettercaves.config.util.ConfigHolder;

public class ConfigLoader {
    public static ConfigHolder loadConfigFromFileForDimension(int dimensionID) {
        return new ConfigHolder(BetterCaves.config.instance);
    }
}
