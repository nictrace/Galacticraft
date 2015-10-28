package micdoodle8.mods.galacticraft.planets.venus.world.gen;

import com.google.common.collect.Lists;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeDecoratorSpace;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.ChunkProviderSpace;
import micdoodle8.mods.galacticraft.api.prefab.world.gen.MapGenBaseMeta;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedCreeper;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSkeleton;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedSpider;
import micdoodle8.mods.galacticraft.core.entities.EntityEvolvedZombie;
import micdoodle8.mods.galacticraft.core.world.gen.dungeon.MapGenDungeon;
import micdoodle8.mods.galacticraft.planets.venus.blocks.VenusBlocks;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class ChunkProviderVenus extends ChunkProviderSpace
{
    private final BiomeDecoratorVenus venusBiomeDecorator = new BiomeDecoratorVenus();
    private final MapGenCaveVenus cavernGenerator = new MapGenCaveVenus();

    private final MapGenDungeon dungeonGenerator = new MapGenDungeon(VenusBlocks.venusBlock, 7, 8, 16, 6);

    public ChunkProviderVenus(World par1World, long seed, boolean mapFeaturesEnabled)
    {
        super(par1World, seed, mapFeaturesEnabled);
//        this.dungeonGenerator.otherRooms.add(new RoomEmptyVenus(null, 0, 0, 0, ForgeDirection.UNKNOWN));
//        this.dungeonGenerator.otherRooms.add(new RoomSpawnerVenus(null, 0, 0, 0, ForgeDirection.UNKNOWN));
//        this.dungeonGenerator.otherRooms.add(new RoomSpawnerVenus(null, 0, 0, 0, ForgeDirection.UNKNOWN));
//        this.dungeonGenerator.otherRooms.add(new RoomSpawnerVenus(null, 0, 0, 0, ForgeDirection.UNKNOWN));
//        this.dungeonGenerator.otherRooms.add(new RoomSpawnerVenus(null, 0, 0, 0, ForgeDirection.UNKNOWN));
//        this.dungeonGenerator.otherRooms.add(new RoomSpawnerVenus(null, 0, 0, 0, ForgeDirection.UNKNOWN));
//        this.dungeonGenerator.otherRooms.add(new RoomSpawnerVenus(null, 0, 0, 0, ForgeDirection.UNKNOWN));
//        this.dungeonGenerator.otherRooms.add(new RoomSpawnerVenus(null, 0, 0, 0, ForgeDirection.UNKNOWN));
//        this.dungeonGenerator.otherRooms.add(new RoomSpawnerVenus(null, 0, 0, 0, ForgeDirection.UNKNOWN));
//        this.dungeonGenerator.otherRooms.add(new RoomChestsVenus(null, 0, 0, 0, ForgeDirection.UNKNOWN));
//        this.dungeonGenerator.otherRooms.add(new RoomChestsVenus(null, 0, 0, 0, ForgeDirection.UNKNOWN));
//        this.dungeonGenerator.bossRooms.add(new RoomBossVenus(null, 0, 0, 0, ForgeDirection.UNKNOWN));
//        this.dungeonGenerator.treasureRooms.add(new RoomTreasureVenus(null, 0, 0, 0, ForgeDirection.UNKNOWN));
    }

    @Override
    protected BiomeDecoratorSpace getBiomeGenerator()
    {
        return this.venusBiomeDecorator;
    }

    @Override
    protected BiomeGenBase[] getBiomesForGeneration()
    {
        return new BiomeGenBase[] { BiomeGenBaseVenus.venusFlat };
    }

    @Override
    protected int getSeaLevel()
    {
        return 93;
    }

    @Override
    protected List<MapGenBaseMeta> getWorldGenerators()
    {
        List<MapGenBaseMeta> generators = Lists.newArrayList();
        generators.add(this.cavernGenerator);
        return generators;
    }

    @Override
    protected BiomeGenBase.SpawnListEntry[] getMonsters()
    {
        List<BiomeGenBase.SpawnListEntry> monsters = new ArrayList<BiomeGenBase.SpawnListEntry>();
        monsters.add(new BiomeGenBase.SpawnListEntry(EntityEvolvedZombie.class, 8, 2, 3));
        monsters.add(new BiomeGenBase.SpawnListEntry(EntityEvolvedSpider.class, 8, 2, 3));
        monsters.add(new BiomeGenBase.SpawnListEntry(EntityEvolvedSkeleton.class, 8, 2, 3));
        monsters.add(new BiomeGenBase.SpawnListEntry(EntityEvolvedCreeper.class, 8, 2, 3));
        return monsters.toArray(new BiomeGenBase.SpawnListEntry[monsters.size()]);
    }

    @Override
    protected BiomeGenBase.SpawnListEntry[] getCreatures()
    {
        return new BiomeGenBase.SpawnListEntry[0];
    }

    @Override
    protected BlockMetaPair getGrassBlock()
    {
        return new BlockMetaPair(Blocks.wool, (byte)4);
    }

    @Override
    protected BlockMetaPair getDirtBlock()
    {
        return new BlockMetaPair(Blocks.dirt, (byte)0);
    }

    @Override
    protected BlockMetaPair getStoneBlock()
    {
        return new BlockMetaPair(Blocks.stone, (byte)0);
    }

//    @Override
//    protected BlockMetaPair getGrassBlock()
//    {
//        return new BlockMetaPair(VenusBlocks.venusBlock, (byte) 5);
//    }
//
//    @Override
//    protected BlockMetaPair getDirtBlock()
//    {
//        return new BlockMetaPair(VenusBlocks.venusBlock, (byte) 6);
//    }
//
//    @Override
//    protected BlockMetaPair getStoneBlock()
//    {
//        return new BlockMetaPair(VenusBlocks.venusBlock, (byte) 9);
//    }

    @Override
    public double getHeightModifier()
    {
        return 12;
    }

    @Override
    public double getSmallFeatureHeightModifier()
    {
        return 26;
    }

    @Override
    public double getMountainHeightModifier()
    {
        return 95;
    }

    @Override
    public double getValleyHeightModifier()
    {
        return 50;
    }

    @Override
    public int getCraterProbability()
    {
        return 2000;
    }

    @Override
    public void onChunkProvide(int cX, int cZ, Block[] blocks, byte[] metadata)
    {
        this.dungeonGenerator.generateUsingArrays(this.worldObj, this.worldObj.getSeed(), cX * 16, 30, cZ * 16, cX, cZ, blocks, metadata);
    }

    @Override
    public void onPopulate(IChunkProvider provider, int cX, int cZ)
    {
        this.dungeonGenerator.handleTileEntities(this.rand);
    }
}
