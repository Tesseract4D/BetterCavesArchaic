package cn.tesseract.bettercaves.world.carver;

import cn.tesseract.mycelium.util.BlockPos;
import cn.tesseract.mycelium.world.ChunkPrimer;
import com.google.common.collect.ImmutableSet;
import cn.tesseract.bettercaves.util.BetterCavesUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

/**
 * Utility functions for Better Caves carvers.
 * This class may not be instantiated - all members are {@code public} and {@code static},
 * and as such may be accessed freely.
 */
public class CarverUtils {
    private CarverUtils() {
    } // Private constructor prevents instantiation

    /* Blocks used in this class */
    private static final Block AIR = Blocks.air;
    private static final Block SAND = Blocks.sand;
    private static final Block SANDSTONE = Blocks.sandstone;
    private static final Block REDSANDSTONE = Blocks.sandstone;
    private static final Block GRAVEL = Blocks.gravel;
    private static final Block ANDESITE = Blocks.stone;
    private static final ImmutableSet<Block> DEBUG_BLOCKS = ImmutableSet.of(Blocks.gold_block, Blocks.planks, Blocks.cobblestone, Blocks.redstone_block, Blocks.emerald_block, Blocks.brick_block);

    /**
     * Digs out the current block, default implementation removes stone, filler, and top block.
     * Sets the block to lavaBlockState if y is less then the liquidAltitude in the Config, and air other wise.
     * If setting to air, it also checks to see if we've broken the surface, and if so,
     * tries to make the floor the biome's top block.
     *
     * @param world the Minecraft world this block is in
     * @param primer the ChunkPrimer containing the block
     * @param blockPos The block's position
     * @param airBlockState the BlockState to use for air.
     * @param liquidBlockState the BlockState to use for liquids. May be null if in buffer zone between liquid regions
     * @param liquidAltitude altitude at and below which air is replaced with liquidBlockState
     */
    public static void digBlock(World world, ChunkPrimer primer, BlockPos blockPos, Block airBlockState, Block liquidBlockState, int liquidAltitude, boolean replaceGravel) {
        int localX = BetterCavesUtils.getLocal(blockPos.x);
        int localZ = BetterCavesUtils.getLocal(blockPos.z);
        int y = blockPos.y;

        Block blockState = primer.getBlockState(localX, y, localZ);
        Block blockStateAbove = primer.getBlockState(localX, y + 1, localZ);
        BiomeGenBase biome = world.getBiomeGenForCoords(blockPos.x, blockPos.z);
        Block biomeTopBlock = biome.topBlock;
        Block biomeFillerBlock = biome.fillerBlock;

        // Only continue if the block is replaceable
        if (canReplaceBlock(blockState, blockStateAbove) || blockState == biomeTopBlock || blockState == biomeFillerBlock) {
            if (airBlockState == AIR && y <= liquidAltitude) { // Replace any block below the liquid altitude with the liquid block passed in
                if (liquidBlockState != null) {
                    primer.setBlockState(localX, y, localZ, liquidBlockState);
                }
            } else {
                // Check for adjacent water blocks to avoid breaking into lakes or oceans
                if (airBlockState == AIR && isWaterAdjacent(primer, blockPos)) return;

                // Adjust block below if block removed is biome top block
                if (isTopBlock(world, primer, blockPos) && canReplaceBlock(primer.getBlockState(localX, y - 1, localZ), AIR))
                    primer.setBlockState(localX, y - 1, localZ, biome.topBlock);

                // Replace floating sand with sandstone
                if (blockStateAbove == SAND)
                    primer.setBlockState(localX, y + 1, localZ, SANDSTONE);
                //else if (blockStateAbove == SAND.withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND))
                else if (blockStateAbove == SAND)
                    primer.setBlockState(localX, y + 1, localZ, REDSANDSTONE);

                // Replace floating gravel with andesite, if enabled
                if (replaceGravel && blockStateAbove == GRAVEL)
                    primer.setBlockState(localX, y + 1, localZ, ANDESITE);

                // Replace this block with air, effectively "digging" it out
                primer.setBlockState(localX, y, localZ, airBlockState);
            }
        }
    }

    public static void digBlock(World world, ChunkPrimer primer, BlockPos blockPos, Block liquidBlockState, int liquidAltitude, boolean replaceGravel) {
        digBlock(world, primer, blockPos, Blocks.air, liquidBlockState, liquidAltitude, replaceGravel);
    }

