package tconstruct.blocks;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import tconstruct.TConstruct;
import tconstruct.blocks.logic.SignalBusLogic;
import tconstruct.blocks.logic.SignalTerminalLogic;
import tconstruct.client.block.SignalBusRender;
import tconstruct.client.block.SignalTerminalRender;
import tconstruct.library.TConstructRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Direction;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ForgeDirection.*;

public class SignalTerminal extends Block implements ITileEntityProvider {
    public Icon[] icons;
    public String[] textureNames = new String[] { "signalbus" };

	public SignalTerminal(int par1) {
		super(par1, Material.circuits);
        this.setHardness(0.1F);
        this.setResistance(1);
        this.setStepSound(soundMetalFootstep);
        setCreativeTab(TConstructRegistry.blockTab);
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

	/**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
    {
        return null;
    }

    /**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
    public boolean isOpaqueCube()
    {
        return false;
    }

    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
    public boolean renderAsNormalBlock()
    {
        return false;
    }
    
    /**
     * The type of render function that is called for this block
     */
    @Override
    public int getRenderType ()
    {
        return SignalTerminalRender.renderID;
    }

    /**
     * How many world ticks before ticking
     */
    public int tickRate(World par1World)
    {
        return 2;
    }

    /**
     * checks to see if you can place this block can be placed on that side of a block: BlockLever overrides
     */
    public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side)
    {
        ForgeDirection dir = ForgeDirection.getOrientation(side);
        return (dir == ForgeDirection.NORTH && world.isBlockSolidOnSide(x, y, z + 1, ForgeDirection.NORTH)) ||
               (dir == ForgeDirection.SOUTH && world.isBlockSolidOnSide(x, y, z - 1, ForgeDirection.SOUTH)) ||
               (dir == ForgeDirection.WEST  && world.isBlockSolidOnSide(x + 1, y, z, ForgeDirection.WEST )) ||
               (dir == ForgeDirection.EAST  && world.isBlockSolidOnSide(x - 1, y, z, ForgeDirection.EAST )) ||
               (dir == ForgeDirection.UP    && world.isBlockSolidOnSide(x, y + 1, z, ForgeDirection.UP   )) ||
               (dir == ForgeDirection.DOWN  && world.isBlockSolidOnSide(x, y - 1, z, ForgeDirection.DOWN ));
    }

    /**
     * Checks to see if its valid to put this block at the specified coordinates. Args: world, x, y, z
     */
    public boolean canPlaceBlockAt(World world, int x, int y, int z)
    {
        return world.isBlockSolidOnSide(x - 1, y, z, ForgeDirection.EAST ) ||
               world.isBlockSolidOnSide(x + 1, y, z, ForgeDirection.WEST ) ||
               world.isBlockSolidOnSide(x, y, z - 1, ForgeDirection.SOUTH) ||
               world.isBlockSolidOnSide(x, y, z + 1, ForgeDirection.NORTH) ||
               world.isBlockSolidOnSide(x, y - 1, z, ForgeDirection.UP)    ||
               world.isBlockSolidOnSide(x, y + 1, z, ForgeDirection.DOWN);
    }

