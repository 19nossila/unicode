package unicode.core;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class GifDecoder {
    public static class GifFrame {
        public BufferedImage image;
        public int delay;
        public GifFrame(BufferedImage image, int delay) { this.image = image; this.delay = delay; }
    }

    public static ArrayList<GifFrame> read(InputStream in) {
        GifDecoder decoder = new GifDecoder();
        if (decoder.readStream(in) != 0) return null;
        return decoder.getFrames();
    }
    
    private InputStream in;
    private int status;
    private int width;
    private int height;
    private boolean gctFlag;
    private int gctSize;
    private int[] gct;
    private int[] lct;
    private int[] act;
    private int bgIndex;
    private int bgColor;
    private int lastBgColor;
    private int pixelAspect;
    private boolean lctFlag;
    private int lctSize;
    private int ix, iy, iw, ih;
    private int lix, liy, liw, lih;
    private BufferedImage image;
    private BufferedImage lastImage;
    private int b;
    private int blockSize = 0;
    private byte[] block = new byte[255];
    private int dispose = 0;
    private int lastDispose = 0;
    private boolean transparency = false;
    private int delay = 0;
    private int transIndex;
    private static final int MaxStackSize = 4096;
    private short[] prefix;
    private byte[] suffix;
    private byte[] pixelStack;
    private byte[] pixels;
    private ArrayList<GifFrame> frames;
    private int frameCount;

    private ArrayList<GifFrame> getFrames() { return frames; }

    private int readStream(InputStream is) {
        init();
        if (is != null) { in = is; readHeader(); if (!err()) { readContents(); if (frameCount < 0) status = 1; } } 
        else { status = 2; }
        try { is.close(); } catch (Exception e) {}
        return status;
    }

    private void decodeImageData() {
        int NullCode = -1;
        int npix = iw * ih;
        int available, clear, code_mask, code_size, end_of_information, in_code, old_code, bits, code, count, i, datum, data_size, first, top, bi, pi;
        if ((pixels == null) || (pixels.length < npix)) pixels = new byte[npix];
        if (prefix == null) prefix = new short[MaxStackSize];
        if (suffix == null) suffix = new byte[MaxStackSize];
        if (pixelStack == null) pixelStack = new byte[MaxStackSize + 1];
        data_size = read();
        clear = 1 << data_size;
        end_of_information = clear + 1;
        available = clear + 2;
        old_code = NullCode;
        code_size = data_size + 1;
        code_mask = (1 << code_size) - 1;
        for (code = 0; code < clear; code++) { prefix[code] = 0; suffix[code] = (byte) code; }
        datum = bits = count = first = top = pi = bi = 0;
        for (i = 0; i < npix;) {
            if (top == 0) {
                if (bits < code_size) {
                    if (count == 0) { count = readBlock(); if (count <= 0) break; bi = 0; }
                    datum += (((int) block[bi]) & 0xff) << bits;
                    bits += 8;
                    bi++;
                    count--;
                    continue;
                }
                code = datum & code_mask;
                datum >>= code_size;
                bits -= code_size;
                if ((code > available) || (code == end_of_information)) break;
                if (code == clear) { code_size = data_size + 1; code_mask = (1 << code_size) - 1; available = clear + 2; old_code = NullCode; continue; }
                if (old_code == NullCode) { pixelStack[top++] = suffix[code]; old_code = code; first = code; continue; }
                in_code = code;
                if (code == available) { pixelStack[top++] = (byte) first; code = old_code; }
                while (code > clear) { pixelStack[top++] = suffix[code]; code = prefix[code]; }
                first = ((int) suffix[code]) & 0xff;
                if (available >= MaxStackSize) break;
                pixelStack[top++] = (byte) first;
                prefix[available] = (short) old_code;
                suffix[available] = (byte) first;
                available++;
                if (((available & code_mask) == 0) && (available < MaxStackSize)) { code_size++; code_mask += available; }
                old_code = in_code;
            }
            top--;
            pixels[pi++] = pixelStack[top];
            i++;
        }
        for (i = pi; i < npix; i++) pixels[i] = 0;
    }

    private boolean err() { return status != 0; }
    private void init() { status = 0; frameCount = 0; frames = new ArrayList<GifFrame>(); gct = null; lct = null; }
    private int read() { int curByte = 0; try { curByte = in.read(); } catch (Exception e) { status = 1; } return curByte; }

    private int readBlock() {
        blockSize = read();
        int n = 0;
        if (blockSize > 0) {
            try { int count = 0; while (n < blockSize) { count = in.read(block, n, blockSize - n); if (count == -1) break; n += count; }
            } catch (Exception e) { e.printStackTrace(); }
            if (n < blockSize) status = 1;
        }
        return n;
    }

    private int[] readColorTable(int ncolors) {
        int nbytes = 3 * ncolors;
        int[] tab = null;
        byte[] c = new byte[nbytes];
        int n = 0;
        try { n = in.read(c); } catch (Exception e) { e.printStackTrace(); }
        if (n < nbytes) status = 1;
        else {
            tab = new int[256];
            int i = 0;
            int j = 0;
            while (i < ncolors) {
                int r = ((int) c[j++]) & 0xff;
                int g = ((int) c[j++]) & 0xff;
                int b = ((int) c[j++]) & 0xff;
                tab[i++] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }
        return tab;
    }

    private void readContents() {
        boolean done = false;
        while (!(done || err())) {
            int code = read();
            switch (code) {
                case 0x2C: readImage(); break;
                case 0x21: code = read();
                    switch (code) {
                        case 0xf9: readGraphicControlExt(); break;
                        case 0xff: readBlock(); String app = ""; for (int i = 0; i < 11; i++) app += (char) block[i]; if (app.equals("NETSCAPE2.0")) readNetscapeExt(); else skip(); break;
                        default: skip();
                    }
                    break;
                case 0x3b: done = true; break;
                case 0x00: break;
                default: status = 1;
            }
        }
    }

    private void readGraphicControlExt() {
        read();
        int packed = read();
        dispose = (packed & 0x1c) >> 2;
        if (dispose == 0) dispose = 1;
        transparency = (packed & 1) != 0;
        delay = readShort() * 10;
        transIndex = read();
        read();
    }

    private void readHeader() { String id = ""; for (int i = 0; i < 6; i++) id += (char) read(); if (!id.startsWith("GIF")) { status = 1; return; } readLSD(); if (gctFlag && !err()) { gct = readColorTable(gctSize); bgColor = gct[bgIndex]; } }

    private void readImage() {
        ix = readShort();
        iy = readShort();
        iw = readShort();
        ih = readShort();
        int packed = read();
        lctFlag = (packed & 0x80) != 0;
        lctSize = (int) Math.pow(2, (packed & 0x07) + 1);
        if (lctFlag) { lct = readColorTable(lctSize); act = lct; } else { act = gct; if (bgIndex == transIndex) bgColor = 0; }
        int save = 0;
        if (transparency) { save = act[transIndex]; act[transIndex] = 0; }
        if (act == null) status = 1;
        if (err()) return;
        decodeImageData();
        skip();
        if (err()) return;
        frameCount++;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        setPixels();
        frames.add(new GifFrame(image, delay));
        if (transparency) act[transIndex] = save;
        resetFrame();
    }

    private void readLSD() { width = readShort(); height = readShort(); int packed = read(); gctFlag = (packed & 0x80) != 0; gctSize = 2 << (packed & 7); bgIndex = read(); pixelAspect = read(); }
    private void readNetscapeExt() { do { readBlock(); if (block[0] == 1) { int b1 = ((int) block[1]) & 0xff; int b2 = ((int) block[2]) & 0xff; } } while ((blockSize > 0) && !err()); }
    private int readShort() { return read() | (read() << 8); }
    private void resetFrame() { lastDispose = dispose; lix = ix; liy = iy; liw = iw; lih = ih; lastImage = image; lastBgColor = bgColor; }

    private void setPixels() {
        int[] dest = new int[width * height];
        if (lastDispose > 0) { if (lastDispose == 3) { int n = frameCount - 2; if (n > 0) lastImage = frames.get(n - 1).image; else lastImage = null; } if (lastImage != null) { lastImage.getRGB(0, 0, width, height, dest, 0, width); if (lastDispose == 2) { int c = 0; if (!transparency) c = lastBgColor; for (int i = 0; i < lih; i++) { int n1 = (liy + i) * width + lix; int n2 = n1 + liw; for (int k = n1; k < n2; k++) dest[k] = c; } } } }
        int pass = 1;
        int inc = 8;
        int iline = 0;
        for (int i = 0; i < ih; i++) {
            int line = i;
            if (line >= ih) {
                pass++;
                switch (pass) {
                    case 2: iline = 4; break;
                    case 3: iline = 2; inc = 4; break;
                    case 4: iline = 1; inc = 2; break;
                }
                line = iline;
            }
            line += iy;
            if (line < height) {
                int k = line * width;
                int dx = k + ix;
                int dlim = dx + iw;
                if ((k + width) < dlim) dlim = k + width;
                int sx = i * iw;
                while (dx < dlim) {
                    int index = ((int) pixels[sx++]) & 0xff;
                    int c = act[index];
                    if (c != 0) dest[dx] = c;
                    dx++;
                }
            }
        }
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, width, height, dest, 0, width);
    }
    private void skip() { do { readBlock(); } while ((blockSize > 0) && !err()); }
}