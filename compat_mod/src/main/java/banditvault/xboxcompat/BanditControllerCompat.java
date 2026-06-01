package banditvault.xboxcompat;

import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_4587;
import net.minecraft.class_746;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

public final class BanditControllerCompat {
    private static final int GAMEPAD_ID = GLFW.GLFW_JOYSTICK_1;
    private static final float MOVE_THRESHOLD = 0.35f;
    private static final float LOOK_DEADZONE = 0.12f;
    private static final float LOOK_SPEED = 5.5f;
    private static final double CURSOR_SPEED = 14.0;
    private static final int LEFT_CLICK = 0;
    private static final int RIGHT_CLICK = 1;

    private static final GLFWGamepadState STATE = GLFWGamepadState.create();
    private static final boolean[] LAST_BUTTONS = new boolean[15];
    private static boolean loggedReady;
    private static boolean loggedLookReflectionFailure;
    private static boolean active;
    private static double cursorX = -1.0;
    private static double cursorY = -1.0;
    private static int scrollCooldown;

    private BanditControllerCompat() {
    }

    public static void tick(class_310 client) {
        if (client == null) {
            return;
        }

        if (!poll()) {
            if (active) {
                releaseGameplayKeys(client);
                active = false;
            }
            return;
        }

        active = true;
        if (!loggedReady) {
            loggedReady = true;
            XboxCompatLog.log("Bandit controller compat active");
        }

        if (client.field_1755 != null) {
            releaseGameplayKeys(client);
            tickScreen(client, client.field_1755);
        } else {
            tickGameplay(client);
        }

        copyButtons();
    }

    public static void renderCursor(class_437 screen, class_4587 matrices) {
        if (!active || screen == null || cursorX < 0.0 || cursorY < 0.0) {
            return;
        }

        int x = (int)Math.round(cursorX);
        int y = (int)Math.round(cursorY);
        class_332.method_25294(matrices, x - 5, y, x + 6, y + 1, 0xFFFFFFFF);
        class_332.method_25294(matrices, x, y - 5, x + 1, y + 6, 0xFFFFFFFF);
        class_332.method_25294(matrices, x - 3, y - 3, x + 4, y + 4, 0x66000000);
    }

    private static boolean poll() {
        try {
            return GLFW.glfwJoystickIsGamepad(GAMEPAD_ID) &&
                GLFW.glfwGetGamepadState(GAMEPAD_ID, STATE);
        } catch (Throwable t) {
            if (!loggedReady) {
                loggedReady = true;
                XboxCompatLog.logException("Bandit controller compat failed to poll GLFW gamepad", t);
            }
            return false;
        }
    }

    private static void tickGameplay(class_310 client) {
        class_315 options = client.field_1690;
        float lx = axis(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X);
        float ly = axis(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y);
        float rx = axis(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X);
        float ry = axis(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y);

        setHeld(options.field_1894, ly < -MOVE_THRESHOLD);
        setHeld(options.field_1881, ly > MOVE_THRESHOLD);
        setHeld(options.field_1913, lx < -MOVE_THRESHOLD);
        setHeld(options.field_1849, lx > MOVE_THRESHOLD);
        setHeld(options.field_1903, button(GLFW.GLFW_GAMEPAD_BUTTON_A));
        setHeld(options.field_1832, button(GLFW.GLFW_GAMEPAD_BUTTON_B));
        setHeld(options.field_1867, button(GLFW.GLFW_GAMEPAD_BUTTON_LEFT_BUMPER));
        setHeld(options.field_1886, trigger(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER));
        setHeld(options.field_1904, trigger(GLFW.GLFW_GAMEPAD_AXIS_LEFT_TRIGGER));
        setEdge(options.field_1822, pressed(GLFW.GLFW_GAMEPAD_BUTTON_Y));

        if (pressed(GLFW.GLFW_GAMEPAD_BUTTON_START)) {
            client.method_1490();
        }

        class_746 player = client.field_1724;
        if (player != null) {
            applyLook(player, rx, ry);
        }
    }

