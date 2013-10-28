package tconstruct.blocks.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tconstruct.blocks.logic.SignalBusLogic;
import tconstruct.library.multiblock.IMultiblockMember;
import tconstruct.library.multiblock.MultiblockMasterBaseLogic;
import tconstruct.library.util.CoordTuple;

public class SignalBusMasterLogic extends MultiblockMasterBaseLogic
{
    private byte[] signals = new byte[16];
    private HashMap<Byte, CoordTuple> highSignal = new HashMap<Byte, CoordTuple>();
    //	private HashMap<CoordTuple, Byte> sources = new HashMap<CoordTuple, Byte>();
    //	private HashMap<CoordTuple, Byte> receivers = new HashMap<CoordTuple, Byte>();

    private ArrayList<CoordTuple> tetheredBuses = new ArrayList<CoordTuple>();

    public SignalBusMasterLogic(World world)
    {
        super(world);

        for (int i = 0; i < 16; i++)
        {
            signals[i] = 0;
        }
    }

    public byte[] getSignals ()
    {
        return signals.clone();
    }

    @Override
    public boolean doUpdate ()
    {

        return false;
    }

    public void updateSignal (CoordTuple source, int channel, int strength)
    {
        if (worldObj.isRemote)
        {
            return;
        }

        if (!(tetheredBuses.contains(source)))
        {
            tetheredBuses.add(new CoordTuple(source));
        }

        if (highSignal.containsKey(((byte) channel)))
        {
            if (!(highSignal.get((byte) channel) instanceof CoordTuple))
            {
                highSignal.remove((byte) channel);
            }
        }
        if (highSignal.containsKey((byte) channel) && highSignal.get((byte) channel).equals(source))
        {
            highSignal.remove((byte) channel);
            int newStrength = 0;
            CoordTuple newHighCoords = null;
            TileEntity te = null;
            for (CoordTuple src : tetheredBuses)
            {
                te = worldObj.getBlockTileEntity(src.x, src.y, src.z);
                if (te instanceof SignalBusLogic)
                {
                    int srcStrength = ((SignalBusLogic) te).getSignal((byte) channel);
                    if (srcStrength > newStrength)
                    {
                        newStrength = srcStrength;
                        newHighCoords = new CoordTuple(src);
                    }
                }
                else
                {
                    tetheredBuses.remove(src);
                }

            }
            if (newStrength > 0)
            {
                signals[channel] = (byte) newStrength;
                highSignal.put((byte) channel, newHighCoords);
            }
            else
            {
                signals[channel] = 0;
            }
        }

        if (signals[channel] < strength)
        {
            signals[channel] = (byte) strength;

            if (highSignal.containsKey((byte) channel))
            {
                highSignal.remove((byte) channel);
            }
            highSignal.put((byte) channel, new CoordTuple(source));
        }

    }

    @Override
    protected void onBlockAdded (IMultiblockMember newMember)
    {
        // Nothing important at the moment
    }

    @Override
    protected void onBlockRemoved (IMultiblockMember oldMember)
    {
        // Nothing important at the moment
    }

    @Override
    protected void onDataMerge (MultiblockMasterBaseLogic newMaster)
    {
        // Nothing important at the moment
    }

    @Override
    public void writeToNBT (NBTTagCompound data)
    {
        // Nothing important at the moment
    }

    @Override
    public void readFromNBT (NBTTagCompound data)
    {
        // Nothing important at the moment
    }

    @Override
    public void formatDescriptionPacket (NBTTagCompound data)
    {
        // Nothing important at the moment
    }

    @Override
    public void decodeDescriptionPacket (NBTTagCompound data)
    {
        // Nothing important at the moment
    }

    @Override
    public String debugString ()
    {
        String fromSuper = super.debugString();

        if (worldObj.isRemote)
        {
            return fromSuper;
        }

        String tstring = "Tethered Buses: " + tetheredBuses.size() + "\n Signals: [";
        for (int n = 0; n < 16; n++)
        {
            tstring += n + ":" + signals[n];

            if (n != 15)
                tstring += ", ";
        }
        tstring += "]";

        return fromSuper + "\n" + tstring;
    }

}