    public static void digBlock(World world, ChunkPrimer primer, int x, int y, int z, Block liquidBlockState, int liquidAltitude, boolean replaceGravel) {
        digBlock(world, primer, new BlockPos(x, y, z), liquidBlockState, liquidAltitude, replaceGravel);
    }

    public static void digBlock(World world, ChunkPrimer primer, int x, int y, int z, Block airBlockState, Block liquidBlockState, int liquidAltitude, boolean replaceGravel) {
        digBlock(world, primer, new BlockPos(x, y, z), airBlockState, liquidBlockState, liquidAltitude, replaceGravel);
    }

    /**
     * DEBUG method for visualizing cave systems. Used as a replacement for the {@code digBlock} method if the
     * debugVisualizer config option is enabled.
     * @param primer Chunk containing the block
     * @param blockPos block position
     * @param blockState The blockState to set dug out blocks to
     */
    public static void debugDigBlock(ChunkPrimer primer, BlockPos blockPos, Block blockState, boolean digBlock) {
        int localX = BetterCavesUtils.getLocal(blockPos.x);
        int localZ = BetterCavesUtils.getLocal(blockPos.z);
        int y = blockPos.y;

        if (DEBUG_BLOCKS.contains(primer.getBlockState(localX, y, localZ))) return;

        if (digBlock)
            primer.setBlockState(localX, y, localZ, blockState);
        else
            primer.setBlockState(localX, y, localZ, Blocks.air);
    }

    public static void debugDigBlock(ChunkPrimer primer, int x, int y, int z, Block blockState, boolean digBlock) {
        debugDigBlock(primer, new BlockPos(x, y, z), blockState, digBlock);
    }

    /**
     * Determine if the block at the specified location is the designated top block for the biome.
     *
     * @param world the Minecraft world this block is in
     * @param primer the ChunkPrimer containing the block
     * @param blockPos The block's position
     * @return true if this block is the same type as the biome's designated top block
     */
    public static boolean isTopBlock(World world, ChunkPrimer primer, BlockPos blockPos) {
        int localX = BetterCavesUtils.getLocal(blockPos.x);
        int localZ = BetterCavesUtils.getLocal(blockPos.z);
        int y = blockPos.y;
        BiomeGenBase biome = world.getBiomeGenForCoords(blockPos.x,blockPos.z);
        Block blockState = primer.getBlockState(localX, y, localZ);
        return blockState == biome.topBlock;
    }

    /**
     * Determines if the Block of a given Block is suitable to be replaced during cave generation.
     * Basically returns true for most common worldgen blocks (e.g. stone, dirt, sand), false if the block is air.
     *
     * @param block the block's Block
     * @param blockAbove the Block of the block above this one
     * @return true if the block can be replaced
     */
    public static boolean canReplaceBlock(Block block, Block blockAbove) {

        // Avoid damaging trees
        if (block instanceof BlockLeaves
            || block instanceof BlockLog)
            return false;

        // Avoid digging out under trees
        if (blockAbove instanceof BlockLog)
            return false;

        // Don't mine bedrock
        if (block == Blocks.bedrock)
            return false;

        // Accept stone-like blocks added from other mods
        if (block.getMaterial() == Material.rock)
            return true;

        // Mine-able blocks
        if (block == Blocks.stone
            || block == Blocks.dirt
            || block == Blocks.grass
            || block == Blocks.hardened_clay
            || block == Blocks.stained_hardened_clay
            || block == Blocks.sandstone
            || block == Blocks.mycelium
            || block == Blocks.snow_layer)
            return true;

        // Only accept gravel and sand if water is not directly above it
        return (block == Blocks.sand || block == Blocks.gravel)
            && blockAbove.getMaterial() != Material.water;
    }

    private static boolean isWaterAdjacent(ChunkPrimer primer, BlockPos blockPos) {
        int localX = BetterCavesUtils.getLocal(blockPos.x);
        int localZ = BetterCavesUtils.getLocal(blockPos.z);
        int y = blockPos.y;

        return primer.getBlockState(localX, y + 1, localZ).getMaterial() == Material.water
            || localX < 15 && primer.getBlockState(localX + 1, y, localZ).getMaterial() == Material.water
            || localX > 0 && primer.getBlockState(localX - 1, y, localZ).getMaterial() == Material.water
            || localZ < 15 && primer.getBlockState(localX, y, localZ + 1).getMaterial() == Material.water
            || localZ > 0 && primer.getBlockState(localX, y, localZ - 1).getMaterial() == Material.water;
    }
}
