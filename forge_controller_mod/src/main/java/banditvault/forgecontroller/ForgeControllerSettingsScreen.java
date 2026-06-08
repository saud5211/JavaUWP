package banditvault.forgecontroller;

import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

public final class ForgeControllerSettingsScreen extends Screen {
    private final Screen parent;

    public ForgeControllerSettingsScreen(Screen parent) {
        super(ForgeControllerCompat.textLiteral("Bandit Controller"));
        this.parent = parent;
    }

    @Override
    protected void m_7856_() {
        rebuildButtons();
    }

    @Override
    public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.m_280273_(graphics);
        graphics.m_280653_(this.f_96547_, ForgeControllerCompat.textLiteral("Bandit Controller"), this.f_96543_ / 2, 18, 0xFFFFFF);
        super.m_88315_(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.m_7933_(keyCode, scanCode, modifiers);
    }

    public void close() {
        ForgeControllerSettings.save();
        if (this.f_96541_ != null) {
            this.f_96541_.m_91152_(parent);
        }
    }

    private void rebuildButtons() {
        clearWidgets();
        ForgeControllerSettings settings = ForgeControllerSettings.get();
        int x = this.f_96543_ / 2 - 105;
        int y = 42;

        addButton(ForgeControllerCompat.createButton(x, y, 210, 20, crouchLabel(settings), button -> {
            settings.toggleCrouch = !settings.toggleCrouch;
            ForgeControllerSettings.save();
            rebuildButtons();
        }));
        y += 24;

        addButton(ForgeControllerCompat.createButton(x, y, 210, 20, sprintLabel(settings), button -> {
            settings.toggleSprint = !settings.toggleSprint;
            ForgeControllerSettings.save();
            rebuildButtons();
        }));
        y += 24;

        addButton(ForgeControllerCompat.createButton(x, y, 210, 20, invertLabel(settings), button -> {
            settings.invertY = !settings.invertY;
            ForgeControllerSettings.save();
            rebuildButtons();
        }));
        y += 28;

        addStepper(x, y, "Look speed", Math.round(settings.lookSpeed), -15.0, 15.0, 30.0, 300.0, value -> settings.lookSpeed = (float) value);
        y += 24;
        addStepper(x, y, "Cursor speed", settings.cursorSpeed, -1.0, 1.0, 4.0, 40.0, value -> settings.cursorSpeed = value);
        y += 24;
        addStepper(x, y, "Scroll speed", settings.scrollAmount, -0.25, 0.25, 0.25, 4.0, value -> settings.scrollAmount = value);
        y += 24;
        addStepper(x, y, "Move deadzone", settings.moveDeadzone, -0.05, 0.05, 0.0, 0.75, value -> settings.moveDeadzone = (float) value);
        y += 24;
        addStepper(x, y, "Look deadzone", settings.lookDeadzone, -0.05, 0.05, 0.0, 0.75, value -> settings.lookDeadzone = (float) value);
        y += 24;
        addStepper(x, y, "Cursor deadzone", settings.cursorDeadzone, -0.05, 0.05, 0.0, 0.75, value -> settings.cursorDeadzone = (float) value);
        y += 30;

        addButton(ForgeControllerCompat.createButton(x, y, 210, 20, "Done", button -> close()));
    }

    private void addStepper(int x, int y, String label, double value, double down, double up, double min, double max, Setter setter) {
        addButton(ForgeControllerCompat.createButton(x, y, 42, 20, "-", button -> adjust(value, down, min, max, setter)));
        addButton(ForgeControllerCompat.createButton(x + 46, y, 118, 20, label + ": " + format(value), button -> {
        }));
        addButton(ForgeControllerCompat.createButton(x + 168, y, 42, 20, "+", button -> adjust(value, up, min, max, setter)));
    }

    private void adjust(double value, double delta, double min, double max, Setter setter) {
        double next = value + delta;
        if (next < min) {
            next = min;
        }
        if (next > max) {
            next = max;
        }
        setter.set(next);
        ForgeControllerSettings.save();
        rebuildButtons();
    }

    private void addButton(Button button) {
        this.m_142416_(button);
    }

    private void clearWidgets() {
        this.m_169413_();
    }

    private String crouchLabel(ForgeControllerSettings settings) {
        return "Crouch: " + (settings.toggleCrouch ? "Toggle" : "Hold");
    }

    private String sprintLabel(ForgeControllerSettings settings) {
        return "Sprint: " + (settings.toggleSprint ? "Toggle" : "Hold");
    }

    private String invertLabel(ForgeControllerSettings settings) {
        return "Invert Y: " + (settings.invertY ? "On" : "Off");
    }

    private String format(double value) {
        if (Math.abs(value - Math.round(value)) < 0.001) {
            return Long.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private interface Setter {
        void set(double value);
    }
}
