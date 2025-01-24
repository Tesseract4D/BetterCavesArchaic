package com.yungnickyoung.minecraft.bettercaves.world.ravine;

import cn.tesseract.mycelium.util.BlockPos;
import cn.tesseract.mycelium.world.ChunkPrimer;
import com.yungnickyoung.minecraft.bettercaves.config.io.ConfigLoader;
import com.yungnickyoung.minecraft.bettercaves.config.util.ConfigHolder;
import com.yungnickyoung.minecraft.bettercaves.util.BetterCavesUtils;
import com.yungnickyoung.minecraft.bettercaves.world.WaterRegionController;
import com.yungnickyoung.minecraft.bettercaves.world.carver.CarverUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.terraingen.InitMapGenEvent;

/**
 * Overrides MapGenRavine, tweaking it to work with config options.
 */
public class MapGenBetterRavine extends MapGenRavine {
    private ConfigHolder config;
    private WaterRegionController waterRegionController;
    private MapGenBase defaultRavineGen;

    Block[][] currChunkLiquidBlocks;
    int currChunkX, currChunkZ;

    public MapGenBetterRavine(InitMapGenEvent event) {
        this.defaultRavineGen = event.originalGen;
    }

    @Override
    public void func_151539_a(IChunkProvider provider, World worldIn, int x, int z, Block[] blocks) {
        // Only operate on whitelisted dimensions.
        if (!BetterCavesUtils.isDimensionWhitelisted(worldIn.provider.dimensionId)) {
            defaultRavineGen.func_151539_a(provider, worldIn, x, z, blocks);
            return;
        }

        if (worldObj == null) { // First call - lazy initialization
            this.initialize(worldIn);
        }

        super.func_151539_a(provider, worldIn, x, z, blocks);
    }

    protected void digBlock(Block[] data, int index, int x, int y, int z, int chunkX, int chunkZ, boolean foundTop) {
        ChunkPrimer primer = new ChunkPrimer(data);
        Block liquidBlockState;
        BlockPos pos = new BlockPos(x + chunkX * 16, y, z + chunkZ * 16);

        if (currChunkLiquidBlocks == null || chunkX != currChunkX || chunkZ != currChunkZ) {
            try {
                currChunkLiquidBlocks = waterRegionController.getLiquidBlocksForChunk(chunkX, chunkZ);
                liquidBlockState = currChunkLiquidBlocks[BetterCavesUtils.getLocal(x)][BetterCavesUtils.getLocal(z)];
                currChunkX = chunkX;
                currChunkZ = chunkZ;
            } catch (Exception e) {
                liquidBlockState = Blocks.lava;
            }
        } else {
            try {
                liquidBlockState = currChunkLiquidBlocks[BetterCavesUtils.getLocal(x)][BetterCavesUtils.getLocal(z)];
            } catch (Exception e) {
                liquidBlockState = Blocks.lava;
            }
        }

        // Don't dig boundaries between flooded and unflooded openings.
        boolean flooded = config.enableFloodedRavines.get() && BiomeDictionary.isBiomeOfType(worldObj.getBiomeGenForCoords(pos.x, pos.z), BiomeDictionary.Type.OCEAN) && y < 63/*world.getSeaLevel()*/;
        if (flooded) {
            float smoothAmpFactor = BetterCavesUtils.biomeDistanceFactor(worldObj, pos, 2, b -> !BiomeDictionary.isBiomeOfType(b, BiomeDictionary.Type.OCEAN));
            if (smoothAmpFactor <= .25f) { // Wall between flooded and normal caves.
                return;
            }
        }

        Block airBlockState = flooded ? Blocks.water : Blocks.air;
        CarverUtils.digBlock(worldObj, primer, pos, airBlockState, liquidBlockState, config.liquidAltitude.get(), config.replaceFloatingGravel.get());

    }

    // Disable built-in water block checks.
    // Without this, ravines in water regions will be sliced up.
    protected boolean isOceanBlock(Block[] data, int index, int x, int y, int z, int chunkX, int chunkZ)
    {
        return false;
    }

    private void initialize(World worldIn) {
        this.worldObj = worldIn;
        int dimensionID = worldIn.provider.dimensionId;
        this.config = ConfigLoader.loadConfigFromFileForDimension(dimensionID);
        this.waterRegionController = new WaterRegionController(worldObj, config);
    }
}
