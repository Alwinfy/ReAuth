package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod("reauth")
public final class Main {

    static final Logger log = LogManager.getLogger("ReAuth");

    static boolean OfflineModeEnabled;

    public Main() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::preInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::securityError);
    }

    public void preInit(FMLClientSetupEvent evt) {
        MinecraftForge.EVENT_BUS.register(this);

        Main.loadConfig();

        Secure.init();
    }

    @SubscribeEvent
    public void onConfigChanged(OnConfigChangedEvent evt) {
        if (evt.getModID().equals("reauth")) {
            Main.loadConfig();
        }
    }

    /**
     * (re-)loads config
     */
    private static void loadConfig() {
        Secure.username = "";

        Secure.password = new char[0];

        Main.OfflineModeEnabled = true;

        GuiHandler.enabled = false;

        GuiHandler.bold = false;
    }

    public void securityError(FMLFingerprintViolationEvent event) {
        log.fatal("+-----------------------------------------------------------------------------------+");// @Replace()
        log.fatal("|The Version of ReAuth is not signed! It was modified! Ignoring because of Dev-Mode!|");// @Replace()
        log.fatal("+-----------------------------------------------------------------------------------+");// @Replace()
        // @Replace(throw new SecurityException("The Version of ReAuth is not signed! It is a modified version!");)
    }

}
