package cn.tesseract.bettercaves.world.mineshaft;

import cn.tesseract.bettercaves.config.io.ConfigLoader;
import cn.tesseract.bettercaves.config.util.ConfigHolder;
import cn.tesseract.bettercaves.util.BetterCavesUtils;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.*;
import net.minecraftforge.event.terraingen.InitMapGenEvent;

import java.util.Random;

/**
 * Overrides mineshaft generation to remove pieces of mineshafts within 4 blocks of the liquid altitude.
 * This prevents mineshafts from being at risk of burning from lava at the liquid altitude.
 *
 * Note -- this was made before my other mod, YUNG's Better Mineshafts, and is not related to it.
 */
public class MapGenBetterMineshaft extends MapGenMineshaft {
    private MapGenBase defaultMineshaftGen;
    private int liquidAltitude;

    public MapGenBetterMineshaft(InitMapGenEvent event) {
        this.defaultMineshaftGen = event.originalGen;
    }

    @Override
    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        MapGenStructureIO.registerStructure(StructureBetterMineshaftStart.class, "Mineshaft");
        BiomeGenBase biome = this.worldObj.getBiomeGenForCoords((chunkX << 4) + 8, (chunkZ << 4) + 8);
        return new StructureBetterMineshaftStart(this.worldObj, this.rand, chunkX, chunkZ);
    }

    @Override
    public void func_151539_a(IChunkProvider provider, World worldIn, int x, int z, Block[] blocks) {
        // Only operate on whitelisted dimensions.
        if (!BetterCavesUtils.isDimensionWhitelisted(worldIn.provider.dimensionId)) {
            defaultMineshaftGen.func_151539_a(provider, worldIn, x, z, blocks);
            return;
        }

        if (worldObj == null) { // First call - lazy initialization
            this.initialize(worldIn);
        }

        super.func_151539_a(provider, worldIn, x, z, blocks);
    }


    private void initialize(World worldIn) {
        this.worldObj = worldIn;
        // Load config for this dimension
        ConfigHolder config = ConfigLoader.loadConfigFromFileForDimension(worldIn.provider.dimensionId);
        this.liquidAltitude = config.liquidAltitude.get();
    }

    private class StructureBetterMineshaftStart extends StructureMineshaftStart {
        public StructureBetterMineshaftStart(World worldIn, Random rand, int chunkX, int chunkZ) {
            super(worldIn, rand, chunkX, chunkZ);
        }

        @Override
        public void generateStructure(World worldIn, Random rand, StructureBoundingBox structurebb) {
            components.removeIf(component ->
                component.getBoundingBox().minY < liquidAltitude + 5 ||
                    (component.getBoundingBox().intersectsWith(structurebb) && !component.addComponentParts(worldIn, rand, structurebb))
            );
        }
    }
}