     /**
     * Updates the blocks bounds based on its current state. Args: world, x, y, z
     */
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
    {
    	/*
    	 *   BITS | Orientation | Signal |  HEX | DEC
    	 * ===========================================
    	 * 000000 | WEST     X- |        | 0x00 |   0
    	 * 000001 | WEST     X- |        | 0x01 |   1 
    	 * 000010 | EAST     X+ |        | 0x02 |   2
    	 * 000011 | EAST     X+ |        | 0x03 |   3
    	 * 000100 | NORTH    Z- |        | 0x04 |   4
    	 * 000101 | NORTH    Z- |        | 0x05 |   5
    	 * 000110 | SOUTH    Z+ |        | 0x06 |   6
    	 * 000111 | SOUTH    Z+ |        | 0x07 |   7
    	 * 001000 | DOWN     Y- |        | 0x08 |   8
    	 * 001001 | DOWN     Y- |        | 0x09 |   9
    	 * 001010 | UP       Y+ |        | 0x0a |  10
    	 * 001011 | UP       Y+ |        | 0x0b |  11
    	 * 
    	 */
    	
    	/*
    	 *   BITS | Signal |  HEX | DEC
    	 * =============================
    	 * 0000 |      0 | 0x00 |   0
    	 * 0001 |      1 | 0x01 |   1 
    	 * 0010 |      2 | 0x02 |   2
    	 * 0011 |      3 | 0x03 |   3
    	 * 0100 |      4 | 0x04 |   4
    	 * 0101 |      5 | 0x05 |   5
    	 * 0110 |      6 | 0x06 |   6
    	 * 0111 |      7 | 0x07 |   7
    	 * 1000 |      8 | 0x08 |   8
    	 * 1001 |      9 | 0x09 |   9
    	 * 1010 |     10 | 0x0a |  10
    	 * 1011 |     11 | 0x0b |  11
    	 * 1100 |     12 | 0x0c |  12
    	 * 1101 |     13 | 0x0d |  13
    	 * 1110 |     14 | 0x0e |  14
    	 * 1111 |     15 | 0x0f |  15
		 *
    	 */
    	
    	
        int orientation = world.getBlockMetadata(x, y, z) >> 1;
        float f = 0.1875F;

        if (orientation == 0)
        {
        	// X-
            this.setBlockBounds(0.0F, 0.25F, 0.25F, 0.75F, 0.75F, 0.75F);
        }
        else if (orientation == 1)
        {
        	// X+
            this.setBlockBounds(0.25F, 0.25F, 0.25F, 1.0F, 0.75F, 0.75F);
        }
        else if (orientation == 2)
        {
        	// Z-
            this.setBlockBounds(0.25F, 0.25F, 0.0F, 0.75F, 0.75F, 0.75F);
        }
        else if (orientation == 3)
        {
        	// Z+
            this.setBlockBounds(0.25F, 0.25F, 0.25F, 0.75F, 0.75F, 1.0F);
        }
        else if (orientation == 4)
        {
        	// Y-
            this.setBlockBounds(0.25F, 0.0F, 0.25F, 0.75F, 0.75F, 0.75F);
        }
        else if (orientation == 5)
        {
        	// Y+
            this.setBlockBounds(0.25F, 0.25F, 0.25F, 0.75F, 1.0F, 0.75F);
        }
    }

    /**
     * Returns true if the block is emitting indirect/weak redstone power on the specified side. If isBlockNormalCube
     * returns true, standard redstone propagation rules will apply instead and this will not be called. Args: World, X,
     * Y, Z, side. Note that the side is reversed - eg it is 1 (up) when checking the bottom of the block.
     */
    public int isProvidingWeakPower(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
    {
    	return par1IBlockAccess.getBlockMetadata(par2, par3, par4) & 15;
    }

    /**
     * Returns true if the block is emitting direct/strong redstone power on the specified side. Args: World, X, Y, Z,
     * side. Note that the side is reversed - eg it is 1 (up) when checking the bottom of the block.
     */
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int localSide)
    {
    	int meta = world.getBlockMetadata(x, y, z);
    	TileEntity te = world.getBlockTileEntity(x, y, z);
    	if (!(te instanceof SignalTerminalLogic)) { return meta & 15; } // Let's just say yes since we don't know better; let's revisit this later as well
    	
    	return ((SignalTerminalLogic)te).isProvidingStrongPower(meta, localSide);
    }

    /**
     * Can this block provide power. Only wire currently seems to have this change based on its state.
     */
    public boolean canProvidePower()
    {
        return true;
    }

	@Override
	public TileEntity createNewTileEntity(World world) {
		return new SignalTerminalLogic();
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborID) {
		super.onNeighborBlockChange(world, x, y, z, neighborID);
		
        int meta = world.getBlockMetadata(x, y, z);
        boolean flag = world.isBlockIndirectlyGettingPowered(x, y, z);
        boolean flag1 = (meta & 1) == 1;

        SignalTerminalLogic logic = (SignalTerminalLogic)world.getBlockTileEntity(x, y, z);
        
        logic.receiveSignal((flag && !flag1) ? 15 : 0);
        
	}

	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
		int meta = par1World.getBlockMetadata(par2, par3, par4);
		
		TConstruct.logger.info("meta: " + meta);
		
		return false;
	}
	
	
}

