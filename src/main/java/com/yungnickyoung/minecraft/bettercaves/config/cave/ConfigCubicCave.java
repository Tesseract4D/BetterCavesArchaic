package com.yungnickyoung.minecraft.bettercaves.config.cave;

import com.yungnickyoung.minecraft.bettercaves.noise.FastNoise;

public class ConfigCubicCave {
    public int caveBottom = 1;
    public int caveTop = 80;
    public int caveSurfaceCutoff = 15;
    public float yCompression = 5.0f;
    public float xzCompression = 1.60f;
    public int cavePriority = 10;
    public Advanced advancedSettings = new Advanced();

    public class Advanced {
        public float noiseThreshold = .95f;
        public int fractalOctaves = 1;
        public float fractalGain = 0.3f;
        public float fractalFrequency = 0.03f;
        public int numGenerators = 2;
        public boolean yAdjust = true;
        public float yAdjustF1 = .9f;
        public float yAdjustF2 = .9f;
        public FastNoise.NoiseType noiseType = FastNoise.NoiseType.CubicFractal;
    }
}
