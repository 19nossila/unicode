package unicode.core;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelSign;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import org.lwjgl.opengl.GL11;

public class CustomSignRenderer extends TileEntitySpecialRenderer {
    
    private final ModelSign modelSign = new ModelSign();


    @Override
    public void renderTileEntityAt(TileEntity par1TileEntity, double par2, double par4, double par6, float par8) {
        TileEntitySign tileentitysign = (TileEntitySign) par1TileEntity;
        Block block = tileentitysign.getBlockType();
        
        GL11.glPushMatrix();
        float f1 = 0.6666667F;

        if (block == Block.signPost) {
            GL11.glTranslatef((float)par2 + 0.5F, (float)par4 + 0.75F * f1, (float)par6 + 0.5F);
            float f2 = (float)(tileentitysign.getBlockMetadata() * 360) / 16.0F;
            GL11.glRotatef(-f2, 0.0F, 1.0F, 0.0F);
            this.modelSign.signStick.showModel = true;
        } else {
            int i = tileentitysign.getBlockMetadata();
            float f2 = 0.0F;
            if (i == 2) { f2 = 180.0F; }
            if (i == 4) { f2 = 90.0F; }
            if (i == 5) { f2 = -90.0F; }
            GL11.glTranslatef((float)par2 + 0.5F, (float)par4 + 0.75F * f1, (float)par6 + 0.5F);
            GL11.glRotatef(-f2, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(0.0F, -0.3125F, -0.4375F);
            this.modelSign.signStick.showModel = false;
        }


        this.bindTextureByName("/item/sign.png");
        GL11.glPushMatrix();
        GL11.glScalef(f1, -f1, -f1);
        this.modelSign.renderSign();
        GL11.glPopMatrix();

        FontRenderer fontrenderer = Minecraft.getMinecraft().fontRenderer;
        float f3 = 0.016666668F * f1;
        GL11.glTranslatef(0.0F, 0.5F * f1, 0.07F * f1);
        GL11.glScalef(f3, -f3, f3);
        GL11.glNormal3f(0.0F, 0.0F, -1.0F * f3);
        GL11.glDepthMask(false);
        byte b0 = 0;
        

        for (int j = 0; j < tileentitysign.signText.length; ++j)
        {
            String s = tileentitysign.signText[j];

            if (j == tileentitysign.lineBeingEdited)
            {
                s = "> " + s + " <";
                fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, j * 10 - tileentitysign.signText.length * 5, b0);
            }
            else
            {
                fontrenderer.drawString(s, -fontrenderer.getStringWidth(s) / 2, j * 10 - tileentitysign.signText.length * 5, b0);
            }
        }

        GL11.glDepthMask(true);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }
}