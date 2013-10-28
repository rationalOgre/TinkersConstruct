package tconstruct.blocks.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	private List<CoordTuple> terminals = new ArrayList<CoordTuple>();

    public SignalBusLogic()
    {

    }
    
    

    @Override
	public boolean canUpdate() {
		return true;
	}



	@Override
	public void updateEntity() {
		if (!this.isConnected()) { return; }
		
		byte[] signals = ((SignalBusMasterLogic)this.getMultiblockMaster()).getSignals();
		
		TileEntity te = null;
		for (CoordTuple term : terminals) {
			te = worldObj.getBlockTileEntity(term.x, term.y, term.z);
			if (te instanceof SignalTerminalLogic) {
				((SignalTerminalLogic)te).receiveSignals(signals);
			}
		}
	}



	public boolean registerTerminal(World world, int x, int y, int z) {
    	if (world == worldObj && world.isRemote == worldObj.isRemote) {
    		if (worldObj.getBlockTileEntity(x, y, z) instanceof SignalTerminalLogic) {
    			terminals.add(new CoordTuple(x, y, z));
    			((SignalTerminalLogic)world.getBlockTileEntity(x, y, z)).setBusCoords(world, xCoord, yCoord, zCoord);
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public boolean isRegisteredTerminal(World world, int x, int y, int z) {
    	return terminals.contains(new CoordTuple(x, y, z));
    }
    
    public boolean unregisterTerminal(World world, int x, int y, int z) {
    	return terminals.remove(new CoordTuple(x, y, z));
    }
    
    public void doTerminalScan() {
    	TileEntity te = null;
    	SignalTerminalLogic tterm = null;
    	byte[] signals = ((SignalBusMasterLogic)this.getMultiblockMaster()).getSignals();
    	
    	for (CoordTuple term : terminals) {
    		te = worldObj.getBlockTileEntity(term.x, term.y, term.z);
    		if (te instanceof SignalTerminalLogic) {
    			((SignalTerminalLogic)te).receiveSignals(signals);
    		}
    		else {
    			terminals.remove(term);
    		}
    	}
    }

    @Override
	public boolean isCompatible(Object other) {
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
	public boolean getActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setActive(boolean flag) {
		// TODO Auto-generated method stub
		
	}

	public boolean isConnected(ForgeDirection dir) {
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
	public MultiblockMasterBaseLogic getNewMultiblockMasterObject() {
		return new SignalBusMasterLogic(this.worldObj);
	}
	
	public String debugString() {
		return "Connected: " + terminals.size();
	}
	
}
