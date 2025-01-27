package cn.tesseract.bettercaves.world;

import cn.tesseract.mycelium.util.BlockPos;
import cn.tesseract.mycelium.world.ChunkPrimer;
import cn.tesseract.bettercaves.BetterCaves;
import cn.tesseract.bettercaves.config.util.ConfigHolder;
import cn.tesseract.bettercaves.config.BCSettings;
import cn.tesseract.bettercaves.enums.CaveType;
import cn.tesseract.bettercaves.enums.RegionSize;
import cn.tesseract.bettercaves.noise.FastNoise;
import cn.tesseract.bettercaves.noise.NoiseColumn;
import cn.tesseract.bettercaves.world.carver.CarverNoiseRange;
import cn.tesseract.bettercaves.world.carver.ICarver;
import cn.tesseract.bettercaves.world.carver.cave.CaveCarver;
import cn.tesseract.bettercaves.world.carver.cave.CaveCarverBuilder;
import cn.tesseract.bettercaves.world.carver.vanilla.VanillaCaveCarver;
import cn.tesseract.bettercaves.world.carver.vanilla.VanillaCaveCarverBuilder;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;

import java.util.ArrayList;
import java.util.List;

public class CaveCarverController {
    private World world;
    private VanillaCaveCarver surfaceCaveCarver; // only used if surface caves enabled
    private FastNoise caveRegionController;
    private List<CarverNoiseRange> noiseRanges = new ArrayList<>();

    // Vars from config
    private boolean isDebugViewEnabled;
    private boolean isOverrideSurfaceDetectionEnabled;
    private boolean isSurfaceCavesEnabled;
    private boolean isFloodedUndergroundEnabled;

    public CaveCarverController(World worldIn, ConfigHolder config) {
        this.world = worldIn;
        this.isDebugViewEnabled = config.debugVisualizer.get();
        this.isOverrideSurfaceDetectionEnabled = config.overrideSurfaceDetection.get();
        this.isSurfaceCavesEnabled = config.isSurfaceCavesEnabled.get();
        this.isFloodedUndergroundEnabled = config.enableFloodedUnderground.get();
        this.surfaceCaveCarver = new VanillaCaveCarverBuilder()
            .bottomY(config.surfaceCaveBottom.get())
            .topY(config.surfaceCaveTop.get())
            .density(config.surfaceCaveDensity.get())
            .liquidAltitude(config.liquidAltitude.get())
            .replaceGravel(config.replaceFloatingGravel.get())
            .floodedUnderground(config.enableFloodedUnderground.get())
            .debugVisualizerEnabled(config.debugVisualizer.get())
            .debugVisualizerBlock(Blocks.emerald_block)
            .build();

        // Configure cave region controller, which determines what type of cave should be
        // carved in any given region
        float caveRegionSize = calcCaveRegionSize(config.caveRegionSize.get(), config.caveRegionCustomSize.get());
        this.caveRegionController = new FastNoise();
        this.caveRegionController.SetSeed((int)worldIn.getSeed() + 222);
        this.caveRegionController.SetFrequency(caveRegionSize);
        this.caveRegionController.SetNoiseType(FastNoise.NoiseType.Cellular);
        this.caveRegionController.SetCellularDistanceFunction(FastNoise.CellularDistanceFunction.Natural);

        // Initialize all carvers using config options
        List<ICarver> carvers = new ArrayList<>();
        // Type 1 caves
        carvers.add(new CaveCarverBuilder(worldIn)
            .ofTypeFromConfig(CaveType.CUBIC, config)
            .debugVisualizerBlock(Blocks.planks)
            .build()
        );
        // Type 2 caves
        carvers.add(new CaveCarverBuilder(worldIn)
            .ofTypeFromConfig(CaveType.SIMPLEX, config)
            .debugVisualizerBlock(Blocks.cobblestone)
            .build()
        );
        // Vanilla caves
        carvers.add(new VanillaCaveCarverBuilder()
            .bottomY(config.vanillaCaveBottom.get())
            .topY(config.vanillaCaveTop.get())
            .density(config.vanillaCaveDensity.get())
            .priority(config.vanillaCavePriority.get())
            .liquidAltitude(config.liquidAltitude.get())
            .replaceGravel(config.replaceFloatingGravel.get())
            .floodedUnderground(config.enableFloodedUnderground.get())
            .debugVisualizerEnabled(config.debugVisualizer.get())
            .debugVisualizerBlock(Blocks.brick_block)
            .build());

        // Remove carvers with no priority
        carvers.removeIf(carver -> carver.getPriority() == 0);

        // Initialize vars for calculating controller noise thresholds
        float maxPossibleNoiseThreshold = config.caveSpawnChance.get() * .01f * 2 - 1;
        int totalPriority = carvers.stream().map(ICarver::getPriority).reduce(0, Integer::sum);
        float totalRangeLength = maxPossibleNoiseThreshold - -1f;
        float currNoise = -1f;

        BetterCaves.LOGGER.debug("CAVE INFORMATION");
        BetterCaves.LOGGER.debug("--> MAX POSSIBLE THRESHOLD: " + maxPossibleNoiseThreshold);
        BetterCaves.LOGGER.debug("--> TOTAL PRIORITY: " + totalPriority);
        BetterCaves.LOGGER.debug("--> TOTAL RANGE LENGTH: " + totalRangeLength);

        for (ICarver carver : carvers) {
            BetterCaves.LOGGER.debug("--> CARVER");
            float noiseRangeLength = (float)carver.getPriority() / totalPriority * totalRangeLength;
            float rangeTop = currNoise + noiseRangeLength;
            CarverNoiseRange range = new CarverNoiseRange(currNoise, rangeTop, carver);
            currNoise = rangeTop;
            noiseRanges.add(range);

            BetterCaves.LOGGER.debug("    --> RANGE FOUND: " + range);
        }
    }

