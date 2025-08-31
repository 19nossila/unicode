package unicode.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

public class CommonProxy implements IProxy {
    @Override
    public void init(FMLInitializationEvent event) {
        // Nada a fazer no servidor
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        // Nada a fazer no servidor
    }
}