package micdoodle8.mods.galacticraft.api.prefab.world.gen;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

/**
 * Do not include this prefab class in your released mod download.
 */
public abstract class BiomeDecoratorSpace
{
    protected Random rand;

    protected int chunkX;
    protected int chunkZ;

    public void decorate(World world, Random random, int chunkX, int chunkZ)
    {
        if (this.getCurrentWorld() != null)
        {
            throw new RuntimeException("Already decorating!!");
        }
        else
        {
            this.setCurrentWorld(world);
            this.rand = random;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
//            MinecraftForge.EVENT_BUS.post(new GCCoreEventPopulate.Pre(world, this.rand, this.chunkX, this.chunkZ)); TODO
            this.decorate();
//            MinecraftForge.EVENT_BUS.post(new GCCoreEventPopulate.Post(world, this.rand, this.chunkX, this.chunkZ)); TODO
            this.setCurrentWorld(null);
            this.rand = null;
        }
    }

    protected abstract void setCurrentWorld(World world);

	protected abstract World getCurrentWorld();

	protected void generateOre(int amountPerChunk, WorldGenerator worldGenerator, int minY, int maxY)
    {
        World currentWorld = this.getCurrentWorld();
		for (int var5 = 0; var5 < amountPerChunk; ++var5)
        {
            final int var6 = this.chunkX + this.rand.nextInt(16);
            final int var7 = this.rand.nextInt(maxY - minY) + minY;
            final int var8 = this.chunkZ + this.rand.nextInt(16);
            worldGenerator.generate(currentWorld, this.rand, new BlockPos(var6, var7, var8));
        }
    }

    protected abstract void decorate();
}
