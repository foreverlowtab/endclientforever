package forever.end.client.ecf.hud;

import java.util.ArrayDeque;
import java.util.Deque;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Живые данные для HUD: счётчики кликов (CPS), история FPS и состояние клавиш (WASD + пробел).
 * Обновляется раз в кадр из игрового HUD-колбека (в меню не тикает).
 */
public final class HudData {
    private HudData() {}

    private static final Deque<Long> LEFT = new ArrayDeque<>();
    private static final Deque<Long> RIGHT = new ArrayDeque<>();
    private static boolean lDown, rDown;

    public static final int LEN = 60;
    private static final int[] FPS = new int[LEN];
    private static int idx = 0;
    private static long lastSample = 0L;

    // Состояние клавиш движения + счётчики нажатий.
    public static boolean kW, kA, kS, kD, kSpace;
    public static int cW, cA, cS, cD;
    private static boolean pW, pA, pS, pD;

    public static void update(Minecraft mc) {
        long win = mc.getWindow().getWindow();
        boolean l = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean r = GLFW.glfwGetMouseButton(win, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        long now = System.currentTimeMillis();
        if (l && !lDown) LEFT.addLast(now);
        if (r && !rDown) RIGHT.addLast(now);
        lDown = l;
        rDown = r;
        prune(LEFT, now);
        prune(RIGHT, now);
        if (now - lastSample >= 100L) {
            lastSample = now;
            FPS[idx] = mc.getFps();
            idx = (idx + 1) % LEN;
        }

        // Клавиши (читаем текущие привязки движения).
        kW = mc.options.keyUp.isDown();
        kA = mc.options.keyLeft.isDown();
        kS = mc.options.keyDown.isDown();
        kD = mc.options.keyRight.isDown();
        kSpace = mc.options.keyJump.isDown();
        if (kW && !pW) cW++;
        if (kA && !pA) cA++;
        if (kS && !pS) cS++;
        if (kD && !pD) cD++;
        pW = kW;
        pA = kA;
        pS = kS;
        pD = kD;
    }

    private static void prune(Deque<Long> d, long now) {
        while (!d.isEmpty() && now - d.peekFirst() > 1000L) d.removeFirst();
    }

    public static int leftCps() {
        prune(LEFT, System.currentTimeMillis());
        return LEFT.size();
    }

    public static int rightCps() {
        prune(RIGHT, System.currentTimeMillis());
        return RIGHT.size();
    }

    /** i = 0 (самый старый) .. LEN-1 (самый свежий). */
    public static int fpsAt(int i) {
        int j = ((idx + i) % LEN + LEN) % LEN;
        return FPS[j];
    }
}
