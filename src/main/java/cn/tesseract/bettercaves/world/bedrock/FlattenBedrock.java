package cn.tesseract.bettercaves.world.bedrock;


import cn.tesseract.mycelium.world.ChunkPrimer;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/**
 * Class containing static method for flattening bedrock.
 */
public class FlattenBedrock {
    private static final Block BEDROCK = Blocks.bedrock;

    /**
     * Flattens bedrock in a given chunk
     * @param primer The chunk's ChunkPrimer
     * @param bedrockLayerWidth Width of the bedrock layer, in blocks
     */
    public static void flattenBedrock(ChunkPrimer primer, int bedrockLayerWidth) {
        Block replacementBlock = Blocks.stone;

        // Replace normal bedrock at bottom of map with stone
        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++)
                for (int y = 1; y < 5; y++)
                    if (primer.getBlockState(x, y, z) == BEDROCK)
                        primer.setBlockState(x, y, z, replacementBlock);

        // Create bedrock layer(s) at bottom of map
        for (int x = 0; x < 16; x++)
            for (int z = 0; z < 16; z++)
                for (int y = 1; y < bedrockLayerWidth; y++)
                    primer.setBlockState(x, y, z, BEDROCK);
    }
}
