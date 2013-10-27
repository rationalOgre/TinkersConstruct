package tconstruct.blocks.logic;

import tconstruct.blocks.component.SignalBusMasterLogic;
import tconstruct.library.multiblock.MultiblockMasterBaseLogic;
import tconstruct.library.util.CoordTuple;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class SignalTerminalLogic extends TileEntity {

	private boolean[] connectedSides = null;
	private CoordTuple signalBus = null;
	private boolean doUpdate = false;
	private byte signalSetting = 0;
	private byte signalStrength = 0;
	
	public SignalTerminalLogic() {
		connectedSides = new boolean[] { false, false, false, false, false, false };
	}
	
	public byte getSignal(byte signal) {
		if (signal != signalSetting) {
			return 0;
		}
		
		return signalStrength;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		
        int tX = data.getInteger("BusX");
        int tY = data.getInteger("BusY");
        int tZ = data.getInteger("BusZ");
        
        signalBus = new CoordTuple(tX, tY, tZ);
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		
		if (signalBus != null) {
	        data.setInteger("BusX", signalBus.x);
	        data.setInteger("BusY", signalBus.y);
	        data.setInteger("BusZ", signalBus.z);
		}
		else {
			data.setInteger("BusX", 0);
			data.setInteger("BusY", 0);
			data.setInteger("BusZ", 0);
		}
	}

	@Override
	public void updateEntity() {
		if (!doUpdate) { return; }
		if (signalBus == null) { return; }
		if (worldObj.isRemote) { return; }
		TileEntity te = worldObj.getBlockTileEntity(signalBus.x, signalBus.y, signalBus.z);
		if (te instanceof SignalBusLogic) {
			MultiblockMasterBaseLogic master = ((SignalBusLogic)te).getMultiblockMaster();
			if (master instanceof SignalBusMasterLogic) {
				((SignalBusMasterLogic)master).updateSignal(new CoordTuple(xCoord, yCoord, zCoord), signalSetting, signalStrength);
			}
		}
		
		doUpdate = false;
		
		return;
	}
	
	public void reportToMaster(SignalBusMasterLogic master) {
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		
		master.updateSignal(new CoordTuple(xCoord, yCoord, zCoord), signalSetting, signalStrength);
		
		return;
	}
	
	public void receiveSignal(int strength) {
		signalStrength = (byte)strength;
		doUpdate = true;
	}
	
	public void receiveSignals(byte[] signals) {
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		
		if (signals[(byte)signalSetting] > 0) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (meta & 14) | 1, 1);
		}
		else {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, (meta & 14), 1);
		}
	}

	public void setBusCoords(World world, int x, int y, int z) {
		if (world.provider.dimensionId == worldObj.provider.dimensionId && !world.isRemote && !worldObj.isRemote) {
			signalBus = new CoordTuple(x, y, z);
		}
		doUpdate = true;
	}
	
	public int isProvidingStrongPower(int meta, int side) {
		
		
		return meta;
	}
}
