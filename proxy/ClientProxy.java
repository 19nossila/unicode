package unicode.proxy;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.KeyBindingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntitySign;
import unicode.api.UnicodeAPI;
import unicode.core.AnimationManager;
import unicode.core.ChatInjector;
import unicode.core.CustomFontRenderer;
import unicode.core.CustomSignRenderer;

public class ClientProxy implements IProxy {
    @Override
    public void init(FMLInitializationEvent event) {

        TickRegistry.registerTickHandler(new AnimationManager(), Side.CLIENT);
        KeyBindingRegistry.registerKeyBinding(new unicode.core.VirtualKeyboardKeyHandler());
        
        TickRegistry.registerTickHandler(new ChatInjector(), Side.CLIENT);
        
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        System.out.println("[UnicodeLib] Injetando codigo de cliente...");
        try {
            Minecraft mc = Minecraft.getMinecraft();
            

            mc.fontRenderer = new CustomFontRenderer(
                mc.gameSettings,
                "/font/default.png",
                mc.renderEngine,
                false
            );

            int requiredHeight = (int)Math.ceil(UnicodeAPI.getMaxRegisteredHeight());
            
            if (requiredHeight > mc.fontRenderer.FONT_HEIGHT) {
                mc.fontRenderer.FONT_HEIGHT = requiredHeight;
                System.out.println("[UnicodeLib] Altura base do FONT ajustada para " + requiredHeight + " pixels para acomodar emojis.");
            }
            
            System.out.println("[UnicodeLib] Injecao do FontRenderer bem-sucedida!");

            ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySign.class, new CustomSignRenderer());
            System.out.println("[UnicodeLib] Injecao do SignRenderer bem-sucedida!");

        } catch (Exception e) {
            System.err.println("[UnicodeLib] FALHA na injecao do codigo de cliente!");
            e.printStackTrace();
        }
    }
}