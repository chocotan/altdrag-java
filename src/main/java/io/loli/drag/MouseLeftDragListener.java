package io.loli.drag;

import com.sun.jna.platform.win32.WinDef;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputAdapter;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.loli.drag.WindowUtils.*;
import static io.loli.drag.WindowUtils.move;
import static org.jnativehook.mouse.NativeMouseEvent.BUTTON1;

/**
 * @author chocotan
 */
public class MouseLeftDragListener extends NativeMouseInputAdapter {
    private WinDef.HWND hwnd;
    private int[] mouseStartPoint;
    private int[] windowStartPoint;
    private int[] windowSize;
    private static ExecutorService dragSingleExecutor = Executors.newSingleThreadExecutor();
    private static List<Future<?>> dragQueue = new LinkedList<>();

    private boolean pressed = false;

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeEvent) {
        super.nativeMouseReleased(nativeEvent);
        pressed = false;
    }

    public void nativeMousePressed(NativeMouseEvent pressEvent) {
        if (pressEvent.getButton() != BUTTON1) {
            return;
        }
        hwnd = getWindowAtMouse();
        WinDef.HWND desktop = getDesktop();
        if (desktop == hwnd) {
            return;
        }
        // TODO 右键拖拽的处理
        mouseStartPoint = new int[]{(int) pressEvent.getPoint().getX(), (int) pressEvent.getPoint().getY()};
        // if the window is maximum, restore it to the position
        if (isMaximum(hwnd)) {
            restoreToMouse(hwnd);
        }
        int windowPosition[] = getWindowRect(hwnd);
        windowSize = new int[]{windowPosition[2] - windowPosition[0], windowPosition[3] - windowPosition[1]};
        windowStartPoint = new int[]{windowPosition[0], windowPosition[1]};
        pressed = true;
    }


    @Override
    public void nativeMouseDragged(NativeMouseEvent dragEvent) {
        if (hwnd != null && pressed) {
            // search and cancel unfinished tasks
            dragQueue.parallelStream().filter(f -> !f.isDone())
                    .forEach(f -> f.cancel(true));
            dragQueue.clear();
            // maximize the window while mouse moves to top of screen
            if (dragEvent.getY() == 0) {
                maximize(hwnd);
                return;
            }

            Future<?> submit = dragSingleExecutor.submit(() -> {
                int offsetX = dragEvent.getX() - mouseStartPoint[0];
                int offsetY = dragEvent.getY() - mouseStartPoint[1];
                int x = windowStartPoint[0] + offsetX;
                int y = windowStartPoint[1] + offsetY;
                move(hwnd, x, y,
                        windowSize[0],
                        windowSize[1],
                        // TODO 可配置 NO_ACTIVE
                        0x0010);
            });
            dragQueue.add(submit);
        }
    }
}
