package tconstruct.client.block;

import org.lwjgl.opengl.GL11;

import tconstruct.blocks.logic.CastingChannelLogic;
import tconstruct.blocks.logic.SignalBusLogic;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class SignalBusRender implements ISimpleBlockRenderingHandler {
	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID,
			RenderBlocks renderer) {
		//Base
		renderer.setRenderBounds(0.375D, 0.0D, 0.375D, 0.625D, 0.2D, 0.625D);
		this.renderStandardBlock(block, metadata, renderer);
		//Extend Z-
		renderer.setRenderBounds(0.375D, 0.0D, 0.0D, 0.625D, 0.2D, 0.375D);
		this.renderStandardBlock(block, metadata, renderer);
		//Extend Z+
		renderer.setRenderBounds(0.375D, 0.0D, 0.625D, 0.625D, 0.2D, 1D);
		this.renderStandardBlock(block, metadata, renderer);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        if (modelId == renderID)
        {
            SignalBusLogic tile = (SignalBusLogic) world.getBlockTileEntity(x, y, z);

            //Base
            renderer.setRenderBounds(0.375D, 0.0D, 0.375D, 0.625D, 0.2D, 0.625D);
            renderer.renderStandardBlock(block, x, y, z);
            
            if (tile.isConnected(ForgeDirection.NORTH))
            {
                //Extend Z-
            	renderer.setRenderBounds(0.375D, 0.0D, 0.0D, 0.625D, 0.2D, 0.375D);
        		renderer.renderStandardBlock(block, x, y, z);
            }

            if (tile.isConnected(ForgeDirection.SOUTH))
            {
                //Extend Z+
        		renderer.setRenderBounds(0.375D, 0.0D, 0.625D, 0.625D, 0.2D, 1D);
        		renderer.renderStandardBlock(block, x, y, z);
            }

            if (tile.isConnected(ForgeDirection.WEST))
            {
                //Extend X-
            	renderer.setRenderBounds(0.0D, 0.0D, 0.375D, 0.375D, 0.2D, 0.625D);
        		renderer.renderStandardBlock(block, x, y, z);
            }

            if (tile.isConnected(ForgeDirection.EAST))
            {
                //Extend X+
            	renderer.setRenderBounds(0.625D, 0.0D, 0.375D, 1.0D, 0.2D, 0.625D);
        		renderer.renderStandardBlock(block, x, y, z);
            }

        }
        return true;
	}
    private void renderStandardBlock (Block block, int meta, RenderBlocks renderer)
    {
        Tessellator tessellator = Tessellator.instance;
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, -1.0F, 0.0F);
        renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, meta));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
        renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, meta));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, -1.0F);
        renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, meta));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, meta));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(-1.0F, 0.0F, 0.0F);
        renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, meta));
        tessellator.draw();
        tessellator.startDrawingQuads();
        tessellator.setNormal(1.0F, 0.0F, 0.0F);
        renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, meta));
        tessellator.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    }

    @Override
    public boolean shouldRender3DInInventory ()
    {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public int getRenderId ()
    {
        return renderID;
    }
}
