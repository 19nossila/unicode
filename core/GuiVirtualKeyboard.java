package unicode.core;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.tileentity.TileEntitySign;
import org.lwjgl.opengl.GL11;
import unicode.api.UnicodeAPI;

@SideOnly(Side.CLIENT)
public class GuiVirtualKeyboard extends GuiScreen {

    private final GuiScreen parentScreen;
    private final List<Character> availableChars = new ArrayList<>();
    
    private int scrollOffset = 0;
    private int charsPerPage;
    private int charsPerRow = 12; 
    private int rowsPerPage = 5;  
    private int buttonWidth = 22; 
    private int buttonHeight = 24; 


    public GuiVirtualKeyboard(GuiScreen parent) {
        this.parentScreen = parent;
    }

    @Override
    public void initGui() {
        super.initGui();
        availableChars.clear();
        availableChars.addAll(UnicodeAPI.getRegisteredStaticChars().keySet());
        availableChars.addAll(UnicodeAPI.getRegisteredAnimatedChars().keySet());
        
        this.charsPerPage = charsPerRow * rowsPerPage;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        this.drawCenteredString(this.fontRenderer, "Teclado Unicode (Ctrl+V) - Use a roda do mouse para rolar", this.width / 2, 20, 0xFFFFFF);

        int startX = (this.width - (charsPerRow * buttonWidth)) / 2;
        int startY = 40;
        int charIndex = scrollOffset;

        for (int row = 0; row < rowsPerPage && charIndex < availableChars.size(); row++) {
            for (int col = 0; col < charsPerRow && charIndex < availableChars.size(); col++) {
                int x = startX + col * buttonWidth;
                int y = startY + row * buttonHeight;
                
                
                drawRect(x, y, x + buttonWidth, y + buttonHeight, 0x55FFFFFF);
                
                
                char emojiChar = availableChars.get(charIndex);
                this.fontRenderer.drawString(String.valueOf(emojiChar), x + 6, y + 4, 0xFFFFFF, true);
                
                
                GL11.glPushMatrix();
                
                String hexValue = Integer.toHexString(emojiChar).toUpperCase();
                String escapeSequence = "[e#" + hexValue + "]";
                
                
                float scale = 0.5F;
                GL11.glTranslatef(x + buttonWidth / 2, y + 18, 0);
                GL11.glScalef(scale, scale, scale);
                
                
                this.drawCenteredString(this.fontRenderer, escapeSequence, 0, 0, 0xAAAAAA);
                GL11.glPopMatrix();
                
                
                charIndex++;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int startX = (this.width - (charsPerRow * buttonWidth)) / 2;
            int startY = 40;
            int charIndex = scrollOffset;

            for (int row = 0; row < rowsPerPage && charIndex < availableChars.size(); row++) {
                for (int col = 0; col < charsPerRow && charIndex < availableChars.size(); col++) {
                    int x = startX + col * buttonWidth;
                    int y = startY + row * buttonHeight;

                   
                    if (mouseX >= x && mouseX < x + buttonWidth && mouseY >= y && mouseY < y + buttonHeight) {
                        sendCharToParent(availableChars.get(charIndex));
                        return;
                    }
                    charIndex++;
                }
            }
        }
    }
    
    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int scroll = org.lwjgl.input.Mouse.getEventDWheel();
        if (scroll != 0) {
            if (scroll > 0) {
                scrollOffset = Math.max(0, scrollOffset - charsPerRow);
            } else {
                if(availableChars.size() > scrollOffset + charsPerPage) {
                    scrollOffset += charsPerRow;
                }
            }
        }
    }

    private void sendCharToParent(char c) {
        String hexValue = Integer.toHexString(c).toUpperCase();
        String escapeSequence = "[e#" + hexValue + "]";

        if (this.parentScreen instanceof GuiEditSign) {
            try {
                GuiEditSign guiSign = (GuiEditSign) this.parentScreen;
                Field tileField = GuiEditSign.class.getDeclaredField("field_73982_c");
                tileField.setAccessible(true);
                TileEntitySign tileEntity = (TileEntitySign) tileField.get(guiSign);
                
                Field lineField = GuiEditSign.class.getDeclaredField("field_73984_m");
                lineField.setAccessible(true);
                int currentLine = (Integer) lineField.get(guiSign);
                
                String oldText = tileEntity.signText[currentLine];
                if (oldText.length() + escapeSequence.length() <= 15) {
                    tileEntity.signText[currentLine] = oldText + escapeSequence;
                } else {
                    this.mc.sndManager.playSoundFX("random.click", 1.0F, 0.5F);
                }

            } catch (Exception e) {
                System.err.println("[UnicodeLib] Falha ao injetar caractere na placa via reflexao!");
                e.printStackTrace();
            }
        } 
        
        else if (this.parentScreen instanceof GuiChat) {
            try {
                GuiChat guiChat = (GuiChat) this.parentScreen;
                GuiTextField textField = null;
                
                
                try {
                    
                    Field inputField = GuiChat.class.getDeclaredField("field_73901_a");
                    inputField.setAccessible(true);
                    textField = (GuiTextField) inputField.get(guiChat);
                } catch (NoSuchFieldException e) {
                    
                    Field inputField = GuiChat.class.getDeclaredField("a");
                    inputField.setAccessible(true);
                    textField = (GuiTextField) inputField.get(guiChat);
                }
                

                if (textField != null) {
                    textField.writeText(escapeSequence);
                }

            } catch (Exception e) {
                System.err.println("[UnicodeLib] Falha ao injetar caractere no chat via reflexao!");
                e.printStackTrace();
            }
        }
        
        
        this.mc.displayGuiScreen(this.parentScreen);
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}