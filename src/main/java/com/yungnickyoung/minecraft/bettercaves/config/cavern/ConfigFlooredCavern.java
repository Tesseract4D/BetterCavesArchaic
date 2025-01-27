package com.yungnickyoung.minecraft.bettercaves.config.cavern;

import com.yungnickyoung.minecraft.bettercaves.noise.FastNoise;

public class ConfigFlooredCavern {
    public int cavernBottom = 1;
    public int cavernTop = 35;
    public float yCompression = 1.3f;
    public float xzCompression = 0.7f;
    public int cavernPriority = 10;
    public Advanced advancedSettings = new Advanced();

    public class Advanced {
        public float noiseThreshold = .6f;
        public int fractalOctaves = 1;
        public float fractalGain = 0.3f;
        public float fractalFrequency = 0.028f;
        public int numGenerators = 2;
        public FastNoise.NoiseType noiseType = FastNoise.NoiseType.SimplexFractal;
    }
}
