package io.loli.drag;

import com.sun.jna.platform.win32.WinDef;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private volatile boolean pressed;
    private static final Logger logger = LoggerFactory.getLogger(MouseLeftDragListener.class);


    private int dragType;
    private static ExecutorService dragSingleExecutor = Executors.newSingleThreadExecutor();
    private static List<Future<?>> dragQueue = new LinkedList<>();

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeEvent) {
        if (nativeEvent.getButton() == BUTTON2) {
            super.nativeMouseReleased(nativeEvent);
            pressed = false;
        }
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeEvent) {
        if (pressed) {
            nativeMouseDragged(nativeEvent);
        }
    }


    public void nativeMousePressed(NativeMouseEvent pressEvent) {
        if (pressEvent.getButton() != BUTTON2) {
            return;
        }
        hwnd = getWindowAtMouse();
        if (isFullScreen(hwnd)) {
            logger.warn("Current window is in full screen");
            return;
        }
        mouseStartPoint = new int[]{(int) pressEvent.getPoint().getX(), (int) pressEvent.getPoint().getY()};
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
        pressed = true;
        preventDefault(pressEvent);
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent dragEvent) {
        if (hwnd != null && pressed) {

            // search and cancel unfinished tasks
            dragQueue.parallelStream().filter(f -> !f.isDone())
                    .forEach(f -> f.cancel(true));
            dragQueue.clear();
            int offsetX = dragEvent.getX() - mouseStartPoint[0];
            int offsetY = dragEvent.getY() - mouseStartPoint[1];
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
            int finalX = x;
            int finalY = y;
            int finalW = w;
            int finalH = h;
            Future<?> submit = dragSingleExecutor.submit(() -> {
                move(hwnd, finalX, finalY,
                        finalW,
                        finalH,
                        // TODO 可配置 NO_ACTIVE
                        0x0010);
            });
            dragQueue.add(submit);
            logger.debug("Resize to {}x{}x{}x{}", x, y, w, h);
        }
    }
}