    public void carveChunk(ChunkPrimer primer, int chunkX, int chunkZ, int[][] surfaceAltitudes, Block[][] liquidBlocks) {
        // Prevent unnecessary computation if caves are disabled
        if (noiseRanges.size() == 0 && !isSurfaceCavesEnabled) {
            return;
        }

        boolean flooded;

        // Flag to keep track of whether or not we've already carved vanilla caves for this chunk, since
        // vanilla caves operate on a chunk-by-chunk basis rather than by column
        boolean shouldCarveVanillaCaves = false;

        // Since vanilla caves carve by chunk and not by column, we store an array
        // indicating which x-z coordinates are valid to be carved in
        boolean[][] vanillaCarvingMask = new boolean[16][16];

        // Break into subchunks for noise interpolation
        for (int subX = 0; subX < 16 / BCSettings.SUB_CHUNK_SIZE; subX++) {
            for (int subZ = 0; subZ < 16 / BCSettings.SUB_CHUNK_SIZE; subZ++) {
                int startX = subX * BCSettings.SUB_CHUNK_SIZE;
                int startZ = subZ * BCSettings.SUB_CHUNK_SIZE;
                int endX = startX + BCSettings.SUB_CHUNK_SIZE - 1;
                int endZ = startZ + BCSettings.SUB_CHUNK_SIZE - 1;
                BlockPos startPos = new BlockPos(chunkX * 16 + startX, 1, chunkZ * 16 + startZ);
                BlockPos endPos = new BlockPos(chunkX * 16 + endX, 1, chunkZ * 16 + endZ);

                noiseRanges.forEach(range -> range.setNoiseCube(null));

                // Get max height in subchunk. This is needed for calculating the noise cube
                int maxHeight = 0;
                if (!isOverrideSurfaceDetectionEnabled) { // Only necessary if we aren't overriding surface detection
                    for (int x = startX; x < endX; x++) {
                        for (int z = startZ; z < endZ; z++) {
                            maxHeight = Math.max(maxHeight, surfaceAltitudes[x][z]);
                        }
                    }
                    for (CarverNoiseRange range : noiseRanges) {
                        maxHeight = Math.max(maxHeight, range.getCarver().getTopY());
                    }
                }

                // Offset within subchunk
                for (int offsetX = 0; offsetX < BCSettings.SUB_CHUNK_SIZE; offsetX++) {
                    for (int offsetZ = 0; offsetZ < BCSettings.SUB_CHUNK_SIZE; offsetZ++) {
                        int localX = startX + offsetX;
                        int localZ = startZ + offsetZ;
                        BlockPos colPos = new BlockPos(chunkX * 16 + localX, 1, chunkZ * 16 + localZ);
                        flooded = isFloodedUndergroundEnabled && !isDebugViewEnabled && BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(colPos.x,colPos.z), BiomeDictionary.Type.OCEAN);
                        if (flooded) {
                            if (
                                !BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(colPos.x + 1, colPos.z), BiomeDictionary.Type.OCEAN) ||
                                    !BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(colPos.x - 1, colPos.z), BiomeDictionary.Type.OCEAN) ||
                                    !BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(colPos.x, colPos.z + 1), BiomeDictionary.Type.OCEAN) ||
                                    !BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoordsBody(colPos.x, colPos.z - 1), BiomeDictionary.Type.OCEAN)
                            ) {
                                continue;
                            }
                        }

