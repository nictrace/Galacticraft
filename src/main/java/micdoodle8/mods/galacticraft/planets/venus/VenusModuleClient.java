package micdoodle8.mods.galacticraft.planets.venus;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.client.CloudRenderer;
import micdoodle8.mods.galacticraft.planets.GuiIdsPlanets;
import micdoodle8.mods.galacticraft.planets.IPlanetsModuleClient;
import micdoodle8.mods.galacticraft.planets.venus.client.CloudRendererVenus;
import micdoodle8.mods.galacticraft.planets.venus.client.SkyProviderVenus;
import micdoodle8.mods.galacticraft.planets.venus.dimension.WorldProviderVenus;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EntityDropParticleFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

import java.util.List;

public class VenusModuleClient implements IPlanetsModuleClient
{
    @Override
    public void preInit(FMLPreInitializationEvent event)
    {

    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        FMLCommonHandler.instance().bus().register(new TickHandlerClient());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        // Tile Entity Renderers

        // Entities

        // Add Armor Renderer Prefix

        // Item Renderers
    }

    @Override
    public Object getGuiElement(Side side, int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        if (side == Side.CLIENT)
        {
            TileEntity tile = world.getTileEntity(x, y, z);

            if (ID == GuiIdsPlanets.MACHINE_VENUS)
            {
//                if (tile instanceof TileEntityTerraformer)
//                {
//                    return new GuiTerraformer(player.inventory, (TileEntityTerraformer) tile);
//                }
            }
        }

        return null;
    }

    @Override
    public int getBlockRenderID(Block block)
    {
//        if (block == VenusBlocks.vine)
//        {
//            return VenusModuleClient.vineRenderID;
//        }

        return -1;
    }

    @Override
    public void spawnParticle(String particleID, Vector3 position, Vector3 motion, Object... extraData)
    {
        Minecraft mc = FMLClientHandler.instance().getClient();

        if (mc != null && mc.renderViewEntity != null && mc.effectRenderer != null)
        {
            final double dPosX = mc.renderViewEntity.posX - position.x;
            final double dPosY = mc.renderViewEntity.posY - position.y;
            final double dPosZ = mc.renderViewEntity.posZ - position.z;
            EntityFX particle = null;
            final double maxDistSqrd = 64.0D;

            if (dPosX * dPosX + dPosY * dPosY + dPosZ * dPosZ < maxDistSqrd * maxDistSqrd)
            {
                if (particleID.equals("sludgeDrip"))
                {
                    particle = new EntityDropParticleFX(mc.theWorld, position.x, position.y, position.z, Material.water);
                }
            }

            if (particle != null)
            {
                particle.prevPosX = particle.posX;
                particle.prevPosY = particle.posY;
                particle.prevPosZ = particle.posZ;
                mc.effectRenderer.addEffect(particle);
            }
        }
    }

    @Override
    public void getGuiIDs(List<Integer> idList)
    {
        idList.add(GuiIdsPlanets.MACHINE_VENUS);
    }

    public static class TickHandlerClient
    {
        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public void onClientTick(ClientTickEvent event)
        {
            final Minecraft minecraft = FMLClientHandler.instance().getClient();

            final WorldClient world = minecraft.theWorld;

            if (world != null)
            {
                if (world.provider instanceof WorldProviderVenus)
                {
                    if (world.provider.getSkyRenderer() == null)
                    {
                        world.provider.setSkyRenderer(new SkyProviderVenus((IGalacticraftWorldProvider) world.provider));
                    }

                    if (world.provider.getCloudRenderer() == null)
                    {
                        CloudRendererVenus cloudRendererVenus = new CloudRendererVenus();
                        FMLCommonHandler.instance().bus().register(cloudRendererVenus);
                        world.provider.setCloudRenderer(cloudRendererVenus);
                    }
                }
            }
        }
    }
}
