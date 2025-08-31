package unicode.core;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StringUtils;
import org.lwjgl.opengl.GL11;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CustomGuiNewChat extends GuiNewChat {

    private final Minecraft mc;

    private static final int LINE_PADDING = 6;

    public CustomGuiNewChat(Minecraft par1Minecraft) {
        super(par1Minecraft);
        this.mc = par1Minecraft;
    }

    @Override
    public void drawChat(int par1) {
        if (this.mc.gameSettings.chatVisibility == 2) {
            return;
        }


        List<ChatLine> drawnChatLines = (List<ChatLine>) ObfuscationReflectionHelper.getPrivateValue(GuiNewChat.class, this, "field_96134_d");
        int scrollPos = (Integer) ObfuscationReflectionHelper.getPrivateValue(GuiNewChat.class, this, "field_73768_d");

        int lineCount = this.func_96127_i();
        boolean chatOpen = this.getChatOpen();
        float opacity = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

        if (drawnChatLines.size() > 0) {
            float scale = this.func_96131_h();
            int chatWidth = MathHelper.ceiling_float_int((float)this.func_96126_f() / scale);

            GL11.glPushMatrix();
            GL11.glTranslatef(2.0F, 20.0F, 0.0F);
            GL11.glScalef(scale, scale, 1.0F);

            int lineHeight = this.mc.fontRenderer.FONT_HEIGHT + LINE_PADDING;

            for (int i = 0; i + scrollPos < drawnChatLines.size() && i < lineCount; ++i) {
                ChatLine chatline = drawnChatLines.get(i + scrollPos);
                if (chatline != null) {
                    int updateCounter = par1 - chatline.getUpdatedCounter();
                    if (updateCounter < 200 || chatOpen) {
                        double fade = (double)updateCounter / 200.0D;
                        fade = 1.0D - fade;
                        fade *= 10.0D;
                        if (fade < 0.0D) fade = 0.0D;
                        if (fade > 1.0D) fade = 1.0D;
                        fade *= fade;
                        
                        int alpha = (int)(255.0D * fade);
                        if (chatOpen) alpha = 255;
                        alpha = (int)((float)alpha * opacity);

                        if (alpha > 3) {

                            int yPos = -i * lineHeight;
                            

                            drawRect(0, yPos - lineHeight, chatWidth + 4, yPos, alpha / 2 << 24);
                            
                            GL11.glEnable(GL11.GL_BLEND);
                            String message = chatline.getChatLineString();
                            

                            this.mc.fontRenderer.drawStringWithShadow(message, 0, yPos - (lineHeight - 1), 16777215 + (alpha << 24));
                        }
                    }
                }
            }
            GL11.glPopMatrix();
        }
    }
}