                        int surfaceAltitude = surfaceAltitudes[localX][localZ];
                        Block liquidBlock = liquidBlocks[localX][localZ];

                        // Get noise values used to determine cave region
                        float caveRegionNoise = caveRegionController.GetNoise(colPos.x, colPos.z);

                        // Carve cave using matching carver
                        for (CarverNoiseRange range : noiseRanges) {
                            if (!range.contains(caveRegionNoise)) {
                                continue;
                            }
                            if (range.getCarver() instanceof CaveCarver) {
                                CaveCarver carver = (CaveCarver) range.getCarver();
                                int bottomY = carver.getBottomY();
                                int topY = Math.min(surfaceAltitude, carver.getTopY());
                                if (isOverrideSurfaceDetectionEnabled) {
                                    topY = carver.getTopY();
                                    maxHeight = carver.getTopY();
                                }
                                if (isDebugViewEnabled) {
                                    topY = 128;
                                    maxHeight = 128;
                                }
                                if (range.getNoiseCube() == null) {
                                    range.setNoiseCube(carver.getNoiseGen().interpolateNoiseCube(startPos, endPos, bottomY, maxHeight));
                                }
                                NoiseColumn noiseColumn = range.getNoiseCube().get(offsetX).get(offsetZ);
                                carver.carveColumn(primer, colPos, topY, noiseColumn, liquidBlock, flooded);
                                break;
                            }
                            else if (range.getCarver() instanceof VanillaCaveCarver) {
                                vanillaCarvingMask[localX][localZ] = true;
                                shouldCarveVanillaCaves = true;
                            }
                        }
                    }
                }
            }
        }
        if (shouldCarveVanillaCaves) {
            VanillaCaveCarver carver = null;
            for (CarverNoiseRange range : noiseRanges) {
                if (range.getCarver() instanceof VanillaCaveCarver) {
                    carver = (VanillaCaveCarver) range.getCarver();
                    break;
                }
            }
            if (carver != null) {
                carver.generate(world, chunkX, chunkZ, primer, true, liquidBlocks, vanillaCarvingMask);
            }
        }
        // Generate surface caves if enabled
        if (isSurfaceCavesEnabled) {
            surfaceCaveCarver.generate(world, chunkX, chunkZ, primer, false, liquidBlocks);
        }
    }

    /**
     * @return frequency value for cave region controller
     */
    private float calcCaveRegionSize(RegionSize caveRegionSize, float caveRegionCustomSize) {
        switch (caveRegionSize) {
            case Small:
                return .008f;
            case Large:
                return .0032f;
            case ExtraLarge:
                return .001f;
            case Custom:
                return caveRegionCustomSize;
            default: // Medium
                return .005f;
        }
    }
}
