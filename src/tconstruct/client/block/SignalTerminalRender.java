package tconstruct.client.block;

import org.lwjgl.opengl.GL11;

import tconstruct.blocks.logic.SignalBusLogic;
import tconstruct.blocks.logic.SignalTerminalLogic;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class SignalTerminalRender implements ISimpleBlockRenderingHandler
{
    public static int renderID = RenderingRegistry.getNextAvailableRenderId();

    @Override
    public void renderInventoryBlock (Block block, int metadata, int modelID, RenderBlocks renderer)
    {
        // Render X-
        renderer.setRenderBounds(0.0D, 0.25D, 0.25D, 0.2D, 0.75D, 0.75D);
        this.renderStandardBlock(block, metadata, renderer);

        renderer.setRenderBounds(0.2D, 0.375D, 0.375D, 0.75D, 0.625D, 0.625D);
        this.renderStandardBlock(block, metadata, renderer);
    }

    @Override
    public boolean renderWorldBlock (IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
    {
        int sidesRendered = 0;

        if (modelId == renderID)
        {
            TileEntity te = world.getBlockTileEntity(x, y, z);
            if (!(te instanceof SignalTerminalLogic))
            {
                // Render X-
                renderer.setRenderBounds(0.0D, 0.25D, 0.25D, 0.2D, 0.75D, 0.75D);
                renderer.renderStandardBlock(block, x, y, z);

                renderer.setRenderBounds(0.2D, 0.375D, 0.375D, 0.625D, 0.625D, 0.625D);
                renderer.setOverrideBlockTexture(SignalTerminalLogic.getChannelIcon(world, x, y, z));
                renderer.renderStandardBlock(block, x, y, z);
                renderer.clearOverrideBlockTexture();

                return true;
            }
            boolean[] connectedSides = ((SignalTerminalLogic) te).getConnectedSides();

            //Base
            //renderer.setRenderBounds(0.375D, 0.0D, 0.375D, 0.625D, 0.2D, 0.625D);
            //renderer.renderStandardBlock(block, x, y, z);

            if (connectedSides[ForgeDirection.WEST.ordinal()])
            {
                // Render X-
                renderer.setRenderBounds(0.0D, 0.25D, 0.25D, 0.2D, 0.75D, 0.75D);
                renderer.renderStandardBlock(block, x, y, z);

                renderer.setRenderBounds(0.2D, 0.375D, 0.375D, 0.625D, 0.625D, 0.625D);
                renderer.setOverrideBlockTexture(SignalTerminalLogic.getChannelIcon(world, x, y, z));
                renderer.renderStandardBlock(block, x, y, z);
                renderer.clearOverrideBlockTexture();

                sidesRendered++;
            }
            if (connectedSides[ForgeDirection.EAST.ordinal()])
            {
                //Extend X+
                renderer.setRenderBounds(0.8D, 0.25D, 0.25D, 1.0D, 0.75D, 0.75D);
                renderer.renderStandardBlock(block, x, y, z);

                renderer.setRenderBounds(0.375D, 0.375D, 0.375D, 0.8D, 0.625D, 0.625D);
                renderer.setOverrideBlockTexture(SignalTerminalLogic.getChannelIcon(world, x, y, z));
                renderer.renderStandardBlock(block, x, y, z);
                renderer.clearOverrideBlockTexture();

                sidesRendered++;
            }
            if (connectedSides[ForgeDirection.SOUTH.ordinal()])
            {
                //Extend Z-
                renderer.setRenderBounds(0.25D, 0.25D, 0.0D, 0.75D, 0.75D, 0.2D);
                renderer.renderStandardBlock(block, x, y, z);

                renderer.setRenderBounds(0.375D, 0.375D, 0.2D, 0.625D, 0.625D, 0.625D);
                renderer.setOverrideBlockTexture(SignalTerminalLogic.getChannelIcon(world, x, y, z));
                renderer.renderStandardBlock(block, x, y, z);
                renderer.clearOverrideBlockTexture();

                sidesRendered++;
            }
            if (connectedSides[ForgeDirection.NORTH.ordinal()])
            {
                //Extend Z+
                renderer.setRenderBounds(0.25D, 0.25D, 0.8D, 0.75D, 0.75D, 1.0D);
                renderer.renderStandardBlock(block, x, y, z);

                renderer.setRenderBounds(0.375D, 0.375D, 0.375D, 0.625D, 0.625D, 0.8D);
                renderer.setOverrideBlockTexture(SignalTerminalLogic.getChannelIcon(world, x, y, z));
                renderer.renderStandardBlock(block, x, y, z);
                renderer.clearOverrideBlockTexture();

                sidesRendered++;
            }
            if (connectedSides[ForgeDirection.DOWN.ordinal()])
            {
                //Extend Y-
                renderer.setRenderBounds(0.25D, 0.0D, 0.25D, 0.75D, 0.2D, 0.75D);
                renderer.renderStandardBlock(block, x, y, z);

                renderer.setRenderBounds(0.375D, 0.2D, 0.375D, 0.625D, 0.625D, 0.625D);
                renderer.setOverrideBlockTexture(SignalTerminalLogic.getChannelIcon(world, x, y, z));
                renderer.renderStandardBlock(block, x, y, z);
                renderer.clearOverrideBlockTexture();

                sidesRendered++;
            }
            if (connectedSides[ForgeDirection.UP.ordinal()])
            {
                //Extend Y+
                renderer.setRenderBounds(0.25D, 0.8D, 0.25D, 0.75D, 1.0D, 0.75D);
                renderer.renderStandardBlock(block, x, y, z);

                renderer.setRenderBounds(0.375D, 0.375D, 0.375D, 0.625D, 0.8D, 0.625D);
                renderer.setOverrideBlockTexture(SignalTerminalLogic.getChannelIcon(world, x, y, z));
                renderer.renderStandardBlock(block, x, y, z);
                renderer.clearOverrideBlockTexture();

                sidesRendered++;
            }

            if (sidesRendered == 0)
            {
                // Render X-
                renderer.setRenderBounds(0.0D, 0.25D, 0.25D, 0.2D, 0.75D, 0.75D);
                renderer.renderStandardBlock(block, x, y, z);

                renderer.setRenderBounds(0.2D, 0.375D, 0.375D, 0.625D, 0.625D, 0.625D);
                renderer.setOverrideBlockTexture(SignalTerminalLogic.getChannelIcon(world, x, y, z));
                renderer.renderStandardBlock(block, x, y, z);
                renderer.clearOverrideBlockTexture();

                return true;
            }
            return true;
        }
        return false;
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
        return true;
    }

    @Override
    public int getRenderId ()
    {
        return this.renderID;
    }
}
