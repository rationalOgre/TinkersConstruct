package tconstruct.blocks.logic;

import tconstruct.blocks.component.SignalBusMasterLogic;
import tconstruct.library.multiblock.MultiblockMasterBaseLogic;
import tconstruct.library.util.CoordTuple;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class SignalTerminalLogic extends TileEntity {

	private boolean[] connectedSides = null;
	private int pendingSide = -1;
	private CoordTuple signalBus = null;
	private boolean doUpdate = false;
	private byte signalSetting = 0;
	private byte signalStrength = 0;
	private boolean newTile = true;
	
	public SignalTerminalLogic() {
		super();
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
		
		signalSetting = data.getByte("signalSetting");
		signalStrength = data.getByte("signalStrength");
		
		byte tbyte = data.getByte("connectedSides");
		for (int n = 0; n < 6; n++) {
			if (((tbyte >> n) & 1) == 0) {
				connectedSides[n] = true;
			}
		}
		
        int tX = data.getInteger("BusX");
        int tY = data.getInteger("BusY");
        int tZ = data.getInteger("BusZ");
        
        signalBus = new CoordTuple(tX, tY, tZ);
        
        newTile = false;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		
		data.setByte("signalSetting", signalSetting);
		data.setByte("signalStrength", signalStrength);
		
		byte tbyte = (byte)0;
		for (int n = 0; n < 6; n++) {
			if (connectedSides[n]) {
				tbyte &= (1 << n);
			}
		}
		data.setByte("connectedSides", tbyte);
		
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
		if (pendingSide >= 0 && pendingSide < 6) {
			connectedSides[pendingSide] = true;
			pendingSide = -1;
		}
		
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
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, signalStrength, 1);
		}
		else {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1);
		}
	}

	public void setBusCoords(World world, int x, int y, int z) {
		if (world.provider.dimensionId == worldObj.provider.dimensionId && !world.isRemote && !worldObj.isRemote) {
			signalBus = new CoordTuple(x, y, z);
		}
		doUpdate = true;
	}
	
	public int isProvidingStrongPower(int meta, int side) {
		if (side >= 0 && side < 6) {
			if (connectedSides[side]) { return meta; }
		}
		
		return 0;
	}

	public void addPendingSide(int side) {
		pendingSide = side;
	}

	public void connectPending() {
		if (newTile) {
			int side = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
			connectedSides[side] = true;
			newTile = false;
		}
		else {
			if (pendingSide >= 0 && pendingSide < 6) {
				connectedSides[pendingSide] = true;
			}
			pendingSide = -1;
		}
	}

	public boolean[] getConnectedSides() {
		return connectedSides.clone();
	}
	
	
}
