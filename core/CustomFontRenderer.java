package unicode.core;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import net.minecraft.client.renderer.RenderEngine;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import unicode.api.UnicodeAPI;
import unicode.api.UnicodeAPI.AnimatedChar;
import unicode.api.UnicodeAPI.CustomChar;
@SideOnly(Side.CLIENT)
public class CustomFontRenderer extends net.minecraft.client.gui.FontRenderer {

    private int[] charWidth = new int[2048];
    public int FONT_HEIGHT = 10;
    public Random fontRandom = new Random();
    private byte[] glyphWidth = new byte[65536];
    private int[] colorCode = new int[32];
    private final String fontTextureName;
    private final RenderEngine renderEngine;
    private float posX;
    private float posY;
    private boolean unicodeFlag;
    private boolean bidiFlag;
    private float red;
    private float green;
    private float blue;
    private float alpha;
    private int textColor;
    private boolean randomStyle = false;
    private boolean boldStyle = false;
    private boolean italicStyle = false;
    private boolean underlineStyle = false;
    private boolean strikethroughStyle = false;

    public CustomFontRenderer(GameSettings par1GameSettings, String par2Str, RenderEngine par3RenderEngine, boolean par4) {
        super(par1GameSettings, par2Str, par3RenderEngine, par4);
        this.fontTextureName = par2Str;
        this.renderEngine = par3RenderEngine;
        this.unicodeFlag = par4;

        try {
            BufferedImage bufferedimage = ImageIO.read(RenderEngine.class.getResourceAsStream(this.fontTextureName));
            int i = bufferedimage.getWidth();
            int j = bufferedimage.getHeight();
            int[] ai = new int[i * j];
            bufferedimage.getRGB(0, 0, i, j, ai, 0, i);
            for (int k = 0; k < 256; ++k) {
                int l = k % 16;
                int i1 = k / 16;
                int j1 = 7;
                while (j1 >= 0) {
                    int k1 = l * 8 + j1;
                    boolean flag = true;
                    for (int l1 = 0; l1 < 8 && flag; ++l1) {
                        int i2 = (i1 * 8 + l1) * i;
                        int j2 = ai[k1 + i2] & 255;
                        if (j2 > 0) { flag = false; }
                    }
                    if (!flag) { break; }
                    --j1;
                }
                if (k == 32) { j1 = 2; }
                this.charWidth[k] = j1 + 2;
            }
        } catch (Exception e) { e.printStackTrace(); }
        
        try {
            InputStream inputstream = RenderEngine.class.getResourceAsStream("/font/glyph_sizes.bin");
            inputstream.read(this.glyphWidth);
        } catch (Exception e) { e.printStackTrace(); }

        for(int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i >> 0 & 1) * 170 + j;
            if (i == 6) { k += 85; }
            if (par1GameSettings.anaglyph) {
                int j1 = (k * 30 + l * 59 + i1 * 11) / 100;
                int k1 = (k * 30 + l * 70) / 100;
                int l1 = (k * 30 + i1 * 70) / 100;
                k = j1; l = k1; i1 = l1;
            }
            if (i >= 16) { k /= 4; l /= 4; i1 /= 4; }
            this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }
    }

    private void renderStringAtPos(String par1Str, boolean par2) {
        for (int i = 0; i < par1Str.length(); ++i) {
            char c0 = par1Str.charAt(i);

            CustomChar staticChar = UnicodeAPI.getChar(c0);
            AnimatedChar animatedChar = UnicodeAPI.getAnimatedChar(c0);

            if (staticChar != null) {
                float advanceWidth = staticChar.renderWidth;
                if (par2) {
                    this.posX += advanceWidth;
                } else { 
                    renderCustomQuad(staticChar.texturePath, staticChar.u, staticChar.v, staticChar.width, staticChar.height, true, staticChar.renderWidth, staticChar.renderHeight);
                    this.posX += advanceWidth;
                }
                continue;
            }
            
            if (animatedChar != null) {
                float advanceWidth = animatedChar.renderWidth;
                if (par2) { 
                    this.posX += advanceWidth;
                } else { 
                    renderCustomQuad("ID:" + animatedChar.textureId, 0, 0, animatedChar.frames.get(0).image.getWidth(), animatedChar.frames.get(0).image.getHeight(), true, animatedChar.renderWidth, animatedChar.renderHeight);
                    this.posX += advanceWidth;
                }
                continue;
            }
            

            if (c0 == 167 && i + 1 < par1Str.length()) {
                int j = "0123456789abcdefklmnor".indexOf(par1Str.toLowerCase().charAt(i + 1));
                if (j < 16) {
                    this.randomStyle = false; this.boldStyle = false; this.strikethroughStyle = false; this.underlineStyle = false; this.italicStyle = false;
                    if (j < 0) { j = 15; }
                    if (par2) { j += 16; }
                    int k = this.colorCode[j];
                    this.textColor = k;
                    GL11.glColor4f((float)(k >> 16) / 255.0F, (float)(k >> 8 & 255) / 255.0F, (float)(k & 255) / 255.0F, this.alpha);
                } else if (j == 16) { this.randomStyle = true;
                } else if (j == 17) { this.boldStyle = true;
                } else if (j == 18) { this.strikethroughStyle = true;
                } else if (j == 19) { this.underlineStyle = true;
                } else if (j == 20) { this.italicStyle = true;
                } else { this.resetStyles(); GL11.glColor4f(this.red, this.green, this.blue, this.alpha); }
                ++i;
            } else {
                int j = ChatAllowedCharacters.allowedCharacters.indexOf(c0);
                if (this.randomStyle && j > 0) {
                    int k;
                    do { k = this.fontRandom.nextInt(ChatAllowedCharacters.allowedCharacters.length()); } while(this.charWidth[j + 32] != this.charWidth[k + 32]);
                    j = k;
                }
                float f = this.unicodeFlag ? 0.5F : 1.0F;
                boolean flag1 = (j <= 0 || this.unicodeFlag) && par2;
                if (flag1) { this.posX -= f; this.posY -= f; }
                float f1 = this.renderCharAtPos(j, c0, this.italicStyle);
                if (flag1) { this.posX += f; this.posY += f; }
                if (this.boldStyle) {
                    this.posX += f;
                    if (flag1) { this.posX -= f; this.posY -= f; }
                    this.renderCharAtPos(j, c0, this.italicStyle);
                    this.posX -= f;
                    if (flag1) { this.posX += f; this.posY += f; }
                    ++f1;
                }
                if (this.strikethroughStyle) {
                    Tessellator tessellator = Tessellator.instance;
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    tessellator.startDrawingQuads();
                    tessellator.addVertex((double)this.posX, (double)(this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0D);
                    tessellator.addVertex((double)(this.posX + f1), (double)(this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0D);
                    tessellator.addVertex((double)(this.posX + f1), (double)(this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0D);
                    tessellator.addVertex((double)this.posX, (double)(this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0D);
                    tessellator.draw();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                }
                if (this.underlineStyle) {
                    Tessellator tessellator = Tessellator.instance;
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    tessellator.startDrawingQuads();
                    tessellator.addVertex((double)this.posX, (double)(this.posY + (float)this.FONT_HEIGHT), 0.0D);
                    tessellator.addVertex((double)(this.posX + f1), (double)(this.posY + (float)this.FONT_HEIGHT), 0.0D);
                    tessellator.addVertex((double)(this.posX + f1), (double)(this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0D);
                    tessellator.addVertex((double)this.posX, (double)(this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0D);
                    tessellator.draw();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                }
                this.posX += (int)f1;
            }
        }
    }
    
    private String decodeEmojiSequences(String text) {
        if (text == null || !text.contains("[e#")) {
            return text;
        }

        StringBuilder decodedText = new StringBuilder();
        int lastIndex = 0;
        int startIndex;

        
        while ((startIndex = text.indexOf("[e#", lastIndex)) != -1) {
            
            int endIndex = text.indexOf(']', startIndex);

            
            if (endIndex > startIndex) {
                
                decodedText.append(text.substring(lastIndex, startIndex));
                
                
                String hex = text.substring(startIndex + 3, endIndex);
                try {
                    
                    char emojiChar = (char) Integer.parseInt(hex, 16);
                    decodedText.append(emojiChar);
                    lastIndex = endIndex + 1;
                } catch (NumberFormatException e) {
                    
                    decodedText.append(text.substring(startIndex, endIndex + 1));
                    lastIndex = endIndex + 1;
                }
            } else {
                
                break;
            }
        }

        
        decodedText.append(text.substring(lastIndex));

        return decodedText.toString();
    }
    

    private void renderCustomQuad(String textureInfo, int u, int v, int width, int height, boolean fullUV, float renderWidth, float renderHeight) {
        GL11.glPushMatrix();

        float originalRed = this.red;
        float originalGreen = this.green;
        float originalBlue = this.blue;
        float originalAlpha = this.alpha;
        
        boolean wasBlendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        try {
        	
        	GL11.glEnable(GL11.GL_TEXTURE_2D);
        	
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

            if (textureInfo.startsWith("ID:")) {
                int textureId = Integer.parseInt(textureInfo.substring(3));
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            } else {
                this.renderEngine.bindTexture(textureInfo);
            }
            
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);


            float yOffset = (this.FONT_HEIGHT - renderHeight) / 2F;

            float u_start = fullUV ? 0.0F : (float)u / 256.0F;
            float v_start = fullUV ? 0.0F : (float)v / 256.0F;
            float u_end = fullUV ? 1.0F : (float)(u + width) / 256.0F;
            float v_end = fullUV ? 1.0F : (float)(v + height) / 256.0F;

            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV((double)this.posX, (double)(this.posY + yOffset + renderHeight), 0.0D, u_start, v_end);
            tessellator.addVertexWithUV((double)(this.posX + renderWidth), (double)(this.posY + yOffset + renderHeight), 0.0D, u_end, v_end);
            tessellator.addVertexWithUV((double)(this.posX + renderWidth), (double)(this.posY + yOffset), 0.0D, u_end, v_start);
            tessellator.addVertexWithUV((double)this.posX, (double)(this.posY + yOffset), 0.0D, u_start, v_start);
            tessellator.draw();
        } finally {
            GL11.glPopMatrix();
            GL11.glColor4f(originalRed, originalGreen, originalBlue, originalAlpha);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
            
            if (!wasBlendEnabled) {
                GL11.glDisable(GL11.GL_BLEND);
            }
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

    private float renderCharAtPos(int par1, char par2, boolean par3) {
        return par2 == 32 ? 4.0F : (par1 > 0 && !this.unicodeFlag ? this.renderDefaultChar(par1 + 32, par3) : this.renderUnicodeChar(par2, par3));
    }


    private float renderDefaultChar(int par1, boolean par2) {
        float f = (float)(par1 % 16 * 8);
        float f1 = (float)(par1 / 16 * 8);
        float f2 = par2 ? 1.0F : 0.0F;
        this.renderEngine.bindTexture(this.fontTextureName);
        float f3 = (float)this.charWidth[par1] - 0.01F;

        float u1 = f / 128.0F;
        float v1 = f1 / 128.0F;
        float u2 = (f + f3) / 128.0F;
        float v2 = (f1 + 7.99F) / 128.0F;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(this.posX + f2, this.posY, 0.0D, u1, v1);
        tessellator.addVertexWithUV(this.posX - f2, this.posY + 7.99F, 0.0D, u1, v2);
        tessellator.addVertexWithUV(this.posX + f3 - f2, this.posY + 7.99F, 0.0D, u2, v2);
        tessellator.addVertexWithUV(this.posX + f3 + f2, this.posY, 0.0D, u2, v1);
        tessellator.draw();
        
        return (float)this.charWidth[par1];
    }

    private void loadGlyphTexture(int par1) {
        String s = String.format("/font/glyph_%02X.png", new Object[] {Integer.valueOf(par1)});
        this.renderEngine.bindTexture(s);
    }


    private float renderUnicodeChar(char par1, boolean par2) {
        if (this.glyphWidth[par1] == 0) {
            return 0.0F;
        } else {
            int i = par1 / 256;
            this.loadGlyphTexture(i);
            int j = this.glyphWidth[par1] >>> 4;
            int k = this.glyphWidth[par1] & 15;
            float f = (float)j;
            float f1 = (float)(k + 1);
            float f2 = (float)(par1 % 16 * 16) + f;
            float f3 = (float)((par1 & 255) / 16 * 16);
            float f4 = f1 - f - 0.02F;
            float f5 = par2 ? 1.0F : 0.0F;

            float u1 = f2 / 256.0F;
            float v1 = f3 / 256.0F;
            float u2 = (f2 + f4) / 256.0F;
            float v2 = (f3 + 15.98F) / 256.0F;

            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(this.posX + f5, this.posY, 0.0D, u1, v1);
            tessellator.addVertexWithUV(this.posX - f5, this.posY + 7.99F, 0.0D, u1, v2);
            tessellator.addVertexWithUV(this.posX + f4 / 2.0F - f5, this.posY + 7.99F, 0.0D, u2, v2);
            tessellator.addVertexWithUV(this.posX + f4 / 2.0F + f5, this.posY, 0.0D, u2, v1);
            tessellator.draw();

            return (f1 - f) / 2.0F + 1.0F;
        }
    }
    
    @Override
    public int drawStringWithShadow(String par1Str, int par2, int par3, int par4)
    {
        return this.drawString(par1Str, par2, par3, par4, false);
    }
    
    @Override
    public int drawString(String par1Str, int par2, int par3, int par4, boolean par5) {
    	par1Str = decodeEmojiSequences(par1Str);
    	this.resetStyles();
        if (this.bidiFlag) {
            par1Str = this.bidiReorder(par1Str);
        }
        int l;
        if (par5) {
            l = this.renderString(par1Str, par2 + 1, par3 + 1, par4, true);
            l = Math.max(l, this.renderString(par1Str, par2, par3, par4, false));
        } else {
            l = this.renderString(par1Str, par2, par3, par4, false);
        }
        return l;
    }

    private String bidiReorder(String par1Str) {
        return par1Str;
    }

    private void resetStyles() {
        this.randomStyle = this.boldStyle = this.italicStyle = this.underlineStyle = this.strikethroughStyle = false;
    }

    private int renderString(String par1Str, int par2, int par3, int par4, boolean par5) {
    	par1Str = decodeEmojiSequences(par1Str);
    	if (par1Str == null) { return 0; }
        if ((par4 & -67108864) == 0) { par4 |= -16777216; }
        if (par5) { par4 = (par4 & 16579836) >> 2 | par4 & -16777216; }
        this.red = (float)(par4 >> 16 & 255) / 255.0F;
        this.green = (float)(par4 >> 8 & 255) / 255.0F;
        this.blue = (float)(par4 & 255) / 255.0F;
        this.alpha = (float)(par4 >> 24 & 255) / 255.0F;
        GL11.glColor4f(this.red, this.green, this.blue, this.alpha);
        this.posX = (float)par2;
        this.posY = (float)par3;
        this.renderStringAtPos(par1Str, par5);
        return (int)this.posX;
    }

    @Override
    public int getCharWidth(char par1) {
        CustomChar custom = UnicodeAPI.getChar(par1);
        if (custom != null) { 
            return (int)custom.renderWidth;
        }
        
        AnimatedChar animated = UnicodeAPI.getAnimatedChar(par1);
        if (animated != null) {
            return (int)animated.renderWidth;
        }
        if (par1 == 167) { return -1; }
        if (par1 == ' ') { return 4; }
        int i = ChatAllowedCharacters.allowedCharacters.indexOf(par1);
        if (i >= 0 && !this.unicodeFlag) {
            return this.charWidth[i + 32];
        } else if (this.glyphWidth[par1] != 0) {
            int j = this.glyphWidth[par1] >>> 4;
            int k = this.glyphWidth[par1] & 15;
            if (k > 7) { k = 15; j = 0; }
            ++k;
            return (k - j) / 2 + 1;
        }
        return 0;
    }
    
    @Override
    public List listFormattedStringToWidth(String par1Str, int par2) {
        return Arrays.asList(this.wrapFormattedStringToWidth(par1Str, par2).split("\n"));
    }

    String wrapFormattedStringToWidth(String par1Str, int par2) {
        int j = this.sizeStringToWidth(par1Str, par2);
        if (par1Str.length() <= j) {
            return par1Str;
        } else {
            String s1 = par1Str.substring(0, j);
            char c0 = par1Str.charAt(j);
            boolean flag = c0 == ' ' || c0 == '\n';
            String s2 = getFormatFromString(s1) + par1Str.substring(j + (flag ? 1 : 0));
            return s1 + "\n" + this.wrapFormattedStringToWidth(s2, par2);
        }
    }

    private int sizeStringToWidth(String par1Str, int par2) {
        int j = par1Str.length();
        int k = 0;
        int l = 0;
        int i1 = -1;
        boolean flag = false;
        int j1;
        for (j1 = l; j1 < j; ++j1) {
            char c0 = par1Str.charAt(j1);
            switch (c0) {
                case '\n':
                    --j1;
                    break;
                case ' ':
                    i1 = j1;
                default:
                    k += this.getCharWidth(c0);
                    if (flag) {
                        ++k;
                    }
                    break;
                case 167:
                    if (j1 < j - 1) {
                        ++j1;
                        char c1 = par1Str.charAt(j1);
                        if (c1 != 'l' && c1 != 'L') {
                            if (c1 == 'r' || c1 == 'R' || isFormatColor(c1)) {
                                flag = false;
                            }
                        } else {
                            flag = true;
                        }
                    }
            }
            if (c0 == '\n') {
                ++j1;
                i1 = j1;
                break;
            }
            if (k > par2) {
                break;
            }
        }
        return j1 != j && i1 != -1 && i1 < j1 ? i1 : j1;
    }

    private static String getFormatFromString(String par0Str) {
        String s1 = "";
        int i = -1;
        int j = par0Str.length();
        while((i = par0Str.indexOf(167, i + 1)) != -1) {
            if (i < j - 1) {
                char c0 = par0Str.charAt(i + 1);
                if (isFormatColor(c0)) {
                    s1 = "\u00a7" + c0;
                } else if (isFormatSpecial(c0)) {
                    s1 = s1 + "\u00a7" + c0;
                }
            }
        }
        return s1;
    }

    private static boolean isFormatColor(char par0) {
        return par0 >= '0' && par0 <= '9' || par0 >= 'a' && par0 <= 'f' || par0 >= 'A' && par0 <= 'F';
    }

    private static boolean isFormatSpecial(char par0) {
        return par0 >= 'k' && par0 <= 'o' || par0 >= 'K' && par0 <= 'O' || par0 == 'r' || par0 == 'R';
    }
}