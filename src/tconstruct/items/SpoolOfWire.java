package tconstruct.items;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.blocks.logic.SignalBusLogic;
import tconstruct.blocks.logic.SignalTerminalLogic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class SpoolOfWire extends CraftingItem {

	public SpoolOfWire(int id) {
		super(id, new String[] { "spoolWire" }, new String[] { "spoolWire" }, "logic/");
		this.maxStackSize = 1;
	}
	
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation (ItemStack stack, EntityPlayer player, List list, boolean par4)
    {
        switch (stack.getItemDamage())
        {
        case 0:
            list.add("Wirey!");
            break;
        }
    }

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		NBTTagCompound data = itemstack.stackTagCompound;
		NBTTagCompound spoolData = null;
		
		if (world.isRemote) {
			return false;
		}
		
		if (data == null) {
			data = new NBTTagCompound();
			itemstack.stackTagCompound = data;
		}
		if (te != null && te instanceof SignalBusLogic) {
			if (data.hasKey("spoolWireData")) {
				spoolData = data.getCompoundTag("spoolWireData");
				
				int targetX = spoolData.getInteger("targetX");
				int targetY = spoolData.getInteger("targetY");
				int targetZ = spoolData.getInteger("targetZ");
				
				((SignalBusLogic)te).registerTerminal(world, targetX, targetY, targetZ);
				data.removeTag("spoolWireData");
				
				return true;
			}
			
			
			return false;
		}
		if (te != null && te instanceof SignalTerminalLogic) {
			data = itemstack.stackTagCompound;
			spoolData = null;
			if (data.hasKey("spoolWireData")) {
				data.removeTag("spoolWireData");
			}
			spoolData = new NBTTagCompound();
			
			spoolData.setInteger("targetX", x);
			spoolData.setInteger("targetY", y);
			spoolData.setInteger("targetZ", z);
			
			data.setCompoundTag("spoolWireData", spoolData);
			
			return true;
		}
		
		return false;		
	}

}
