package tconstruct.blocks.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import tconstruct.blocks.logic.SignalTerminalLogic;
import tconstruct.library.multiblock.IMultiblockMember;
import tconstruct.library.multiblock.MultiblockMasterBaseLogic;
import tconstruct.library.util.CoordTuple;

public class SignalBusMasterLogic extends MultiblockMasterBaseLogic {
	private byte[] signals = new byte[16];
	private HashMap<Byte, CoordTuple> highSignal = new HashMap<Byte, CoordTuple>();
	private HashMap<CoordTuple, Byte> sources = new HashMap<CoordTuple, Byte>();

	public SignalBusMasterLogic(World world) {
		super(world);
		
		for (int i = 0; i < 16; i++) {
			signals[i] = 0;
		}
	}

	public byte[] getSignals() {
		return signals.clone();
	}
	
	@Override
	public boolean doUpdate() {
		
		
		return false;
	}
	
	public void updateSignal(CoordTuple source, int signal, int value) {
		if (value <= 0) {
			if (highSignal.containsKey((byte)signal) && highSignal.get((byte)signal).equals(source)) {
				highSignal.remove((byte)signal);
				int newSignal = 0;
				CoordTuple newHighCoords = null;
				TileEntity te = null;
				SignalTerminalLogic term = null;
				for (CoordTuple src : sources.keySet()) {
					if (sources.get(src) == signal) {
						te = worldObj.getBlockTileEntity(src.x, src.y, src.z);
						if (te instanceof SignalTerminalLogic) {
							int srcSignal = ((SignalTerminalLogic)te).getSignal((byte)signal); 
							if (srcSignal > newSignal) {
								newSignal = srcSignal;
								newHighCoords = new CoordTuple(src.x, src.y, src.z);
							}
						}	
					}
				}
				signals[signal] = (byte)newSignal;
				highSignal.put((byte)signal, newHighCoords);
			}
			if (sources.containsKey(source)) {
				sources.remove(source);
			}
			
		}
		if (signals[signal] < value) {
			signals[signal] = (byte)value;
			
			if (highSignal.containsKey((byte)signal)) {
				highSignal.remove((byte)signal);
			}
			highSignal.put((byte)signal, new CoordTuple(source.x, source.y, source.z));
		}
		sources.put(new CoordTuple(source.x, source.y, source.z), (byte)signal);
		
	}

	@Override
	protected void onBlockAdded(IMultiblockMember newMember) {
		// Nothing important at the moment
	}

	@Override
	protected void onBlockRemoved(IMultiblockMember oldMember) {
		// Nothing important at the moment
	}

	@Override
	protected void onDataMerge(MultiblockMasterBaseLogic newMaster) {
		// Nothing important at the moment
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		// Nothing important at the moment
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		// Nothing important at the moment
	}

	@Override
	public void formatDescriptionPacket(NBTTagCompound data) {
		// Nothing important at the moment
	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		// Nothing important at the moment
	}
	
	@Override
	public String debugString() {
		String fromSuper = super.debugString();
		
		String tstring = "Sources: " + sources.size() + "\n Signals: [";
		for (int n = 0; n < 16; n++) {
			tstring += n + ":" + signals[n];
			
			if (n != 15) tstring += ", ";
		}
		tstring += "]";
		
		return fromSuper + "\n" + tstring;
	}

}
