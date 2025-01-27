package cn.tesseract.bettercaves.world.carver.vanilla;

import cn.tesseract.mycelium.util.BlockPos;
import cn.tesseract.mycelium.world.ChunkPrimer;
import cn.tesseract.bettercaves.BetterCaves;
import cn.tesseract.bettercaves.util.BetterCavesUtils;
import cn.tesseract.bettercaves.world.carver.CarverUtils;
import cn.tesseract.bettercaves.world.carver.ICarver;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Random;

/**
 * Generates vanilla caves, with the ability to modify some parameters.
 * Most of the code is directly taken from vanilla caves, with improved variable names
 * and added comments to help clarify what is going on.
 */
public class VanillaCaveCarver extends MapGenCaves implements ICarver {
    private int
        bottomY,
        topY,
        density,
        priority,
        liquidAltitude;
    private Block debugBlock;
    private boolean
        isDebugVisualizerEnabled,
        isReplaceGravel,
        isFloodedUndergroundEnabled;

    public VanillaCaveCarver(final VanillaCaveCarverBuilder builder) {
        this.bottomY = builder.getBottomY();
        this.topY = builder.getTopY();
        this.density = builder.getDensity();
        this.priority = builder.getPriority();
        this.liquidAltitude = builder.getLiquidAltitude();
        this.debugBlock = builder.getDebugBlock();
        this.isDebugVisualizerEnabled = builder.isDebugVisualizerEnabled();
        this.isReplaceGravel = builder.isReplaceGravel();
        this.isFloodedUndergroundEnabled = builder.isFloodedUndergroundEnabled();
        if (bottomY > topY) {
            BetterCaves.LOGGER.warn("Warning: Min altitude for vanilla caves should not be greater than max altitude.");
            BetterCaves.LOGGER.warn("Using default values...");
            this.bottomY = 40;
            this.topY = 128;
        }
    }

    /**
     * Calls recursiveGenerate() on all chunks within a certain square range (default 8) of this chunk.
     */
    public void generate(World worldIn, int chunkX, int chunkZ, ChunkPrimer primer, boolean addRooms, Block[][] liquidBlocks, boolean[][] carvingMask) {
        int chunkRadius = this.range;
        this.worldObj = worldIn;
        this.rand.setSeed(worldIn.getSeed());
        long j = this.rand.nextLong();
        long k = this.rand.nextLong();
        for (int currChunkX = chunkX - chunkRadius; currChunkX <= chunkX + chunkRadius; ++currChunkX) {
            for (int currChunkZ = chunkZ - chunkRadius; currChunkZ <= chunkZ + chunkRadius; ++currChunkZ) {
                long j1 = (long) currChunkX * j;
                long k1 = (long) currChunkZ * k;
                this.rand.setSeed(j1 ^ k1 ^ worldIn.getSeed());
                this.recursiveGenerate(currChunkX, currChunkZ, chunkX, chunkZ, primer, addRooms, liquidBlocks, carvingMask);
            }
        }
    }

    public void generate(World worldIn, int x, int z, ChunkPrimer primer, boolean addRooms, Block[][] liquidBlocks) {
        boolean[][] carvingMask = new boolean[16][16];
        for (boolean[] row : carvingMask)
            Arrays.fill(row, true);
        generate(worldIn, x, z, primer, addRooms, liquidBlocks, carvingMask);
    }

