package tconstruct.blocks.logic;

import tconstruct.TConstruct;
import tconstruct.blocks.SignalTerminal;
import tconstruct.blocks.TConstructBlock;
import tconstruct.common.TContent;
import tconstruct.library.util.CoordTuple;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class SignalTerminalLogic extends TileEntity
{
    private byte[] connectedSides = new byte[6];
    private byte[] receivingSides = new byte[6];
    private byte[] sideChannel = new byte[6];
    
    private int pendingSide = -1;
    private CoordTuple signalBus = null;
    private boolean doUpdate = false;
    private boolean isRegistered = false;

    public SignalTerminalLogic()
    {
        super();
        
        for (int i = 0; i < 6; i++) {
            connectedSides[i] = -1;
            receivingSides[i] = -1;
            sideChannel[i] = 0;
        }
        
    }

    public byte getSignal (byte signal)
    {
        byte highSignal = 0;
        
        for (int i = 0; i < 6; i++) {
            if (sideChannel[i] == signal && receivingSides[i] > 0) {
                if (receivingSides[i] > highSignal) {
                    highSignal = receivingSides[i];
                }
            }
        }

        return highSignal;
    }

    private void tryRegister ()
    {
        boolean wasRegistered = isRegistered;
        if (worldObj == null || !(worldObj instanceof World))
        {
            return;
        }
        if (signalBus == null || !(signalBus instanceof CoordTuple))
        {
            return;
        }

        TileEntity te = worldObj.getBlockTileEntity(signalBus.x, signalBus.y, signalBus.z);
        if (te == null || !(te instanceof SignalBusLogic))
        {
            return;
        }

        isRegistered = ((SignalBusLogic) te).registerTerminal(worldObj, xCoord, yCoord, zCoord);

        if (isRegistered != wasRegistered)
        {
            if (isRegistered)
            {
                doUpdate = true;
            }
            else
            {
                signalBus = null;
                doUpdate = true;
            }
        }
    }

    @Override
    public void readFromNBT (NBTTagCompound data)
    {
        super.readFromNBT(data);

        sideChannel = data.getByteArray("sideChannel");
        receivingSides = data.getByteArray("receivingSides");
        connectedSides = data.getByteArray("connectedSides");

        if (sideChannel.length != 6) { sideChannel = new byte[] { 0, 0, 0, 0, 0, 0 }; }
        if (receivingSides.length != 6) { receivingSides = new byte[] { -1, -1, -1, -1, -1, -1 }; }
        if (connectedSides.length != 6) { connectedSides = new byte[] { -1, -1, -1, -1, -1, -1 }; }
        
        int tX = data.getInteger("BusX");
        int tY = data.getInteger("BusY");
        int tZ = data.getInteger("BusZ");

        if (tX == xCoord && tY == yCoord && tZ == zCoord) {
            signalBus = null;
        } else {
            signalBus = new CoordTuple(tX, tY, tZ);
        }

        if (!isRegistered)
        {
            tryRegister();
        }

        doUpdate = true;
    }

    @Override
    public void writeToNBT (NBTTagCompound data)
    {
        super.writeToNBT(data);

        data.setByteArray("sideChannel", sideChannel);
        data.setByteArray("receivingSides", receivingSides);
        data.setByteArray("connectedSides", connectedSides);

        if (signalBus != null)
        {
            data.setInteger("BusX", signalBus.x);
            data.setInteger("BusY", signalBus.y);
            data.setInteger("BusZ", signalBus.z);
        }
        else
        {
            data.setInteger("BusX", xCoord);
            data.setInteger("BusY", yCoord);
            data.setInteger("BusZ", zCoord);
        }
    }

    private void checkSanity ()
    {
        if (worldObj == null || worldObj.isRemote)
        {
            return;
        }

        boolean brainless = false;
        if (signalBus == null)
        {
            return;
        }

        if (!(worldObj.getChunkProvider().chunkExists(signalBus.x >> 4, signalBus.z >> 4))) {
            return;
        }
        
        if (!(signalBus instanceof CoordTuple))
        {
            signalBus = null;
            brainless = true;
        }
        TileEntity te = worldObj.getBlockTileEntity(signalBus.x, signalBus.y, signalBus.z);
        if (te == null || !(te instanceof SignalBusLogic))
        {
            // We missed the bus! And we'll never, ever, ever do it again
            signalBus = null;
            brainless = true;
        }
        else
        {
            if (!SignalBusLogic.hasTerminals(worldObj, signalBus.x, signalBus.y, signalBus.z))
            {
                tryRegister();
                if (!isRegistered)
                {
                    brainless = true;
                }
            }
        }

        if (brainless)
        {
            this.receiveSignals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
            doUpdate = true;
        }

    }

    @Override
    public void updateEntity ()
    {
        if (pendingSide >= 0 && pendingSide < 6 && connectedSides[pendingSide] == -1)
        {
            connectedSides[pendingSide] = 0;
            pendingSide = -1;
        }

        if (!doUpdate)
        {
            return;
        }
        
        //checkSanity();

        if (!isRegistered)
        {
            tryRegister();
        }

        doUpdate = false;

        if (!worldObj.isRemote)
        {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
        if (signalBus == null)
        {
            return;
        }
        if (worldObj.isRemote)
        {
            return;
        }

        doUpdate = false;

        if (!isRegistered)
        {
            doUpdate = true;
        }

        return;
    }

    public void receiveSignals (byte[] signals)
    {
        int oldValue;
        
        for (int i = 0; i < 6; i++) {
            if (connectedSides[i] != -1) {
                oldValue = connectedSides[i];
                if (signals[sideChannel[i]] > receivingSides[i]) {
                    receivingSides[i] = 0;
                    connectedSides[i] = signals[sideChannel[i]];
                }
                if (receivingSides[i] == 0 && connectedSides[i] != signals[sideChannel[i]]) {
                    connectedSides[i] = signals[sideChannel[i]];
                }
                
                if (oldValue != connectedSides[i])
                {
                    worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, TConstruct.instance.content.signalTerminal.blockID);
                }
                
            }
        }
    }
    
    public void sendSignals () {
        if (!(signalBus instanceof CoordTuple)) {
            return;
        }
        TileEntity te = worldObj.getBlockTileEntity(signalBus.x, signalBus.y, signalBus.z);
        if (!(te instanceof SignalBusLogic)) {
            return;
        }
        byte[] highSignal = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        
        int indirect = 0;
        int direct = 0;
        int offset[] = new int[] { 0, 0, 0 };
        int oSide = 0;
        for (int i = 0; i < 6; i++) {
            if (connectedSides[i] != -1) {

                offset[0] = 0;
                offset[1] = 0;
                offset[2] = 0;
                switch (i) {
                case 0:
                    offset[1] += -1;
                    oSide = 1;
                    break;
                case 1:
                    offset[1] += 1;
                    oSide = 0;
                    break;
                case 2:
                    offset[2] += 1;
                    oSide = 3;
                    break;
                case 3:
                    offset[2] += -1;
                    oSide = 2;
                    break;
                case 4:
                    offset[0] += -1;
                    oSide = 5;
                    break;
                case 5:
                    offset[0] += 1;
                    oSide = 4;
                    break;
                }
                indirect = worldObj.getStrongestIndirectPower(xCoord + offset[0], yCoord + offset[1], zCoord + offset[2]);
                direct = worldObj.isBlockProvidingPowerTo(xCoord + offset[0], yCoord + offset[1], zCoord + offset[2], oSide);
                                
                if (Math.max(indirect, direct) > connectedSides[i])
                {
                    receivingSides[i] = (byte)Math.max(indirect, direct);
                    connectedSides[i] = 0;
                    highSignal[sideChannel[i]] = (byte)Math.max(highSignal[sideChannel[i]], Math.max(indirect, direct));
                }
                else {
                    receivingSides[i] = 0;
                    connectedSides[i] = 0;
                }
            }
            
        }
        
        ((SignalBusLogic)te).fullUpdateLocalSignals();
    }

    public void setBusCoords (World world, int x, int y, int z)
    {
        if (world.provider.dimensionId == worldObj.provider.dimensionId && !world.isRemote && !worldObj.isRemote)
        {
            signalBus = new CoordTuple(x, y, z);
        }
        doUpdate = true;
    }

    public int isProvidingStrongPower (int side)
    {
        int tside = side;

        switch (side)
        {
        case 0:
            tside = 1;
            break;
        case 1:
            tside = 0;
            break;
        case 2:
            tside = 2;
            break;
        case 3:
            tside = 3;
            break;
        case 4:
            tside = 5;
            break;
        case 5:
            tside = 4;
            break;
        default:
            tside = side;
        }

        if (side >= 0 && tside < 6)
        {
            if (connectedSides[tside] > 0 && receivingSides[tside] == 0)
            {
                return connectedSides[tside];
            }
        }

        return 0;
    }
    
    public int isProvidingWeakPower (int side)
    {
        int tside = side;

        switch (side)
        {
        case 0:
            tside = 1;
            break;
        case 1:
            tside = 0;
            break;
        case 2:
            tside = 2;
            break;
        case 3:
            tside = 3;
            break;
        case 4:
            tside = 5;
            break;
        case 5:
            tside = 4;
            break;
        default:
            tside = side;
        }

        if (side >= 0 && tside < 6)
        {
            if (connectedSides[tside] > 1 && receivingSides[tside] == 0)
            {
                return connectedSides[tside] - 1;
            }
        }

        return 0;
    }

    public void addPendingSide (int side)
    {
        pendingSide = side;
    }

    public void connectPending ()
    {
        if (pendingSide >= 0 && pendingSide < 6 && connectedSides[pendingSide] == -1)
        {
            connectedSides[pendingSide] = 0;
        }
        pendingSide = -1;
        doUpdate = true;
    }

    public byte[] getConnectedSides ()
    {
        return connectedSides.clone();
    }

    public String debugString ()
    {
        String tstr = "";

        if (!worldObj.isRemote)
        {
            byte tbyte = (byte) 0;
            for (int n = 0; n < 6; n++)
            {
                if (connectedSides[n] != -1)
                {
                    tbyte &= (1 << n);
                }
            }
            tstr += "Sides: " + tbyte + "\n";
        }

        return tstr;
    }

    /* Packets */
    @Override
    public Packet getDescriptionPacket ()
    {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new Packet132TileEntityData(xCoord, yCoord, zCoord, 1, tag);
    }

    @Override
    public void onDataPacket (INetworkManager net, Packet132TileEntityData packet)
    {
        readFromNBT(packet.data);
        onInventoryChanged();
        worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
        this.doUpdate = true;
    }

    public static Icon getChannelIcon (IBlockAccess world, int x, int y, int z, int side)
    {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te != null && te instanceof SignalTerminalLogic)
        {
            int channel = ((SignalTerminalLogic) te).sideChannel[side];

            return ((SignalTerminal) TConstruct.instance.content.signalTerminal).getChannelIcon(channel);
        }

        return ((SignalTerminal) TConstruct.instance.content.signalTerminal).getChannelIcon(0);
    }
    
    public static Icon[] getChannelIcons () {
        return ((SignalTerminal) TConstruct.instance.content.signalTerminal).channelIcons;
    }
    
    public static Icon getChannelIcon (int channel) {
        return getChannelIcons()[channel];
    }
    
    public Icon getChannelIconFromLogic(int side) {
        return getChannelIcons()[sideChannel[side]];
    }

    public void nextChannel (int side)
    {
        sideChannel[side]++;

        if (sideChannel[side] >= 16)
        {
            sideChannel[side] = 0;
        }

        doUpdate = true;
    }

    public void prevChannel (int side)
    {
        sideChannel[side]--;
        
        if (sideChannel[side] < 0)
        {
            sideChannel[side] = 15;
        }
        
        doUpdate = true;
    }

    public void notifyBreak ()
    {
        if (!(worldObj instanceof World))
        {
            return;
        }
        if (!(signalBus instanceof CoordTuple))
        {
            return;
        }

        TileEntity te = worldObj.getBlockTileEntity(signalBus.x, signalBus.y, signalBus.z);
        if (te == null || !(te instanceof SignalBusLogic))
        {
            return;
        }

        ((SignalBusLogic) te).unregisterTerminal(worldObj, xCoord, yCoord, zCoord);
    }
    
    public void onNeighborBlockChange () {
        sendSignals();
    }

    public Icon[] getSideIcons ()
    {
        Icon[] icons = getChannelIcons();
        Icon[] sideIcons = new Icon[6];
        
        for (int i = 0; i < 6; i++) {
            if (sideChannel[i] > 0) {
                sideIcons[i] = icons[sideChannel[i]];
            }
            else {
                sideIcons[i] = icons[0];
            }
        }
        
        return sideIcons;
    }
    
    public byte[] getReceivedSignals() {
        byte[] highChannel = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        
        for (int i = 0; i < 6; i++) {
            if (connectedSides[i] != -1) 
            {
                highChannel[sideChannel[i]] = (byte)Math.max(highChannel[sideChannel[i]], receivingSides[i]);
            }
        }

        return highChannel;
    }
}
