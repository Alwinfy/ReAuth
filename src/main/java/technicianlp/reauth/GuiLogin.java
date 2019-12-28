package technicianlp.reauth;

import com.mojang.authlib.exceptions.AuthenticationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

final class GuiLogin extends Screen {

    private TextFieldWidget username;
    private TextFieldWidget pw;
    //private GuiPasswordField pw;
    private Button login;
    private Button cancel;
    private Button offline;
    private Button config;

    private Screen prev;

    private int basey;

    private String message = "";

    GuiLogin(Screen prev) {
		super(new net.minecraft.util.text.TranslationTextComponent("demo.help.title", new Object[0]));
		Minecraft.getInstance().keyboardListener.enableRepeatEvents(true);
		this.prev = prev;
    }

    @Override
    public void render(int x, int y, float p) {
        renderDirtBackground(0);

        drawCenteredString(font, "Username/E-Mail:", width / 2, basey,
                Color.WHITE.getRGB());
        drawCenteredString(font, "Password:", width / 2, basey + 45,
                Color.WHITE.getRGB());
        if (!(message == null || message.isEmpty())) {
            drawCenteredString(font, message, width / 2, basey - 15, 0xFFFFFF);
        }
        username.render(x, y, p);
        pw.render(x, y, p);

        super.render(x, y, p);
    }

    @Override
    public void init() {
        super.init();

        basey = height / 2 - 110 / 2;

        username = new TextFieldWidget(font, width / 2 - 155, basey + 15, 2 * 155, 20, Secure.username);
        username.setMaxStringLength(512);
        username.setFocused2(true);
        username.setEnabled(true);
        username.setVisible(true);
        username.setCanLoseFocus(true);

        pw = new TextFieldWidget(font, width / 2 - 155, basey + 60, 2 * 155, 20, "");
        pw.setEnabled(true);
        pw.setVisible(true);
        pw.setCanLoseFocus(true);
	
        this.children.add(username);
        this.children.add(pw);
	this.setFocusedDefault(username);

        login = new Button(width / 2 - 155, basey + 105, 100, 20, "Login", x -> {if(login()) minecraft.displayGuiScreen(prev);});
        offline = new Button(width / 2 - 50, basey + 105, 100, 20, "Play Offline", x -> {if(playOffline()) minecraft.displayGuiScreen(prev);});
        cancel = new Button(width / 2 + 55, basey + 105, 100, 20, "Cancel", x -> minecraft.displayGuiScreen(prev));
        addButton(login);
        addButton(offline);
        addButton(cancel);
    }

    public void tick() {
        this.username.tick();
        this.pw.tick();
    }

    /*@Override
    public boolean charTyped(char c, int mod) {
        if (c == '\t') {
            this.username.setFocused2(!this.username.isFocused());
            this.pw.setFocused2(!this.pw.isFocused());
            return true;
        } else if (c == '\n' || c == '\r') {
            if (this.username.isFocused()) {
                this.username.setFocused2(false);
                this.pw.setFocused2(true);
            } else if (this.pw.isFocused()) {
                this.login();
            }
            return true;
        }
        boolean k = super.charTyped(c, mod);
        this.username.charTyped(c, mod);
        this.pw.charTyped(c, mod);
        return k;
    }*/

    /*@Override
    public boolean keyPressed(int key, int scan, int mod) {
        if (key == GLFW.GLFW_KEY_TAB) {
            this.username.setFocused2(!this.username.isFocused());
            this.pw.setFocused2(!this.pw.isFocused());
            return true;
        } else if (key == GLFW.GLFW_KEY_ENTER) {
            if (this.username.isFocused()) {
                this.username.setFocused2(false);
                this.pw.setFocused2(true);
            } else if (this.pw.isFocused()) {
                this.login();
            }
            return true;
        }
        boolean k = super.keyPressed(key, scan, mod);
        this.username.keyPressed(key, scan, mod);
        this.pw.keyPressed(key, scan, mod);
        return k;
    }*/
    /**
     * used as an interface between this and the secure class
     * <p>
     * returns whether the login was successful
     */
    private boolean login() {
        try {
            Secure.login(username.getText(), pw.getText().toCharArray(), false);
            message = (char) 167 + "aLogin successful!";
            return true;
        } catch (AuthenticationException e) {
            message = (char) 167 + "4Login failed: " + e.getMessage();
            Main.log.error("Login failed:", e);
            return false;
        } catch (Exception e) {
            message = (char) 167 + "4Error: Something went wrong!";
            Main.log.error("Error:", e);
            return false;
        }
    }

    /**
     * sets the name for playing offline
     */
    private boolean playOffline() {
        String username = this.username.getText();
        if (!(username.length() >= 2 && username.length() <= 16)) {
            message = (char) 167 + "4Error: Username needs a length between 2 and 16";
            return false;
        }
        if (!username.matches("[A-Za-z0-9_]{2,16}")) {
            message = (char) 167 + "4Error: Username has to be alphanumerical";
            return false;
        }
        try {
            Secure.offlineMode(username);
            return true;
        } catch (Exception e) {
            message = (char) 167 + "4Error: Something went wrong!";
            Main.log.error("Error:", e);
            return false;
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        pw.setText("");
        Minecraft.getInstance().keyboardListener.enableRepeatEvents(false);
		Minecraft.getInstance().displayGuiScreen(prev);
    }
}
