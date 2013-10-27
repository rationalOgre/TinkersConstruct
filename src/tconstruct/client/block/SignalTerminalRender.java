package tconstruct.client.block;

import org.lwjgl.opengl.GL11;

import tconstruct.blocks.logic.SignalBusLogic;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class SignalTerminalRender  implements ISimpleBlockRenderingHandler {
	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	/*
	 *   BITS | Orientation | Signal |  HEX | DEC
	 * ===========================================
	 * 000000 |          X- | false  | 0x00 |   0
	 * 000001 |          X- | true   | 0x01 |   1 
	 * 000010 |          X+ | false  | 0x02 |   2
	 * 000011 |          X+ | true   | 0x03 |   3
	 * 000100 |          Z- | false  | 0x04 |   4
	 * 000101 |          Z- | true   | 0x05 |   5
	 * 000110 |          Z+ | false  | 0x06 |   6
	 * 000111 |          Z+ | true   | 0x07 |   7
	 * 001000 |          Y- | false  | 0x08 |   8
	 * 001001 |          Y- | true   | 0x09 |   9
	 * 001010 |          Y+ | false  | 0x0a |  10
	 * 001011 |          Y+ | true   | 0x0b |  11
	 * 
	 */
	
	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
		// Render X-
		renderer.setRenderBounds(0.0D, 0.25D, 0.25D, 0.2D, 0.75D, 0.75D);
		this.renderStandardBlock(block, metadata, renderer);
		
		renderer.setRenderBounds(0.2D, 0.375D, 0.375D, 0.75D, 0.625D, 0.625D);
		this.renderStandardBlock(block, metadata, renderer);
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
		int orientation = world.getBlockMetadata(x, y, z) >> 1;
				
        if (modelId == renderID)
        {
            //Base
            //renderer.setRenderBounds(0.375D, 0.0D, 0.375D, 0.625D, 0.2D, 0.625D);
            //renderer.renderStandardBlock(block, x, y, z);
            
            if (orientation == 0)
            {
                // Render X-
        		renderer.setRenderBounds(0.0D, 0.25D, 0.25D, 0.2D, 0.75D, 0.75D);
        		renderer.renderStandardBlock(block, x, y, z);
        		
        		renderer.setRenderBounds(0.2D, 0.375D, 0.375D, 0.75D, 0.625D, 0.625D);
        		renderer.renderStandardBlock(block, x, y, z);
            }
            else if (orientation == 1)
            {
                //Extend X+
        		renderer.setRenderBounds(0.8D, 0.25D, 0.25D, 1.0D, 0.75D, 0.75D);
        		renderer.renderStandardBlock(block, x, y, z);
        		
        		renderer.setRenderBounds(0.25D, 0.375D, 0.375D, 0.8D, 0.625D, 0.625D);
        		renderer.renderStandardBlock(block, x, y, z);

            }
            else if (orientation == 2)
            {
                //Extend Z-
        		renderer.setRenderBounds(0.25D, 0.25D, 0.0D, 0.75D, 0.75D, 0.2D);
        		renderer.renderStandardBlock(block, x, y, z);
        		
        		renderer.setRenderBounds(0.375D, 0.375D, 0.2D, 0.625D, 0.625D, 0.75D);
        		renderer.renderStandardBlock(block, x, y, z);

            }
            else if (orientation == 3)
            {
                //Extend Z+
        		renderer.setRenderBounds(0.25D, 0.25D, 0.8D, 0.75D, 0.75D, 1.0D);
        		renderer.renderStandardBlock(block, x, y, z);
        		
        		renderer.setRenderBounds(0.375D, 0.375D, 0.25D, 0.625D, 0.625D, 0.8D);
        		renderer.renderStandardBlock(block, x, y, z);

            }
            else if (orientation == 4)
            {
                //Extend Y-
        		renderer.setRenderBounds(0.25D, 0.0D, 0.25D, 0.75D, 0.2D, 0.75D);
        		renderer.renderStandardBlock(block, x, y, z);
        		
        		renderer.setRenderBounds(0.375D, 0.2D, 0.375D, 0.625D, 0.75D, 0.625D);
        		renderer.renderStandardBlock(block, x, y, z);

            }
            else if (orientation == 5)
            {
                //Extend Y+
        		renderer.setRenderBounds(0.25D, 0.8D, 0.25D, 0.75D, 1.0D, 0.75D);
        		renderer.renderStandardBlock(block, x, y, z);
        		
        		renderer.setRenderBounds(0.375D, 0.25D, 0.375D, 0.625D, 0.8D, 0.625D);
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
	public boolean shouldRender3DInInventory() {
		return true;
	}

	@Override
	public int getRenderId() {
		// TODO Auto-generated method stub
		return this.renderID;
	}
}