    private static void tickScreen(class_310 client, class_437 screen) {
        if (cursorX < 0.0 || cursorY < 0.0) {
            cursorX = Math.max(1, screen.field_22789 / 2);
            cursorY = Math.max(1, screen.field_22790 / 2);
        }

        float lx = axis(GLFW.GLFW_GAMEPAD_AXIS_LEFT_X);
        float ly = axis(GLFW.GLFW_GAMEPAD_AXIS_LEFT_Y);
        float rx = axis(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_X);
        float ry = axis(GLFW.GLFW_GAMEPAD_AXIS_RIGHT_Y);
        double dx = shapedCursorAxis(Math.abs(lx) > Math.abs(rx) ? lx : rx);
        double dy = shapedCursorAxis(Math.abs(ly) > Math.abs(ry) ? ly : ry);

        cursorX = clamp(cursorX + dx * CURSOR_SPEED, 0.0, Math.max(1, screen.field_22789 - 1));
        cursorY = clamp(cursorY + dy * CURSOR_SPEED, 0.0, Math.max(1, screen.field_22790 - 1));
        screen.method_16014(cursorX, cursorY);

        if (pressed(GLFW.GLFW_GAMEPAD_BUTTON_A)) {
            screen.method_25402(cursorX, cursorY, LEFT_CLICK);
        }
        if (released(GLFW.GLFW_GAMEPAD_BUTTON_A)) {
            screen.method_25406(cursorX, cursorY, LEFT_CLICK);
        }
        if (pressed(GLFW.GLFW_GAMEPAD_BUTTON_X)) {
            screen.method_25402(cursorX, cursorY, RIGHT_CLICK);
        }
        if (released(GLFW.GLFW_GAMEPAD_BUTTON_X)) {
            screen.method_25406(cursorX, cursorY, RIGHT_CLICK);
        }
        if (pressed(GLFW.GLFW_GAMEPAD_BUTTON_B)) {
            if (!screen.method_25404(GLFW.GLFW_KEY_ESCAPE, 0, 0)) {
                client.method_1507(null);
            }
        }

        if (scrollCooldown > 0) {
            scrollCooldown--;
        }
        if (scrollCooldown == 0) {
            double scroll = 0.0;
            if (button(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_UP)) {
                scroll = 1.0;
            } else if (button(GLFW.GLFW_GAMEPAD_BUTTON_DPAD_DOWN)) {
                scroll = -1.0;
            }
            if (scroll != 0.0) {
                screen.method_25401(cursorX, cursorY, scroll);
                scrollCooldown = 5;
            }
        }
    }

    private static void applyLook(class_746 player, float rx, float ry) {
        float lookX = shapedLookAxis(rx);
        float lookY = shapedLookAxis(ry);
        if (lookX == 0.0f && lookY == 0.0f) {
            return;
        }

        try {
            float yaw = getEntityFloat(player, "field_6031");
            float pitch = getEntityFloat(player, "field_5965");
            setEntityFloat(player, "field_6031", yaw + lookX * LOOK_SPEED);
            setEntityFloat(player, "field_5965", (float)clamp(pitch + lookY * LOOK_SPEED, -90.0, 90.0));
        } catch (Throwable t) {
            if (!loggedLookReflectionFailure) {
                loggedLookReflectionFailure = true;
                XboxCompatLog.logException("Bandit controller compat failed to apply look", t);
            }
        }
    }

    private static void releaseGameplayKeys(class_310 client) {
        class_315 options = client.field_1690;
        setHeld(options.field_1894, false);
        setHeld(options.field_1881, false);
        setHeld(options.field_1913, false);
        setHeld(options.field_1849, false);
        setHeld(options.field_1903, false);
        setHeld(options.field_1832, false);
        setHeld(options.field_1867, false);
        setHeld(options.field_1886, false);
        setHeld(options.field_1904, false);
    }

    private static void setHeld(class_304 key, boolean held) {
        if (key == null) {
            return;
        }
        key.method_23481(held);
        class_304.method_1416(key.method_1429(), held);
    }

    private static void setEdge(class_304 key, boolean pressed) {
        if (pressed) {
            setHeld(key, true);
        } else if (released(GLFW.GLFW_GAMEPAD_BUTTON_Y)) {
            setHeld(key, false);
        }
    }

    private static float axis(int index) {
        return clampAxis(STATE.axes(index));
    }

    private static boolean trigger(int index) {
        return axis(index) > 0.25f;
    }

    private static boolean button(int index) {
        return STATE.buttons(index) == GLFW.GLFW_PRESS;
    }

    private static boolean pressed(int index) {
        return button(index) && !LAST_BUTTONS[index];
    }

    private static boolean released(int index) {
        return !button(index) && LAST_BUTTONS[index];
    }

    private static void copyButtons() {
        for (int i = 0; i < LAST_BUTTONS.length; i++) {
            LAST_BUTTONS[i] = button(i);
        }
    }

    private static float shapedLookAxis(float value) {
        value = clampAxis(value);
        float abs = Math.abs(value);
        if (abs < LOOK_DEADZONE) {
            return 0.0f;
        }
        float shaped = (abs - LOOK_DEADZONE) / (1.0f - LOOK_DEADZONE);
        shaped = shaped * shaped;
        return Math.copySign(shaped, value);
    }

    private static double shapedCursorAxis(float value) {
        value = clampAxis(value);
        float abs = Math.abs(value);
        if (abs < 0.12f) {
            return 0.0;
        }
        return Math.copySign((abs - 0.12f) / 0.88f, value);
    }

    private static float clampAxis(float value) {
        if (value < -1.0f) {
            return -1.0f;
        }
        if (value > 1.0f) {
            return 1.0f;
        }
        return value;
    }

    private static float getEntityFloat(Object value, String fieldName) throws ReflectiveOperationException {
        java.lang.reflect.Field field = findField(value.getClass(), fieldName);
        field.setAccessible(true);
        return field.getFloat(value);
    }

    private static void setEntityFloat(Object value, String fieldName, float fieldValue) throws ReflectiveOperationException {
        java.lang.reflect.Field field = findField(value.getClass(), fieldName);
        field.setAccessible(true);
        field.setFloat(value, fieldValue);
    }

    private static java.lang.reflect.Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
