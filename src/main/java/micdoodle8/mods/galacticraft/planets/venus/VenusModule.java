package micdoodle8.mods.galacticraft.planets.venus;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import micdoodle8.mods.galacticraft.api.GalacticraftRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.recipe.CompressorRecipes;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IAtmosphericGas;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.items.GCItems;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.planets.GalacticraftPlanets;
import micdoodle8.mods.galacticraft.planets.GuiIdsPlanets;
import micdoodle8.mods.galacticraft.planets.IPlanetsModule;
import micdoodle8.mods.galacticraft.planets.venus.blocks.VenusBlocks;
import micdoodle8.mods.galacticraft.planets.venus.dimension.TeleportTypeVenus;
import micdoodle8.mods.galacticraft.planets.venus.dimension.WorldProviderVenus;
import micdoodle8.mods.galacticraft.planets.venus.items.VenusItems;
import micdoodle8.mods.galacticraft.planets.venus.network.PacketSimpleVenus;
import micdoodle8.mods.galacticraft.planets.venus.recipe.RecipeManagerVenus;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

public class VenusModule implements IPlanetsModule
{
    public static final String ASSET_PREFIX = "galacticraftvenus";
    public static final String TEXTURE_PREFIX = VenusModule.ASSET_PREFIX + ":";

    public static Material sludgeMaterial = new MaterialLiquid(MapColor.foliageColor);

    public static Planet planetVenus;

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new EventHandlerVenus());
        new ConfigManagerVenus(new File(event.getModConfigurationDirectory(), "Galacticraft/venus.conf"));

        VenusBlocks.initBlocks();
        VenusBlocks.registerBlocks();
        VenusBlocks.setHarvestLevels();
        VenusBlocks.oreDictRegistration();

        VenusItems.initItems();
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        this.registerMicroBlocks();

        GalacticraftCore.packetPipeline.addDiscriminator(8, PacketSimpleVenus.class);

        this.registerTileEntities();
        this.registerCreatures();
        this.registerOtherEntities();

        VenusModule.planetVenus = (Planet) new Planet("venus").setParentSolarSystem(GalacticraftCore.solarSystemSol).setRingColorRGB(0.67F, 0.1F, 0.1F).setRelativeSize(0.5319F).setPhaseShift(2.0F).setRelativeDistanceFromCenter(new CelestialBody.ScalableDistance(0.75F, 0.75F)).setRelativeOrbitTime(0.61527929901423877327491785323111F);
        VenusModule.planetVenus.setBodyIcon(new ResourceLocation(GalacticraftCore.ASSET_PREFIX, "textures/gui/celestialbodies/venus.png"));
        VenusModule.planetVenus.setDimensionInfo(ConfigManagerVenus.dimensionIDVenus, WorldProviderVenus.class).setTierRequired(2);
        VenusModule.planetVenus.atmosphereComponent(IAtmosphericGas.CO2).atmosphereComponent(IAtmosphericGas.ARGON).atmosphereComponent(IAtmosphericGas.NITROGEN);

        GalaxyRegistry.registerPlanet(VenusModule.planetVenus);
        GalacticraftRegistry.registerTeleportType(WorldProviderVenus.class, new TeleportTypeVenus());
        GalacticraftRegistry.registerRocketGui(WorldProviderVenus.class, new ResourceLocation(VenusModule.ASSET_PREFIX, "textures/gui/venusRocketGui.png"));
