package micdoodle8.mods.galacticraft.planets.venus;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;

public class EventHandlerVenus
{
    @SubscribeEvent
    public void lightningStrikeEvent(EntityStruckByLightningEvent event)
    {
        if (event.lightning.worldObj.provider.dimensionId == ConfigManagerVenus.dimensionIDVenus)
        {
            event.setResult(Event.Result.DEFAULT);
            event.setCanceled(true);
        }
    }

}
