package technicianlp.reauth;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.SharedConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper; // bc selectionEnd isn't exposed

import java.util.Arrays;
import java.lang.reflect.Field;

@OnlyIn(Dist.CLIENT)
final class GuiPasswordField extends TextFieldWidget {
    private static Field selectionEndField = ObfuscationReflectionHelper.findField(TextFieldWidget.class, "field_146223_s");
    int getSelEnd() {
        try {
            return selectionEndField.getInt(this);
        } catch(ReflectiveOperationException e) {
            return getCursorPosition();
        }
    }

    GuiPasswordField(FontRenderer renderer, int posx, int posy, int x, int y, String text) {
        super(renderer, posx, posy, x, y, text);
        this.setMaxStringLength(512);
    }

    private char[] password = new char[0];

    final char[] getPW() {
        char[] pw = new char[password.length];
        System.arraycopy(password,0,pw,0,password.length);
        return pw;
    }

    public boolean keyPressed(int key, int scan, int mod) {
        if (!this.isFocused() || Screen.isCopy(key) || Screen.isCut(key))
            return false; // Prevent Cut/Copy
        return super.keyPressed(key, scan, mod); // combos handled by super
    }

    public final void writeText(String rawInput) {
        int selectionEnd = getSelEnd();
        int selStart = this.getCursorPosition() < selectionEnd ? this.getCursorPosition() : selectionEnd;
        int selEnd = this.getCursorPosition() < selectionEnd ? selectionEnd : this.getCursorPosition();

        char[] input = filterAllowedCharacters(rawInput).toCharArray();
        char[] newPW = new char[selStart + password.length - selEnd + input.length];

        if (password.length != 0 && selStart > 0)
            System.arraycopy(password, 0, newPW, 0, Math.min(selStart, password.length));

        System.arraycopy(input, 0, newPW, selStart, input.length);
        int l = input.length;


        if (password.length != 0 && selEnd < password.length)
            System.arraycopy(password, selEnd, newPW, selStart + input.length, password.length - selEnd);

        setPassword(newPW);
        Arrays.fill(newPW, 'f');
        this.moveCursorBy(selStart - selectionEnd + l);
    }

    @Override
    public final void deleteFromCursor(int num) {
        if (password.length == 0)
            return;
        if (getSelEnd() != this.getCursorPosition()) {
            this.writeText("");
        } else {
            boolean direction = num < 0;
            int start = direction ? Math.max(this.getCursorPosition() + num, 0) : this.getCursorPosition();
            int end = direction ? this.getCursorPosition() : Math.min(this.getCursorPosition() + num, password.length);

            char[] newPW = new char[start + password.length - end];


            if (start >= 0)
                System.arraycopy(password, 0, newPW, 0, start);

            if (end < password.length)
                System.arraycopy(password, end, newPW, start, password.length - end);

            setPassword(newPW);
            Arrays.fill(newPW,'f');
            if (direction)
                this.moveCursorBy(num);
        }
    }

    final void setPassword(char[] password) {
        Arrays.fill(this.password, 'f');
        this.password = new char[password.length];
        System.arraycopy(password, 0, this.password, 0, password.length);
        updateText();
    }

    @Override
    public final void setText(String textIn) {
        setPassword(textIn.toCharArray());
        updateText();
    }

    private void updateText() {
        char[] chars = new char[password.length];
        Arrays.fill(chars, '\u25CF');
        super.setText(new String(chars));
    }

    /**
     * Allow SectionSign to be input into the field
     */
    private boolean isAllowedCharacter(int character) {
        return character == 0xa7 || SharedConstants.isAllowedCharacter((char) character);
    }

    /**
     * Modified version of {@link ChatAllowedCharacters#filterAllowedCharacters(String)}
     */
    private String filterAllowedCharacters(String input) {
        StringBuilder stringbuilder = new StringBuilder();
        input.chars().filter(this::isAllowedCharacter).forEach(i -> stringbuilder.append((char) i));
        return stringbuilder.toString();
    }
}
