package micdoodle8.mods.galacticraft.planets.venus.world.gen;

import micdoodle8.mods.galacticraft.api.prefab.world.gen.BiomeDecoratorSpace;
import micdoodle8.mods.galacticraft.core.world.gen.WorldGenMinableMeta;
import micdoodle8.mods.galacticraft.planets.venus.ConfigManagerVenus;
import micdoodle8.mods.galacticraft.planets.venus.blocks.VenusBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class BiomeDecoratorVenus extends BiomeDecoratorSpace
{
    private WorldGenerator dirtGen;
    private WorldGenerator deshGen;
    private WorldGenerator tinGen;
    private WorldGenerator copperGen;
    private WorldGenerator ironGen;
    private WorldGenerator iceGen;
    private World currentWorld;

    public BiomeDecoratorVenus()
    {
        this.copperGen = new WorldGenMinableMeta(VenusBlocks.venusBlock, 4, 0, true, VenusBlocks.venusBlock, 9);
        this.tinGen = new WorldGenMinableMeta(VenusBlocks.venusBlock, 4, 1, true, VenusBlocks.venusBlock, 9);
        this.deshGen = new WorldGenMinableMeta(VenusBlocks.venusBlock, 6, 2, true, VenusBlocks.venusBlock, 9);
        this.ironGen = new WorldGenMinableMeta(VenusBlocks.venusBlock, 8, 3, true, VenusBlocks.venusBlock, 9);
        this.dirtGen = new WorldGenMinableMeta(VenusBlocks.venusBlock, 32, 6, true, VenusBlocks.venusBlock, 9);
        this.iceGen = new WorldGenMinableMeta(Blocks.ice, 18, 0, true, VenusBlocks.venusBlock, 6);
    }

    @Override
    protected void decorate()
    {
        this.generateOre(4, this.iceGen, 60, 120);
        this.generateOre(20, this.dirtGen, 0, 200);
//        if (!ConfigManagerVenus.disableDeshGen) this.generateOre(15, this.deshGen, 20, 64);
//        if (!ConfigManagerVenus.disableCopperGen) this.generateOre(26, this.copperGen, 0, 60);
//        if (!ConfigManagerVenus.disableTinGen) this.generateOre(23, this.tinGen, 0, 60);
//        if (!ConfigManagerVenus.disableIronGen) this.generateOre(20, this.ironGen, 0, 64);
    }
    
    protected void setCurrentWorld(World world)
    {
    	this.currentWorld = world;
    }

	protected World getCurrentWorld()
	{
		return this.currentWorld;
	}
}