//        GalacticraftRegistry.addDungeonLoot(2, new ItemStack(VenusItems.schematic, 1, 0));
//        GalacticraftRegistry.addDungeonLoot(2, new ItemStack(VenusItems.schematic, 1, 1));
//        GalacticraftRegistry.addDungeonLoot(2, new ItemStack(VenusItems.schematic, 1, 2));
//        GalacticraftRegistry.addDungeonLoot(3, new ItemStack(VenusItems.schematic, 1, 2));
//
//        CompressorRecipes.addShapelessRecipe(new ItemStack(VenusItems.venusItemBasic, 1, 3), new ItemStack(GCItems.heavyPlatingTier1), new ItemStack(GCItems.meteoricIronIngot, 1, 1));
//        CompressorRecipes.addShapelessRecipe(new ItemStack(VenusItems.venusItemBasic, ConfigManagerCore.quickMode ? 2 : 1, 5), new ItemStack(VenusItems.venusItemBasic, 1, 2));
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        RecipeManagerVenus.loadRecipes();
    }

    @Override
    public void serverStarting(FMLServerStartingEvent event)
    {
    }

    @Override
    public void serverInit(FMLServerStartedEvent event)
    {

    }

    private void registerMicroBlocks()
    {
		try {
			Class clazz = Class.forName("codechicken.microblock.MicroMaterialRegistry");
			if (clazz != null)
			{
				Method registerMethod = null;
				Method[] methodz = clazz.getMethods();
				for (Method m : methodz)
				{
					if (m.getName().equals("registerMaterial"))
					{
						registerMethod = m;
						break;
					}
				}
				Class clazzbm = Class.forName("codechicken.microblock.BlockMicroMaterial");
				registerMethod.invoke(null, clazzbm.getConstructor(Block.class, int.class).newInstance(VenusBlocks.venusBlock, 4), "tile.venus.venuscobblestone");
				registerMethod.invoke(null, clazzbm.getConstructor(Block.class, int.class).newInstance(VenusBlocks.venusBlock, 5), "tile.venus.venusgrass");
				registerMethod.invoke(null, clazzbm.getConstructor(Block.class, int.class).newInstance(VenusBlocks.venusBlock, 6), "tile.venus.venusdirt");
				registerMethod.invoke(null, clazzbm.getConstructor(Block.class, int.class).newInstance(VenusBlocks.venusBlock, 7), "tile.venus.venusdungeon");
				registerMethod.invoke(null, clazzbm.getConstructor(Block.class, int.class).newInstance(VenusBlocks.venusBlock, 8), "tile.venus.venusdeco");
				registerMethod.invoke(null, clazzbm.getConstructor(Block.class, int.class).newInstance(VenusBlocks.venusBlock, 9), "tile.venus.venusstone");
			}
		} catch (Exception e) {}
	}

    public void registerTileEntities()
    {
    }

    public void registerCreatures()
    {
    }

    public void registerOtherEntities()
    {
    }

    public void registerGalacticraftCreature(Class<? extends Entity> var0, String var1, int back, int fore)
    {
        EntityList.stringToClassMapping.put(var1, var0);
        EntityRegistry.registerModEntity(var0, var1, GCCoreUtil.nextInternalID(), GalacticraftPlanets.instance, 80, 3, true);
    }

    public static void registerGalacticraftNonMobEntity(Class<? extends Entity> var0, String var1, int trackingDistance, int updateFreq, boolean sendVel)
    {
    	if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
		{
    		LanguageRegistry.instance().addStringLocalization("entity.GalacticraftVenus." + var1 + ".name", GCCoreUtil.translate("entity." + var1 + ".name"));
		}
    	EntityRegistry.registerModEntity(var0, var1, GCCoreUtil.nextInternalID(), GalacticraftPlanets.instance, trackingDistance, updateFreq, sendVel);
    }

    @Override
    public void getGuiIDs(List<Integer> idList)
    {
        idList.add(GuiIdsPlanets.MACHINE_VENUS);
    }

    @Override
    public Object getGuiElement(Side side, int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (side == Side.SERVER)
        {
            TileEntity tile = world.getTileEntity(x, y, z);

            if (ID == GuiIdsPlanets.MACHINE_VENUS)
            {
//                if (tile instanceof TileEntityTerraformer)
//                {
//                    return new ContainerTerraformer(player.inventory, (TileEntityTerraformer) tile);
//                }
            }
        }

        return null;
    }

    @Override
    public Configuration getConfiguration()
    {
        return ConfigManagerVenus.config;
    }

    @Override
    public void syncConfig()
    {
        ConfigManagerVenus.syncConfig(false);
    }

    public static long tickCount = 0;

    @Override
    public void onServerTick(TickEvent.ServerTickEvent event)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        //Prevent issues when clients switch to LAN servers
        if (server == null) return;

        if (event.phase == TickEvent.Phase.START)
        {
            if (tickCount % 25 == 0)
            {
                World worldVenus = FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(ConfigManagerVenus.dimensionIDVenus);

                if (worldVenus != null)
                {
                    for (Object o : worldVenus.playerEntities)
                    {
                        if (o instanceof EntityPlayer)
                        {
                            EntityPlayer player = (EntityPlayer) o;
                            double posX = player.posX + Math.random() * 500 - 250;
                            double posZ = player.posZ + Math.random() * 500 - 250;
                            EntityLightningBolt bolt = new EntityLightningBolt(worldVenus, (int)posX, 125, (int)posZ);
                            bolt.renderDistanceWeight *= 5;
                            worldVenus.addWeatherEffect(bolt);
                        }
                    }
                }
            }
        }
        else
        {
            tickCount++;
        }
    }
}
