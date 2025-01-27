package cn.tesseract.bettercaves.world;

import cn.tesseract.mycelium.world.ChunkPrimer;
import cn.tesseract.bettercaves.config.BCSettings;
import cn.tesseract.bettercaves.config.io.ConfigLoader;
import cn.tesseract.bettercaves.config.util.ConfigHolder;
import cn.tesseract.bettercaves.util.BetterCavesUtils;
import cn.tesseract.bettercaves.world.bedrock.FlattenBedrock;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraftforge.event.terraingen.InitMapGenEvent;

/**
 * Class that overrides vanilla cave gen with Better Caves gen.
 * Combines multiple types of caves and caverns using different types of noise to create a
 * novel underground experience.
 */
public class MapGenBetterCaves extends MapGenCaves {
    // Vanilla cave gen if user sets config to use it
    private MapGenBase defaultCaveGen;

    // Region Controllers
    public WaterRegionController waterRegionController;
    private CaveCarverController caveCarverController;
    private CavernCarverController cavernCarverController;

    // Config holder for options specific to this carver
    public ConfigHolder config;

    public MapGenBetterCaves(InitMapGenEvent event) {
        this.defaultCaveGen = event.originalGen;
    }

    /**
     * Function for generating Better Caves in a single chunk. This overrides the vanilla cave generation, which is
     * ordinarily performed by the MapGenCaves class.
     * This function is called for every new chunk that is generated in a world.
     * @param worldIn The Minecraft world
     * @param chunkX The chunk's x-coordinate (on the chunk grid, not the block grid)
     * @param chunkZ The chunk's z-coordinate (on the chunk grid, not the block grid)
     * @param primer The chunk's ChunkPrimer
     */
    @Override
    public void func_151539_a(IChunkProvider provider, World worldIn, int chunkX, int chunkZ, Block[] blocks) {
        ChunkPrimer primer = new ChunkPrimer(blocks);
        // Only operate on whitelisted dimensions.
        if (!BetterCavesUtils.isDimensionWhitelisted(worldIn.provider.dimensionId)) {
            defaultCaveGen.func_151539_a(provider, worldIn, chunkX, chunkZ, blocks);
            return;
        }

        if (worldObj == null) { // First call - lazy initialization of all controllers and config
            this.initialize(worldIn);
        }

        // Flatten bedrock, if enabled
        if (config.flattenBedrock.get())
            FlattenBedrock.flattenBedrock(primer, config.bedrockWidth.get());

        // Determine surface altitudes in this chunk
        int[][] surfaceAltitudes = new int[16][16];
        for (int subX = 0; subX < 16 / BCSettings.SUB_CHUNK_SIZE; subX++) {
            for (int subZ = 0; subZ < 16 / BCSettings.SUB_CHUNK_SIZE; subZ++) {
                int startX = subX * BCSettings.SUB_CHUNK_SIZE;
                int startZ = subZ * BCSettings.SUB_CHUNK_SIZE;
                for (int offsetX = 0; offsetX < BCSettings.SUB_CHUNK_SIZE; offsetX++) {
                    for (int offsetZ = 0; offsetZ < BCSettings.SUB_CHUNK_SIZE; offsetZ++) {
                        int surfaceHeight;
                        if (config.overrideSurfaceDetection.get()) {
                            surfaceHeight = 1; // Don't waste time calculating surface height if it's going to be overridden anyway
                        } else {
                            surfaceHeight = BetterCavesUtils.getSurfaceAltitudeForColumn(primer, startX + offsetX, startZ + offsetZ);
                        }
                        surfaceAltitudes[startX + offsetX][startZ + offsetZ] = surfaceHeight;
                    }
                }
            }
        }

        // Determine liquid blocks for this chunk
        Block[][] liquidBlocks = waterRegionController.getLiquidBlocksForChunk(chunkX, chunkZ);

        // Carve chunk
        caveCarverController.carveChunk(primer, chunkX, chunkZ, surfaceAltitudes, liquidBlocks);
        cavernCarverController.carveChunk(primer, chunkX, chunkZ, surfaceAltitudes, liquidBlocks);
    }

    /**
     * Initialize Better Caves carvers and controllers for this dimension.
     * @param worldIn The minecraft world
     */
    private void initialize(World worldIn) {
        // Extract world information
        this.worldObj = worldIn;

        // Load config for this dimension
        this.config = ConfigLoader.loadConfigFromFileForDimension(worldObj.provider.dimensionId);

        // Initialize controllers
        this.waterRegionController = new WaterRegionController(worldObj, config);
        this.caveCarverController = new CaveCarverController(worldObj, config);
        this.cavernCarverController = new CavernCarverController(worldIn, config);
    }
}