    /**
     * Calls addTunnel and addRoom (wrapper for addTunnel) for this chunk.
     * Note that each call to this function (and subsequently addTunnel) will be done with the same rand seed.
     * This means that when a chunk is checked multiple times by different neighbor chunks, each time it will be processed
     * the same way, ensuring the tunnels are always consistent and connecting.
     */
    protected void recursiveGenerate(int chunkX, int chunkZ, int originalChunkX, int originalChunkZ, @Nonnull ChunkPrimer primer, boolean addRooms, Block[][] liquidBlocks, boolean[][] carvingMask) {
        int numAttempts = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(15) + 1) + 1);

        if (this.rand.nextInt(100) > this.density) {
            numAttempts = 0;
        }

        for (int i = 0; i < numAttempts; ++i) {
            double caveStartX = chunkX * 16 + this.rand.nextInt(16);
            double caveStartY = this.rand.nextInt(this.topY - this.bottomY) + this.bottomY;
            double caveStartZ = chunkZ * 16 + this.rand.nextInt(16);

            int numAddTunnelCalls = 1;

            if (addRooms && this.rand.nextInt(4) == 0) {
                this.addRoom(this.rand.nextLong(), originalChunkX, originalChunkZ, primer, caveStartX, caveStartY, caveStartZ, liquidBlocks, carvingMask);
                numAddTunnelCalls += this.rand.nextInt(4);
            }

            for (int j = 0; j < numAddTunnelCalls; ++j) {
                float yaw = this.rand.nextFloat() * ((float) Math.PI * 2F);
                float pitch = (this.rand.nextFloat() - 0.5F) * 2.0F / 8.0F;
                float width = this.rand.nextFloat() * 2.0F + this.rand.nextFloat();

                // Chance of wider caves.
                // Although not actually related to adding rooms, I perform an addRoom check here
                // to avoid the chance of really large caves when generating surface caves.
                if (addRooms && this.rand.nextInt(10) == 0) {
                    width *= this.rand.nextFloat() * this.rand.nextFloat() * 3.0F + 1.0F;
                }

                this.addTunnel(this.rand.nextLong(), originalChunkX, originalChunkZ, primer, caveStartX, caveStartY, caveStartZ, width, yaw, pitch, 0, 0, 1.0D, liquidBlocks, carvingMask);
            }
        }
    }

    public int getPriority() {
        return this.priority;
    }

    @Override
    public int getTopY() {
        return this.topY;
    }

    protected void addRoom(long seed, int originChunkX, int originChunkZ, ChunkPrimer primer, double caveStartX, double caveStartY, double caveStartZ, Block[][] liquidBlocks, boolean[][] carvingMask) {
        this.addTunnel(seed, originChunkX, originChunkZ, primer, caveStartX, caveStartY, caveStartZ, 1.0F + this.rand.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D, liquidBlocks, carvingMask);
    }

    protected void addTunnel(long seed, int originChunkX, int originChunkZ, ChunkPrimer primer, double caveStartX, double caveStartY, double caveStartZ, float width, float yaw, float pitch, int startCounter, int endCounter, double heightModifier, Block[][] liquidBlocks, boolean[][] carvingMask) {
        Block liquidBlock;
        Random random = new Random(seed);

        // Center block of the origin chunk
        double originBlockX = (originChunkX * 16 + 8);
        double originBlockZ = (originChunkZ * 16 + 8);

        // Variables to slightly change the yaw/pitch for each iteration in the while loop below.
        float yawModifier = 0.0F;
        float pitchModifier = 0.0F;

        // Raw calls to addTunnel from recursiveGenerate give startCounter and endCounter a value of 0.
        // Calls from addRoom make them both -1.

        // This appears to be called regardless of where addTunnel was called from.
        if (endCounter <= 0) {
            int i = this.range * 16 - 16;
            endCounter = i - random.nextInt(i / 4);
        }

        // Whether or not this function call was made from addRoom.
        boolean comesFromRoom = false;

        // Only called if the function call came from addRoom.
        // If this call came from addRoom, startCounter is set to halfway to endCounter.
        // If this is a raw call from recursiveGenerate, startCounter will be zero.
        if (startCounter == -1) {
            startCounter = endCounter / 2;
            comesFromRoom = true;
        }

        int randomCounterValue = random.nextInt(endCounter / 2) + endCounter / 4;

        // Loops one block at a time to the endCounter (about 6-7 chunks away on average).
        // startCounter starts at either zero or endCounter / 2.
        while (startCounter < endCounter) {
            // Appears to change how wide caves are. Value will be between 1.5 and 1.5 + width.
            // Note that caves will become wider toward the middle, and close off on the ends.
            double xzOffset = 1.5D + (double) (MathHelper.sin((float) startCounter * (float) Math.PI / (float) endCounter) * width);

            // Appears to just be a linear modifier for the cave height
            double yOffset = xzOffset * heightModifier;

            float pitchXZ = MathHelper.cos(pitch);
            float pitchY = MathHelper.sin(pitch);
            caveStartX += MathHelper.cos(yaw) * pitchXZ;
            caveStartY += pitchY;
            caveStartZ += MathHelper.sin(yaw) * pitchXZ;

            boolean flag = random.nextInt(6) == 0;
            if (flag) {
                pitch = pitch * 0.92F;
            } else {
                pitch = pitch * 0.7F;
            }

            pitch = pitch + pitchModifier * 0.1F;
            yaw += yawModifier * 0.1F;

            pitchModifier = pitchModifier * 0.9F;
            yawModifier = yawModifier * 0.75F;

            pitchModifier = pitchModifier + (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            yawModifier = yawModifier + (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (!comesFromRoom && startCounter == randomCounterValue && width > 1.0F && endCounter > 0) {
                this.addTunnel(random.nextLong(), originChunkX, originChunkZ, primer, caveStartX, caveStartY, caveStartZ, random.nextFloat() * 0.5F + 0.5F, yaw - ((float) Math.PI / 2F), pitch / 3.0F, startCounter, endCounter, 1.0D, liquidBlocks, carvingMask);
                this.addTunnel(random.nextLong(), originChunkX, originChunkZ, primer, caveStartX, caveStartY, caveStartZ, random.nextFloat() * 0.5F + 0.5F, yaw + ((float) Math.PI / 2F), pitch / 3.0F, startCounter, endCounter, 1.0D, liquidBlocks, carvingMask);
                return;
            }

            if (comesFromRoom || random.nextInt(4) != 0) {
                double caveStartXOffsetFromCenter = caveStartX - originBlockX; // Number of blocks from current caveStartX to center of origin chunk
                double caveStartZOffsetFromCenter = caveStartZ - originBlockZ; // Number of blocks from current caveStartZ to center of origin chunk
                double distanceToEnd = endCounter - startCounter;
                double d7 = width + 2.0F + 16.0F;

                // I think this prevents caves from generating too far from the origin chunk
                if (caveStartXOffsetFromCenter * caveStartXOffsetFromCenter + caveStartZOffsetFromCenter * caveStartZOffsetFromCenter - distanceToEnd * distanceToEnd > d7 * d7) {
                    return;
                }

                // Only continue if cave start is close enough to origin
                if (caveStartX >= originBlockX - 16.0D - xzOffset * 2.0D && caveStartZ >= originBlockZ - 16.0D - xzOffset * 2.0D && caveStartX <= originBlockX + 16.0D + xzOffset * 2.0D && caveStartZ <= originBlockZ + 16.0D + xzOffset * 2.0D) {
                    int minX = MathHelper.floor_double(caveStartX - xzOffset) - originChunkX * 16 - 1;
                    int minY = MathHelper.floor_double(caveStartY - yOffset) - 1;
                    int minZ = MathHelper.floor_double(caveStartZ - xzOffset) - originChunkZ * 16 - 1;
                    int maxX = MathHelper.floor_double(caveStartX + xzOffset) - originChunkX * 16 + 1;
                    int maxY = MathHelper.floor_double(caveStartY + yOffset) + 1;
                    int maxZ = MathHelper.floor_double(caveStartZ + xzOffset) - originChunkZ * 16 + 1;

                    if (minX < 0) {
                        minX = 0;
                    }

                    if (maxX > 16) {
                        maxX = 16;
                    }

                    if (minY < 1) {
                        minY = 1;
                    }

                    if (maxY > 248) {
                        maxY = 248;
                    }

                    if (minZ < 0) {
                        minZ = 0;
                    }

                    if (maxZ > 16) {
                        maxZ = 16;
                    }

                    for (int currX = minX; currX < maxX; ++currX) {
                        // Distance along the x-axis from the center (caveStart) of this ellipsoid.
                        // You can think of this value as (x/a), where a is the length of the ellipsoid's semi-axis in the x direction.
                        double xAxisDist = ((double) (currX + originChunkX * 16) + 0.5D - caveStartX) / xzOffset;

                        for (int currZ = minZ; currZ < maxZ; ++currZ) {
                            // Distance along the z-axis from the center (caveStart) of this ellipsoid.
                            // You can think of this value as (z/b), where b is the length of the ellipsoid's semi-axis in the z direction (same as a in this case).
                            double zAxisDist = ((double) (currZ + originChunkZ * 16) + 0.5D - caveStartZ) / xzOffset;

                            // Skip column if carving mask not set
                            if (!carvingMask[currX][currZ])
                                continue;

                            // Only operate on points within ellipse on XZ axis. Avoids unnecessary computation along y axis
                            if (xAxisDist * xAxisDist + zAxisDist * zAxisDist < 1.0D) {
                                for (int currY = maxY; currY > minY; --currY) {
                                    // Distance along the y-axis from the center (caveStart) of this ellipsoid.
                                    // You can think of this value as (y/c), where c is the length of the ellipsoid's semi-axis in the y direction.
                                    double yAxisDist = ((double) (currY - 1) + 0.5D - caveStartY) / yOffset;

                                    // Only operate on points within the ellipsoid.
                                    // This conditional is validating the current coordinate against the equation of the ellipsoid, that is,
                                    // (x/a)^2 + (z/b)^2 + (y/c)^2 <= 1.
                                    if (yAxisDist > -0.7D && xAxisDist * xAxisDist + yAxisDist * yAxisDist + zAxisDist * zAxisDist < 1.0D) {
                                        liquidBlock = liquidBlocks[BetterCavesUtils.getLocal(currX)][BetterCavesUtils.getLocal(currZ)];
                                        if (this.isDebugVisualizerEnabled)
                                            CarverUtils.debugDigBlock(primer, currX, currY, currZ, debugBlock, true);
                                        else
                                            digBlock(worldObj, primer, originChunkX, originChunkZ, currX, currY, currZ, liquidBlock, this.liquidAltitude, this.isReplaceGravel);
                                    } else {
                                        if (this.isDebugVisualizerEnabled)
                                            CarverUtils.debugDigBlock(primer, currX, currY, currZ, debugBlock, false);
                                    }
                                }
                            }
                        }
                    }

                    if (comesFromRoom) {
                        break;
                    }
                }
            }
            startCounter++;
        }
    }

    private void digBlock(World world, ChunkPrimer primer, int chunkX, int chunkZ, int localX, int y, int localZ, Block liquidBlockState, int liquidAltitude, boolean replaceGravel) {
        BlockPos pos = new BlockPos(chunkX * 16 + localX, y, chunkZ * 16 + localZ);

        // Don't dig boundaries between flooded and unflooded openings.
        boolean flooded = isFloodedUndergroundEnabled && !isDebugVisualizerEnabled && BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoordsBody(pos.x, pos.z), BiomeDictionary.Type.OCEAN) && y < 63/*world.getSeaLevel()*/;

        if (flooded) {
            if (
                !BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(pos.x + 1, pos.z), BiomeDictionary.Type.OCEAN) ||
                    !BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(pos.x - 1, pos.z), BiomeDictionary.Type.OCEAN) ||
                    !BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(pos.x, pos.z + 1), BiomeDictionary.Type.OCEAN) ||
                    !BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoordsBody(pos.x, pos.z - 1), BiomeDictionary.Type.OCEAN)
            ) {
                return;
            }
        }

        Block airBlockState = flooded ? Blocks.water : Blocks.air;
        CarverUtils.digBlock(world, primer, pos, airBlockState, liquidBlockState, liquidAltitude, replaceGravel);
    }

    @Override
    protected boolean isOceanBlock(Block[] data, int index, int x, int y, int z, int chunkX, int chunkZ) {
        return false;
    }
}
