package unicode.core;

import cpw.mods.fml.client.registry.KeyBindingRegistry.KeyHandler;
import cpw.mods.fml.common.TickType;
import java.util.EnumSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class VirtualKeyboardKeyHandler extends KeyHandler {

    private static KeyBinding key = new KeyBinding("Abrir Teclado Unicode", Keyboard.KEY_V);

    public VirtualKeyboardKeyHandler() {
        super(new KeyBinding[]{key}, new boolean[]{false});
    }

    @Override
    public String getLabel() {
        return "UnicodeVirtualKeyboardHandler";
    }

    @Override
    public void keyDown(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd, boolean isRepeat) {
        if (tickEnd && kb == key) {
            
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
                
                Minecraft mc = Minecraft.getMinecraft();
                
                
                if (mc.currentScreen instanceof GuiEditSign || mc.currentScreen instanceof GuiChat) {
                    mc.displayGuiScreen(new GuiVirtualKeyboard(mc.currentScreen));
                }
               
            }
        }
    }

    @Override
    public void keyUp(EnumSet<TickType> types, KeyBinding kb, boolean tickEnd) {}

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.CLIENT);
    }
}