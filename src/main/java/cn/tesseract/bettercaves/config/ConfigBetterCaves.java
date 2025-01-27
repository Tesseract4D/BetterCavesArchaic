package cn.tesseract.bettercaves.config;

import cn.tesseract.mycelium.config.ConfigJSON;

import java.io.File;

public class ConfigBetterCaves extends ConfigJSON<ConfigInstance> {
    public ConfigBetterCaves(File file, Class<ConfigInstance> clazz) {
        super(file, clazz);
    }

    public ConfigBetterCaves(String name, Class<ConfigInstance> clazz) {
        super(name, clazz);
    }
}
