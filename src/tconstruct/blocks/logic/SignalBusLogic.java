package tconstruct.blocks.logic;

import java.util.LinkedList;
import java.util.List;

import javax.swing.text.MaskFormatter;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;
import tconstruct.blocks.component.SignalBusMasterLogic;
import tconstruct.library.multiblock.IMultiblockMember;
import tconstruct.library.multiblock.MultiblockBaseLogic;
import tconstruct.library.multiblock.MultiblockMasterBaseLogic;
import tconstruct.library.util.CoordTuple;
import tconstruct.library.util.IActiveLogic;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SignalBusLogic extends MultiblockBaseLogic implements IActiveLogic
{
    private int ticks = 0;
    private List<CoordTuple> terminals = new LinkedList<CoordTuple>();
    private byte[] localHighSignals = new byte[16]; 
    private byte[] cachedReceivedSignals = new byte[16];

    public SignalBusLogic()
    {

    }

    @Override
    public boolean canUpdate ()
    {
        return true;
    }

    @Override
    public void updateEntity ()
    {
        if (worldObj.isRemote)
        {
            return;
        }
        if (!this.isConnected())
        {
            return;
        }
        
//        if (++ticks % 20 == 0) {
//            
//            byte[] signals = ((SignalBusMasterLogic) this.getMultiblockMaster()).getSignals();
//            List<CoordTuple> remove = new LinkedList<CoordTuple>();
//            TileEntity te = null;
//            for (CoordTuple term : terminals)
//            {
//                te = worldObj.getBlockTileEntity(term.x, term.y, term.z);
//                if (te instanceof SignalTerminalLogic)
//                {
//                    ((SignalTerminalLogic) te).receiveSignals(signals);
//                }
//                else
//                {
//                    remove.add(term);
//                }
//            }
//    
//            if (remove.size() > 0)
//            {
//                for (CoordTuple term : remove)
//                {
//                    terminals.remove(term);
//                }
//            }
//        }
            
//        if (ticks >= 20) {
//            ticks = 0;
//        }
    }

    public boolean registerTerminal (World world, int x, int y, int z)
    {
        if (worldObj.isRemote)
        {
            return false;
        }
        if (world == worldObj && world.isRemote == worldObj.isRemote)
        {
            if (worldObj.getBlockTileEntity(x, y, z) instanceof SignalTerminalLogic)
            {
                CoordTuple newTerm = new CoordTuple(x, y, z);
                if (!terminals.contains(newTerm)) {
                    terminals.add(newTerm);
                    ((SignalTerminalLogic) world.getBlockTileEntity(x, y, z)).setBusCoords(world, xCoord, yCoord, zCoord);
                    ((SignalBusMasterLogic) this.getMultiblockMaster()).recalculateBus(this);
                }
                return true;
            }
        }

        return false;
    }

    public boolean isRegisteredTerminal (World world, int x, int y, int z)
    {
        if (worldObj.isRemote)
        {
            return false;
        }
        return terminals.contains(new CoordTuple(x, y, z));
    }

    public boolean unregisterTerminal (World world, int x, int y, int z)
    {
        if (worldObj.isRemote)
        {
            return false;
        }
        if (terminals.remove(new CoordTuple(x, y, z))) {
            fullUpdateLocalSignals();
            ((SignalBusMasterLogic)this.getMultiblockMaster()).recalculateBus(this);
            return true;
        }
        return false;
    }

    public boolean hasTerminals ()
    {
        return (terminals.size() > 0);
    }

    public static boolean hasTerminals (World world, int x, int y, int z)
    {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te != null && te instanceof SignalBusLogic)
        {
            return ((SignalBusLogic) te).hasTerminals();
        }

        return false;
    }

    @Override
    public boolean isCompatible (Object other)
    {
        return (other.getClass() == this.getClass());
    }

    @Override
    public void readFromNBT (NBTTagCompound tags)
    {
        super.readFromNBT(tags);
        readCustomNBT(tags);
    }

    public void readCustomNBT (NBTTagCompound tags)
    {

    }

    @Override
    public void writeToNBT (NBTTagCompound tags)
    {
        super.writeToNBT(tags);
        writeCustomNBT(tags);
    }

    public void writeCustomNBT (NBTTagCompound tags)
    {

    }

    @Override
    public Packet getDescriptionPacket ()
    {
        NBTTagCompound tag = new NBTTagCompound();
        writeCustomNBT(tag);
        return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, tag);
    }

    @Override
    public void onDataPacket (INetworkManager net, Packet132TileEntityData packet)
    {
        readCustomNBT(packet.data);
        this.worldObj.markBlockForRenderUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public boolean getActive ()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setActive (boolean flag)
    {
        // TODO Auto-generated method stub

    }

    public boolean isConnected (ForgeDirection dir)
    {
        switch (dir)
        {
        case DOWN:
            return (this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord - 1, this.zCoord) instanceof SignalBusLogic);
        case NORTH:
            return (this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord - 1) instanceof SignalBusLogic);
        case SOUTH:
            return (this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord + 1) instanceof SignalBusLogic);
        case WEST:
            return (this.worldObj.getBlockTileEntity(this.xCoord - 1, this.yCoord, this.zCoord) instanceof SignalBusLogic);
        case EAST:
            return (this.worldObj.getBlockTileEntity(this.xCoord + 1, this.yCoord, this.zCoord) instanceof SignalBusLogic);
        default:
            return false;
        }
    }

    @Override
    public MultiblockMasterBaseLogic getNewMultiblockMasterObject ()
    {
        return new SignalBusMasterLogic(this.worldObj);
    }

    public String debugString ()
    {
        return "Connected: " + terminals.size();
    }

    public byte[] getLocalSignals () {
        return localHighSignals;
    }
        
//    public void updateLocalSignals (byte[] signals, boolean sendNotify) {
//        
//        for (int i = 0; i < 16; i++) {
//            localHighSignals[i] = signals[i] > localHighSignals[i] ? signals[i] : localHighSignals[i];
//        }
//        
//        if (sendNotify) {
//            ((SignalBusMasterLogic)getMultiblockMaster()).updateSignals(getCoordInWorld(), localHighSignals);
//        }
//    }
//        
    public void fullUpdateLocalSignals () {
        localHighSignals = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        
        byte[] termSignals = null;
        
        TileEntity te = null;
        for (CoordTuple term : terminals) {
            if (!worldObj.getChunkProvider().chunkExists(term.x >> 4, term.z >> 4)) {
                continue;
            }
            te = worldObj.getBlockTileEntity(term.x, term.y, term.z);
            if (te instanceof SignalTerminalLogic) {
                termSignals = ((SignalTerminalLogic)te).getReceivedSignals();
                
                for (int i = 0; i < 16; i++) {
                    localHighSignals[i] = (byte)Math.max(termSignals[i], localHighSignals[i]);
                }
            }
        }
        
        ((SignalBusMasterLogic)getMultiblockMaster()).updateSignals(getCoordInWorld(), localHighSignals);
    }

    public void sendUpdates (byte[] signals)
    {
        cachedReceivedSignals = signals.clone();
        TileEntity te = null;
        
        for (CoordTuple term : terminals) {
            if (!worldObj.getChunkProvider().chunkExists(term.x >> 4, term.z >> 4)) {
                continue;
            }
            te = worldObj.getBlockTileEntity(term.x, term.y, term.z);
            if (te instanceof SignalTerminalLogic) {
                ((SignalTerminalLogic)te).receiveSignals(signals);
            }
        }
    }
}
