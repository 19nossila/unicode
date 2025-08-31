package unicode;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import unicode.proxy.IProxy;

@Mod(modid = "UnicodeLib", name = "Unicode Font Library", version = "1.2.0-FINAL")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class UnicodeLib {

    @SidedProxy(clientSide = "unicode.proxy.ClientProxy", serverSide = "unicode.proxy.CommonProxy")
    public static IProxy proxy;

	@Init
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
	
	@PostInit
	public void postInit(FMLPostInitializationEvent event) {
	    proxy.postInit(event);
	}
}