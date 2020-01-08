package technicianlp.reauth;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.DrawScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import java.awt.Color;

@Mod.EventBusSubscriber(modid = "reauth", value = Dist.CLIENT)
public final class GuiHandler {

    /**
     * Cache the Status for 5 Minutes
     */
    private static final CachedProperty<ValidationStatus> status = new CachedProperty<>(1000 * 60 * 5, ValidationStatus.Unknown);
    private static Thread validator;

    static boolean enabled = true;
    static boolean bold = true;

    @SubscribeEvent
    public static void open(InitGuiEvent e) {
        boolean run = false;
        if (e.getGui() instanceof MultiplayerScreen) {
            e.addWidget(new Button(5, 5, 100, 20, "Re-Login", GuiHandler::yeet));
            run = true;

            if (enabled && !status.check()) {
                if (validator != null)
                    validator.interrupt();
                validator = new Thread(() -> status.set(Secure.SessionValid() ? ValidationStatus.Valid : ValidationStatus.Invalid), "Session-Validator");
                validator.setDaemon(true);
                validator.start();
            }
        } else if (e.getGui() instanceof MainMenuScreen) {
            run = true;
            // Support for Custom Main Menu (add button outside of viewport)
            //e.addWidget(new Button(-50, -50, 20, 20, "ReAuth", GuiHandler::yeet));
        }
    }

    @SubscribeEvent(priority=net.minecraftforge.eventbus.api.EventPriority.HIGHEST)
    public static void draw(DrawScreenEvent.Post e) {
        if (enabled && e.getGui() instanceof MultiplayerScreen) {
            e.getGui().drawString(e.getGui().getMinecraft().fontRenderer, "Online:", 110, 10, 0xFFFFFFFF);
            ValidationStatus state = status.get();
            e.getGui().drawString(e.getGui().getMinecraft().fontRenderer, (bold ? ChatFormatting.BOLD : "") + state.text, 145, 10, state.color);
        }
    }

    public static void yeet(Object e) {
        Minecraft.getInstance().displayGuiScreen(new GuiLogin(null));
    }

    static void invalidateStatus() {
        status.invalidate();
    }

    private enum ValidationStatus {
        Unknown("?", Color.GRAY.getRGB()), Valid("\u2714", Color.GREEN.getRGB()), Invalid("\u2718", Color.RED.getRGB());

        private final String text;
        private final int color;

        ValidationStatus(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }

}
