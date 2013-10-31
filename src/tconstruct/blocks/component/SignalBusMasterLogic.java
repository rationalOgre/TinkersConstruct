package tconstruct.blocks.component;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tconstruct.blocks.logic.SignalBusLogic;
import tconstruct.library.multiblock.IMultiblockMember;
import tconstruct.library.multiblock.MultiblockMasterBaseLogic;
import tconstruct.library.util.CoordTuple;

public class SignalBusMasterLogic extends MultiblockMasterBaseLogic
{
    private boolean forceUpdate = false;
    private byte[] signals = new byte[16];
    private CoordTuple[] signalProviderCoords = new CoordTuple[16];

    private List<CoordTuple> tetheredBuses = new LinkedList<CoordTuple>(); // Buses that contain linked Terminals

    public SignalBusMasterLogic(World world)
    {
        super(world);

        for (int i = 0; i < 16; i++)
        {
            signals[i] = 0;
        }
    }

    @Override
    public boolean doUpdate ()
    {
        if (worldObj.isRemote || !forceUpdate) {
            return false;
        }
        
        forceUpdate = false;

        for (int i = 0; i < 16; i++) {
            updateSignal(i);
        }
        
        sendUpdates();

        return true;
    }

    public byte[] getSignals ()
    {
        return signals.clone();
    }

    public void updateSignal (CoordTuple source, int channel, int strength)
    {
        if (worldObj.isRemote || channel < 0 || channel >= 16 || strength < 0 || strength >= 16)
        {
            return;
        }

//        if (SignalBusLogic.hasTerminals(worldObj, source.x, source.y, source.z) && connectedBlocks.contains(source))
//        {
//            if (!(tetheredBuses.contains(source)))
//            {
//                tetheredBuses.add(new CoordTuple(source));
//                forceUpdate = true;
//            }
//        }
//        else
//        {
//            if (tetheredBuses.contains(source))
//            {
//                tetheredBuses.remove(source);
//                forceUpdate = true;
//            }
//        }
//        
        if (source.equals(signalProviderCoords[channel]) && signals[channel] > strength) {
            // Our highest provider is bailing on us. Time to find a new one
            updateSignal(channel);
            forceUpdate = true;
        } else if (strength > signals[channel])
        {
            signals[channel] = (byte) strength;

            signalProviderCoords[channel] = new CoordTuple(source);
            
            forceUpdate = true;
        }
    }
    
    public void updateSignals (CoordTuple source, byte[] signals) {
        if (signals.length != 16) {
            return;
        }
        
        if (!(tetheredBuses.contains(source))) {
            tetheredBuses.add(source);
        }
        
        for (int i = 0; i < 16; i++) {
            updateSignal(source, i, signals[i]);
        }
    }
    
    public void updateSignal (int channel) {
        int highPower = 0;
        CoordTuple highCoords = null;
        
        TileEntity te = null;
        int tPower = 0;
        for (CoordTuple bus : tetheredBuses) {
            if (worldObj.getChunkProvider().chunkExists(bus.x >> 4, bus.z >> 4)) {
                te = worldObj.getBlockTileEntity(bus.x, bus.y, bus.z);
                if (te == null || !(te instanceof SignalBusLogic)) {
                    // Perhaps not the best exception choice, but we can revisit this later
                    throw new NullPointerException();
                }
                
                tPower = ((SignalBusLogic)te).getLocalSignals()[channel];
                
                if (tPower > highPower) {
                    highPower = tPower;
                    highCoords = new CoordTuple(bus);
                }
            }
        }
        
        if (highPower == 0 || highCoords == null) {
            signals[channel] = 0;
            signalProviderCoords[channel] = null;
        } else {
            signals[channel] = (byte)highPower;
            signalProviderCoords[channel] = highCoords;
        }
        
    }
    
    public void sendUpdates () {
        TileEntity te = null;
        for (CoordTuple bus : tetheredBuses) {
            te = worldObj.getBlockTileEntity(bus.x, bus.y, bus.z);
            if (te instanceof SignalBusLogic) {
                ((SignalBusLogic)te).sendUpdates(signals);
            }
        }
    }
    
    public void recalculateBus (SignalBusLogic bus) {
        CoordTuple busCoords = new CoordTuple (bus.xCoord, bus.yCoord, bus.zCoord);
        
        if (!tetheredBuses.contains(bus) && SignalBusLogic.hasTerminals(worldObj, bus.xCoord, bus.yCoord, bus.zCoord))
        {
            tetheredBuses.add(busCoords);
        }
        updateSignals(busCoords, bus.getLocalSignals());
        sendUpdates();
        forceUpdate = true;
    }

    @Override
    protected void onBlockAdded (IMultiblockMember newMember)
    {
        CoordTuple coords = newMember.getCoordInWorld();

        if (!tetheredBuses.contains(newMember.getCoordInWorld()) && SignalBusLogic.hasTerminals(worldObj, coords.x, coords.y, coords.z))
        {
            tetheredBuses.add(newMember.getCoordInWorld());
            forceUpdate = true;
        }
    }

    @Override
    protected void onBlockRemoved (IMultiblockMember oldMember)
    {
        if (tetheredBuses.contains(oldMember.getCoordInWorld()))
        {
            tetheredBuses.remove(oldMember.getCoordInWorld());
            forceUpdate = true;
        }
    }

    @Override
    protected void onDataMerge (MultiblockMasterBaseLogic newMaster)
    {
        byte[] newMasterSignals = ((SignalBusMasterLogic) newMaster).getSignals();

        if (tetheredBuses.size() > 0)
        {
            ((SignalBusMasterLogic) newMaster).mergeTethered(tetheredBuses);
        }

        for (int n = 0; n < 16; n++)
        {
            if (signals[n] > newMasterSignals[n])
            {
                ((SignalBusMasterLogic) newMaster).updateSignal(signalProviderCoords[n], n, signals[n]);
            }
        }
    }

    protected void mergeTethered (List<CoordTuple> oldMasterTethered)
    {
        for (CoordTuple bus : oldMasterTethered)
        {
            if (!(tetheredBuses.contains(bus)) && SignalBusLogic.hasTerminals(worldObj, bus.x, bus.y, bus.z))
            {
                tetheredBuses.add(bus);
            }
        }
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
