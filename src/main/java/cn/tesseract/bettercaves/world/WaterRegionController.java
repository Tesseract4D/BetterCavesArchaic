package cn.tesseract.bettercaves.world;

import cn.tesseract.mycelium.util.BlockPos;
import cn.tesseract.bettercaves.BetterCaves;
import cn.tesseract.bettercaves.config.util.ConfigHolder;
import cn.tesseract.bettercaves.enums.RegionSize;
import cn.tesseract.bettercaves.noise.FastNoise;
import cn.tesseract.bettercaves.noise.NoiseUtils;
import cn.tesseract.bettercaves.util.BetterCavesUtils;
import net.minecraft.block.Block;

import net.minecraft.init.Blocks;

import net.minecraft.world.World;

import java.util.Random;

public class WaterRegionController {
    private FastNoise waterRegionController;
    private long worldSeed;
    private int dimensionID;
    private String dimensionName;
    private Random rand;

    // Vars determined from config
    private Block lavaBlock;
    private Block waterBlock;
    private float waterRegionThreshold;

    // Constants
    private static final float SMOOTH_RANGE = .04f;
    private static final float SMOOTH_DELTA = .01f;

    public WaterRegionController(World world, ConfigHolder config) {
        this.worldSeed = world.getSeed();
        this.dimensionID = world.provider.dimensionId;
        this.dimensionName = world.provider.getDimensionName();
        this.rand = new Random();

        // Vars from config
        this.lavaBlock = getLavaBlockFromString(config.lavaBlock.get());
        this.waterBlock = getWaterBlockFromString(config.waterBlock.get());
        this.waterRegionThreshold = NoiseUtils.simplexNoiseOffsetByPercent(-1f, config.waterRegionSpawnChance.get() / 100f);

        // Water region controller
        float waterRegionSize = calcWaterRegionSize(config.waterRegionSize.get(), config.waterRegionCustomSize.get());
        this.waterRegionController = new FastNoise();
        this.waterRegionController.SetSeed((int)world.getSeed() + 444);
        this.waterRegionController.SetFrequency(waterRegionSize);
    }

    public Block[][] getLiquidBlocksForChunk(int chunkX, int chunkZ) {
        rand.setSeed(worldSeed ^ chunkX ^ chunkZ);
        Block[][] blocks = new Block[16][16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int realX = chunkX * 16 + x;
                int realZ = chunkZ * 16 + z;
                BlockPos pos = new BlockPos(realX, 1, realZ);
                blocks[x][z] = getLiquidBlockAtPos(rand, pos);
            }
        }
        return blocks;
    }

    private Block getLiquidBlockAtPos(Random rand, BlockPos blockPos) {
        Block liquidBlock = lavaBlock;
        if (waterRegionThreshold > -1f) { // Don't bother calculating noise if water regions are disabled
            float waterRegionNoise = waterRegionController.GetNoise(blockPos.x, blockPos.z);

            // If water region threshold check is passed, change liquid block to water
            float randOffset = rand.nextFloat() * SMOOTH_DELTA + SMOOTH_RANGE;
            if (waterRegionNoise < waterRegionThreshold - randOffset)
                liquidBlock = waterBlock;
            else if (waterRegionNoise < waterRegionThreshold + randOffset)
                liquidBlock = null;
        }
        return liquidBlock;
    }

    private Block getLavaBlockFromString(String lavaString) {
        Block lavaBlock;
        try {
            lavaBlock = Block.getBlockFromName(lavaString);
            BetterCaves.LOGGER.info("Using block '" + lavaString + "' as lava in cave generation for dimension " +
                    BetterCavesUtils.dimensionAsString(dimensionID, dimensionName) + " ...");
        } catch (Exception e) {
            BetterCaves.LOGGER.warn("Unable to use block '" + lavaString + "': " + e);
            BetterCaves.LOGGER.warn("Using vanilla lava instead...");
            lavaBlock = Blocks.lava;
        }
        if (lavaBlock == null) {
            BetterCaves.LOGGER.warn("Unable to use block '" + lavaString + "': null block returned.\n Using vanilla lava instead...");
            lavaBlock = Blocks.lava;
        }
        return lavaBlock;
    }

    private Block getWaterBlockFromString(String waterString) {
        Block waterBlock;
        try {
            waterBlock = Block.getBlockFromName(waterString);
            BetterCaves.LOGGER.info("Using block '" + waterString + "' as water in cave generation for dimension " +
                    BetterCavesUtils.dimensionAsString(dimensionID, dimensionName) + " ...");
        } catch (Exception e) {
            BetterCaves.LOGGER.warn("Unable to use block '" + waterString + "': " + e);
            BetterCaves.LOGGER.warn("Using vanilla water instead...");
            waterBlock = Blocks.water;
        }

        if (waterBlock == null) {
            BetterCaves.LOGGER.warn("Unable to use block '" + waterString + "': null block returned.\n Using vanilla water instead...");
            waterBlock = Blocks.water;
        }

        return waterBlock;
    }

    /**
     * @return frequency value for water region controller
     */
    private float calcWaterRegionSize(RegionSize waterRegionSize, float waterRegionCustomSize) {
        return switch (waterRegionSize) {
            case Small -> .008f;
            case Large -> .0028f;
            case ExtraLarge -> .001f;
            case Custom -> waterRegionCustomSize;
            default -> // Medium
                    .004f;
        };
    }
}
