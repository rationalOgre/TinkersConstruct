package tconstruct.blocks.logic;

import tconstruct.blocks.component.SignalBusMasterLogic;
import tconstruct.common.TContent;
import tconstruct.library.multiblock.MultiblockMasterBaseLogic;
import tconstruct.library.util.CoordTuple;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class SignalTerminalLogic extends TileEntity
{

    private boolean[] connectedSides = null;
    private int pendingSide = -1;
    private CoordTuple signalBus = null;
    private boolean doUpdate = false;
    private byte signalSetting = 0;
    private byte signalStrength = 0;
    private boolean newTile = true;
    private byte cachedSignal = 0;
    private boolean isRegistered = false;

    public SignalTerminalLogic()
    {
        super();
        connectedSides = new boolean[] { false, false, false, false, false, false };

    }

    public byte getSignal (byte signal)
    {
        if (signal != signalSetting)
        {
            return 0;
        }

        return signalStrength;
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

        signalSetting = data.getByte("signalSetting");
        signalStrength = data.getByte("signalStrength");

        byte tbyte = data.getByte("connectedSides");
        for (int n = 0; n < 6; n++)
        {
            if (((tbyte >> n) & 1) == 1)
            {
                connectedSides[n] = true;
            }
        }

        int tX = data.getInteger("BusX");
        int tY = data.getInteger("BusY");
        int tZ = data.getInteger("BusZ");

        signalBus = new CoordTuple(tX, tY, tZ);

        if (!isRegistered)
        {
            tryRegister();
        }

        newTile = false;
        doUpdate = true;
    }

    @Override
    public void writeToNBT (NBTTagCompound data)
    {
        super.writeToNBT(data);

        data.setByte("signalSetting", signalSetting);
        data.setByte("signalStrength", signalStrength);

        byte tbyte = (byte) 0;
        for (int n = 0; n < 6; n++)
        {
            if (connectedSides[n])
            {
                tbyte = (byte) ((int) tbyte | (1 << n));
            }
        }
        data.setByte("connectedSides", tbyte);

        if (signalBus != null)
        {
            data.setInteger("BusX", signalBus.x);
            data.setInteger("BusY", signalBus.y);
            data.setInteger("BusZ", signalBus.z);
        }
        else
        {
            data.setInteger("BusX", 0);
            data.setInteger("BusY", 0);
            data.setInteger("BusZ", 0);
        }
    }

    @Override
    public void updateEntity ()
    {
        if (pendingSide >= 0 && pendingSide < 6)
        {
            connectedSides[pendingSide] = true;
            pendingSide = -1;
        }

        if (!doUpdate)
        {
            return;
        }

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
        TileEntity te = worldObj.getBlockTileEntity(signalBus.x, signalBus.y, signalBus.z);
        if (te instanceof SignalBusLogic)
        {
            MultiblockMasterBaseLogic master = ((SignalBusLogic) te).getMultiblockMaster();
            if (master instanceof SignalBusMasterLogic)
            {
                ((SignalBusMasterLogic) master).updateSignal(signalBus, signalSetting, signalStrength);
            }
        }

        doUpdate = false;

        if (!isRegistered)
        {
            doUpdate = true;
        }

        return;
    }

    public void reportToMaster (SignalBusMasterLogic master)
    {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

        master.updateSignal(new CoordTuple(xCoord, yCoord, zCoord), signalSetting, signalStrength);

        return;
    }

    public void receiveSignal (int strength)
    {
        signalStrength = (byte) strength;
        doUpdate = true;
    }

    public void receiveSignals (byte[] signals)
    {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);

        if (meta == 0 && signalStrength > 0)
        {
            return;
        }

        if (signals[(byte) signalSetting] == cachedSignal)
        {
            return;
        }

        if (signals[(byte) signalSetting] > 0)
        {
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, signals[(byte) signalSetting], 1);
        }
        else
        {
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1);
        }
        // Notify direct neighbors
        worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, TContent.signalTerminal.blockID);
        // Notify connected neighbors direction (strong power)
        for (int n = 0; n < 6; n++)
        {
            if (connectedSides[n])
            {
                ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[n];
                if (dir == ForgeDirection.NORTH || dir == ForgeDirection.SOUTH)
                {
                    dir = dir.getOpposite();
                }
                worldObj.notifyBlocksOfNeighborChange(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ, TContent.signalTerminal.blockID);
            }
        }

        cachedSignal = signals[(byte) signalSetting];
    }

    public void setBusCoords (World world, int x, int y, int z)
    {
        if (world.provider.dimensionId == worldObj.provider.dimensionId && !world.isRemote && !worldObj.isRemote)
        {
            signalBus = new CoordTuple(x, y, z);
        }
        doUpdate = true;
    }

    public int isProvidingStrongPower (int meta, int side)
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
            if (connectedSides[tside])
            {
                return meta;
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
        if (newTile)
        {
            int side = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
            connectedSides[side] = true;
            newTile = false;

            if (!worldObj.isRemote)
            {
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1);
            }
            doUpdate = true;
        }
        else
        {
            if (pendingSide >= 0 && pendingSide < 6)
            {
                connectedSides[pendingSide] = true;
            }
            pendingSide = -1;
            doUpdate = true;
        }
    }

    public boolean[] getConnectedSides ()
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
                if (connectedSides[n])
                {
                    tbyte &= (1 << n);
                }
            }
            tstr += "Sides: " + tbyte + "\n";
        }

        return tstr + "Channel: " + signalSetting + "\n" + "Strength: " + signalStrength;
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
}
