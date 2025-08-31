package unicode.core;

import java.util.EnumSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ChatInjector implements ITickHandler {

    private boolean injected = false;

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {

    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        if (!injected && type.contains(TickType.RENDER)) {
            Minecraft mc = Minecraft.getMinecraft();
            

            if (mc != null && mc.ingameGUI != null) {
                try {
                    CustomGuiNewChat customChat = new CustomGuiNewChat(mc);


                    String[] fieldNames = {"field_73840_e"};
                    ObfuscationReflectionHelper.setPrivateValue(GuiIngame.class, mc.ingameGUI, customChat, fieldNames);
                    
                    System.out.println("[UnicodeLib] CustomGuiNewChat injetado com sucesso via TickHandler!");
                    this.injected = true;

                } catch (Exception e) {
                    System.err.println("[UnicodeLib] FALHA CRiTICA ao injetar CustomGuiNewChat via TickHandler!");
                    e.printStackTrace();
                    this.injected = true;
                }
            }
        }
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.RENDER);
    }

    @Override
    public String getLabel() {
        return "UnicodeLibChatInjector";
    }
}