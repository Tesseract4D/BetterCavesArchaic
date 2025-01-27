package com.yungnickyoung.minecraft.bettercaves.config.cave;

import com.yungnickyoung.minecraft.bettercaves.noise.FastNoise;

public class ConfigSimplexCave {
    public int caveBottom = 1;

    public int caveTop = 80;

    public int caveSurfaceCutoff = 15;

    public float yCompression = 2.2f;

    public float xzCompression = 0.9f;

    public int cavePriority = 5;

    public Advanced advancedSettings = new Advanced();

    public class Advanced {
        public float noiseThreshold = .82f;

        public int fractalOctaves = 1;

        public float fractalGain = 0.3f;

        public float fractalFrequency = 0.025f;

        public int numGenerators = 2;

        public boolean yAdjust = true;

        public float yAdjustF1 = .95f;

        public float yAdjustF2 = .5f;

        public FastNoise.NoiseType noiseType = FastNoise.NoiseType.SimplexFractal;
    }
}
