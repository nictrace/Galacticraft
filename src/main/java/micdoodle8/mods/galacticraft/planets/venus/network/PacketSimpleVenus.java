package micdoodle8.mods.galacticraft.planets.venus.network;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import micdoodle8.mods.galacticraft.core.network.IPacket;
import micdoodle8.mods.galacticraft.core.network.NetworkUtil;
import micdoodle8.mods.galacticraft.core.util.GCLog;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PacketSimpleVenus implements IPacket
{
    public static enum EnumSimplePacketVenus
    {
        // SERVER
        S_VOID(Side.SERVER),
        // CLIENT
        C_VOID(Side.CLIENT);

        private Side targetSide;
        private Class<?>[] decodeAs;

        private EnumSimplePacketVenus(Side targetSide, Class<?>... decodeAs)
        {
            this.targetSide = targetSide;
            this.decodeAs = decodeAs;
        }

        public Side getTargetSide()
        {
            return this.targetSide;
        }

        public Class<?>[] getDecodeClasses()
        {
            return this.decodeAs;
        }
    }

    private EnumSimplePacketVenus type;
    private List<Object> data;

    public PacketSimpleVenus()
    {

    }

    public PacketSimpleVenus(EnumSimplePacketVenus packetType, Object[] data)
    {
        this(packetType, Arrays.asList(data));
    }

    public PacketSimpleVenus(EnumSimplePacketVenus packetType, List<Object> data)
    {
        if (packetType.getDecodeClasses().length != data.size())
        {
            GCLog.info("Simple Packet found data length different than packet type");
        }

        this.type = packetType;
        this.data = data;
    }

    @Override
    public void encodeInto(ChannelHandlerContext context, ByteBuf buffer)
    {
        buffer.writeInt(this.type.ordinal());

        try
        {
            NetworkUtil.encodeData(buffer, this.data);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void decodeInto(ChannelHandlerContext context, ByteBuf buffer)
    {
        this.type = EnumSimplePacketVenus.values()[buffer.readInt()];

        if (this.type.getDecodeClasses().length > 0)
        {
            this.data = NetworkUtil.decodeData(this.type.getDecodeClasses(), buffer);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleClientSide(EntityPlayer player)
    {
        EntityClientPlayerMP playerBaseClient = null;

        if (player instanceof EntityClientPlayerMP)
        {
            playerBaseClient = (EntityClientPlayerMP) player;
        }

        switch (this.type)
        {
        default:
            break;
        }
    }

    @Override
    public void handleServerSide(EntityPlayer player)
    {
        EntityPlayerMP playerBase = PlayerUtil.getPlayerBaseServerFromPlayer(player, false);
        GCPlayerStats stats = GCPlayerStats.get(playerBase);

        switch (this.type)
        {
        default:
            break;
        }
    }
}
