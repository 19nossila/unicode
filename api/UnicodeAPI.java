package unicode.api;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderEngine;
import unicode.core.CustomFontRenderer;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import unicode.core.GifDecoder;
import unicode.core.GifDecoder.GifFrame;

public class UnicodeAPI {
	
	private static float maxRegisteredHeight = 9.0F;

    public static class CustomChar {
        public final String texturePath;
        public final int u, v, width, height;
        public final float renderWidth, renderHeight;


        public CustomChar(String path, int u, int v, int w, int h) {
            this(path, u, v, w, h, 9.0F, 9.0F);
        }


        public CustomChar(String path, int u, int v, int w, int h, float renderW, float renderH) {
            this.texturePath = path; this.u = u; this.v = v; this.width = w; this.height = h;
            this.renderWidth = renderW; this.renderHeight = renderH;
        }
    }

    public static class AnimatedChar {
        public final int textureId;
        public final ArrayList<GifFrame> frames;
        public final float renderWidth, renderHeight;
        private long startTime = 0;
        private int totalDelay = 0;
        private int currentFrameIndex = -1;
        private ByteBuffer buffer;

        public AnimatedChar(int textureId, ArrayList<GifFrame> frames, float renderW, float renderH) {
            this.textureId = textureId;
            this.frames = frames;
            this.renderWidth = renderW;
            this.renderHeight = renderH;
            this.startTime = System.currentTimeMillis();
            for (GifFrame frame : frames) {
                this.totalDelay += frame.delay;
            }
            if(this.totalDelay <= 0) this.totalDelay = 100;
        }
        
        public void update() {
            if (frames.size() <= 1) return;

            long elapsed = (System.currentTimeMillis() - startTime) % totalDelay;
            int cumulativeDelay = 0;
            int frameToShow = 0;
            for (int i = 0; i < frames.size(); i++) {
                cumulativeDelay += frames.get(i).delay;
                if (elapsed < cumulativeDelay) {
                    frameToShow = i;
                    break;
                }
            }
            
            if (frameToShow != this.currentFrameIndex) {
                this.currentFrameIndex = frameToShow;
                BufferedImage image = frames.get(frameToShow).image;
                int width = image.getWidth();
                int height = image.getHeight();

                int[] imageData = new int[width * height];
                image.getRGB(0, 0, width, height, imageData, 0, width);

                if (this.buffer == null) {
                    this.buffer = ByteBuffer.allocateDirect(4 * width * height).order(ByteOrder.nativeOrder());
                }
                
                this.buffer.clear();

                for (int pixel : imageData) {
                    byte alpha = (byte) ((pixel >> 24) & 0xFF);
                    byte red   = (byte) ((pixel >> 16) & 0xFF);
                    byte green = (byte) ((pixel >> 8)  & 0xFF);
                    byte blue  = (byte) (pixel & 0xFF);
                    
                    this.buffer.put(red);
                    this.buffer.put(green);
                    this.buffer.put(blue);
                    this.buffer.put(alpha);
                }
                
                this.buffer.flip();

                GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textureId);
                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.buffer);
            }
        }
    }

    private static final Map<Character, CustomChar> customCharMap = new HashMap<>();
    private static final Map<Character, AnimatedChar> animatedCharMap = new HashMap<>();
    
    public static Map<Character, CustomChar> getRegisteredStaticChars() {
        return java.util.Collections.unmodifiableMap(customCharMap);
    }

    public static Map<Character, AnimatedChar> getRegisteredAnimatedChars() {
        return java.util.Collections.unmodifiableMap(animatedCharMap);
    }
    
    
    public static void registerChar(char c, CustomChar customChar) {
        if (!customCharMap.containsKey(c) && !animatedCharMap.containsKey(c)) {
            customCharMap.put(c, customChar);
            maxRegisteredHeight = Math.max(maxRegisteredHeight, customChar.renderHeight);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public static void registerAnimatedChar(char c, String gifResourcePath) {
        registerAnimatedChar(c, gifResourcePath, 9.0F, 9.0F);
    }

    @SideOnly(Side.CLIENT)
    public static void registerAnimatedChar(char c, String gifResourcePath, float renderWidth, float renderHeight) {
        if (customCharMap.containsKey(c) || animatedCharMap.containsKey(c)) return;

        try {
        	
        	maxRegisteredHeight = Math.max(maxRegisteredHeight, renderHeight);
        	
            InputStream stream = UnicodeAPI.class.getResourceAsStream(gifResourcePath);
            ArrayList<GifFrame> frames = GifDecoder.read(stream);
            if (frames == null || frames.isEmpty()) return;

            RenderEngine renderEngine = Minecraft.getMinecraft().renderEngine;
            
            int textureId = renderEngine.allocateAndSetupTexture(frames.get(0).image);
            
            AnimatedChar animatedChar = new AnimatedChar(textureId, frames, renderWidth, renderHeight);
            animatedCharMap.put(c, animatedChar);
            System.out.println("Registrado GIF animado para o caractere '" + c + "' com Texture ID: " + textureId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CustomChar getChar(char c) { return customCharMap.get(c); }
    public static AnimatedChar getAnimatedChar(char c) { return animatedCharMap.get(c); }
    @SideOnly(Side.CLIENT)
    public static void updateAnimations() {
        int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        for (AnimatedChar ac : animatedCharMap.values()) {
            ac.update();
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
    }
    
    public static float getMaxRegisteredHeight() {
        return maxRegisteredHeight;
    }
    @SideOnly(Side.CLIENT)
    public static void setLineHeight(int height) {
        if (Minecraft.getMinecraft().fontRenderer instanceof CustomFontRenderer) {
            ((CustomFontRenderer) Minecraft.getMinecraft().fontRenderer).FONT_HEIGHT = height;
        }
    }

}