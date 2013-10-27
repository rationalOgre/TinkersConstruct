package tconstruct.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.TConstruct;
import tconstruct.blocks.logic.CastingChannelLogic;
import tconstruct.blocks.logic.SignalBusLogic;
import tconstruct.client.block.BlockRenderCastingChannel;
import tconstruct.client.block.SignalBusRender;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.multiblock.IMultiblockMember;
import tconstruct.library.multiblock.MultiblockMasterBaseLogic;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class SignalBus extends Block implements ITileEntityProvider {
    public Icon[] icons;
    public String[] textureNames = new String[] { "signalbus" };

	public SignalBus(int par1) {
		super(par1, Material.circuits);
        this.setHardness(0.1F);
        this.setResistance(1);
        this.setStepSound(soundMetalFootstep);
        setCreativeTab(TConstructRegistry.blockTab);
	}
	
	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
		if (par1World.isRemote) { return false; } 
		TileEntity te = par1World.getBlockTileEntity(par2, par3, par4);
		if (te != null && te instanceof SignalBusLogic) {
			TConstruct.logger.info(((SignalBusLogic)te).debugString());
			if (((SignalBusLogic)te).getMultiblockMaster() != null) {
				TConstruct.logger.info((((SignalBusLogic)te).getMultiblockMaster().debugString()));
			}
		}
		
		return false;
	}

	@Override
	public void onNeighborTileChange(World world, int x, int y, int z, int tileX, int tileY, int tileZ) {
		TileEntity te = world.getBlockTileEntity(tileX, tileY, tileZ);
		if (te != null && te instanceof SignalBusLogic) {
			if (((SignalBusLogic)te).getMultiblockMaster() != null) {
				((SignalBusLogic)te).getMultiblockMaster().detachBlock((IMultiblockMember)te, false);
			}
		}

	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		TileEntity te = world.getBlockTileEntity(x, y, z);
		if (te != null && te instanceof SignalBusLogic) {
			((SignalBusLogic)te).onBlockAdded(world, x, y, z);
		}
	}

    @Override
    @SideOnly(Side.CLIENT)
    public Icon getIcon (int side, int metadata)
    {
        return icons[0];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons (IconRegister iconRegister)
    {
        this.icons = new Icon[textureNames.length];

        for (int i = 0; i < this.icons.length; ++i)
        {
            this.icons[i] = iconRegister.registerIcon("tinker:" + textureNames[i]);
        }
    }

    @Override
    public void setBlockBoundsBasedOnState (IBlockAccess world, int x, int y, int z)
    {
        SignalBusLogic tile = (SignalBusLogic) world.getBlockTileEntity(x, y, z);
        if (!(tile instanceof SignalBusLogic)) return;
        float minX = 0.375F;
        float maxX = 0.625F;
        float minZ = 0.375F;
        float maxZ = 0.625F;
        if (tile.isConnected(ForgeDirection.NORTH))
            minZ = 0F;
        if (tile.isConnected(ForgeDirection.SOUTH))
            maxZ = 1F;
        if (tile.isConnected(ForgeDirection.WEST))
            minX = 0F;
        if (tile.isConnected(ForgeDirection.EAST))
            maxX = 1F;

        this.setBlockBounds(minX, 0.0F, minZ, maxX, 0.2F, maxZ);
    }

    @Override
    public boolean renderAsNormalBlock ()
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube ()
    {
        return false;
    }

    @Override
    public int getRenderType ()
    {
        return SignalBusRender.renderID;
    }

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		return new SignalBusLogic();
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new SignalBusLogic();
	}
   
}
