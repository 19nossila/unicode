package unicode.proxy;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

public interface IProxy {
    public void init(FMLInitializationEvent event);
    public void postInit(FMLPostInitializationEvent event);
}