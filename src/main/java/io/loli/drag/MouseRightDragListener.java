package io.loli.drag;

import com.sun.jna.platform.win32.WinDef;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputAdapter;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static io.loli.drag.WindowUtils.*;
import static org.jnativehook.mouse.NativeMouseEvent.BUTTON1;
import static org.jnativehook.mouse.NativeMouseEvent.BUTTON2;

/**
 * @author chocotan
 */
public class MouseRightDragListener extends NativeMouseInputAdapter {
    private WinDef.HWND hwnd;
    private int[] mouseStartPoint;
    private int[] windowStartPoint;
    private int[] windowSize;
    private long[] mousePosition;
    private boolean pressed;

    private int dragType;
    private static ExecutorService dragSingleExecutor = Executors.newSingleThreadExecutor();
    private static List<Future<?>> dragQueue = new LinkedList<>();

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeEvent) {
        super.nativeMouseReleased(nativeEvent);
        pressed = false;
    }

    public void nativeMousePressed(NativeMouseEvent pressEvent) {
        if (pressEvent.getButton() != BUTTON2) {
            return;
        }
        hwnd = getWindowAtMouse();
        WinDef.HWND desktop = getDesktop();
        if (desktop == hwnd) {
            // TODO 桌面的处理
            return;
        }
        mouseStartPoint = new int[]{(int) pressEvent.getPoint().getX(), (int) pressEvent.getPoint().getY()};
        // if the window is maximum, restore it to the position
        if (isMaximum(hwnd)) {
            // TODO 还原，大小根据鼠标位移来判断
        }
        int windowPosition[] = getWindowRect(hwnd);
        double first = Math.pow(mouseStartPoint[0] - windowPosition[0], 2) + Math.pow(mouseStartPoint[1] - windowPosition[1], 2);
        double second = Math.pow(windowPosition[2] - mouseStartPoint[0], 2) + Math.pow(mouseStartPoint[1] - windowPosition[1], 2);
        double third = Math.pow(mouseStartPoint[0] - windowPosition[0], 2) + Math.pow(windowPosition[3] - mouseStartPoint[1], 2);
        double forth = Math.pow(windowPosition[2] - mouseStartPoint[0], 2) + Math.pow(windowPosition[3] - mouseStartPoint[1], 2);
        Double min = Stream.of(first, second, third, forth).mapToDouble(d -> d).min().orElseGet(() -> 0);
        if (first == min) {
            dragType = 1;
        }
        if (second == min) {
            dragType = 2;
        }
        if (third == min) {
            dragType = 3;
        }
        if (forth == min) {
            dragType = 4;
        }
        windowSize = new int[]{windowPosition[2] - windowPosition[0], windowPosition[3] - windowPosition[1]};
        windowStartPoint = new int[]{windowPosition[0], windowPosition[1]};
        mousePosition = getMousePosition();
        pressed = true;
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent dragEvent) {
        if (hwnd != null && pressed) {

            // search and cancel unfinished tasks
            dragQueue.parallelStream().filter(f -> !f.isDone())
                    .forEach(f -> f.cancel(true));
            dragQueue.clear();
            Future<?> submit = dragSingleExecutor.submit(() -> {
                int offsetX = dragEvent.getX() - mouseStartPoint[0];
                int offsetY = dragEvent.getY() - mouseStartPoint[1];
//                int x = windowStartPoint[0] + offsetX;
//                int y = windowStartPoint[1] + offsetY;
                int x = 0, y = 0, w = 0, h = 0;
                if (dragType == 1) {
                    x = windowStartPoint[0] + offsetX;
                    y = windowStartPoint[1] + offsetY;
                    w = windowSize[0] - offsetX;
                    h = windowSize[1] - offsetY;
                }
                if (dragType == 2) {
                    x = windowStartPoint[0];
                    y = windowStartPoint[1] + offsetY;
                    w = windowSize[0] + offsetX;
                    h = windowSize[1] - offsetY;
                }
                if (dragType == 3) {
                    x = windowStartPoint[0] + offsetX;
                    y = windowStartPoint[1];
                    w = windowSize[0] - offsetX;
                    h = windowSize[1] + offsetY;
                }
                if (dragType == 4) {
                    x = windowStartPoint[0];
                    y = windowStartPoint[1];
                    w = windowSize[0] + offsetX;
                    h = windowSize[1] + offsetY;
                }

                move(hwnd, x, y,
                        w,
                        h,
                        // TODO 可配置 NO_ACTIVE
                        0x0010);
            });
            dragQueue.add(submit);
        }
    }
}
