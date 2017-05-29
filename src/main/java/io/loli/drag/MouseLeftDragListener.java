package io.loli.drag;

import com.sun.jna.platform.win32.WinDef;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
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

    private static final Logger logger = LoggerFactory.getLogger(MouseLeftDragListener.class);
    private volatile boolean pressed = false;

    public void nativeMouseClicked(NativeMouseEvent nativeEvent) {
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeEvent) {
        if (nativeEvent.getButton() == BUTTON1) {
            super.nativeMouseReleased(nativeEvent);
            pressed = false;
        }
    }

    public void nativeMousePressed(NativeMouseEvent pressEvent) {
        if (pressEvent.getButton() != BUTTON1) {
            return;
        }
        hwnd = getWindowAtMouse();
        if (isFullScreen(hwnd)) {
            logger.warn("Current window is in full screen");
            return;
        }
        mouseStartPoint = new int[]{(int) pressEvent.getPoint().getX(), (int) pressEvent.getPoint().getY()};
        // if the window is maximum, restore it to the position
        if (isMaximum(hwnd)) {
            restoreToMouse(hwnd);
            logger.info("Restore the window, mouse position:{}x{}", mouseStartPoint[0], mouseStartPoint[1]);
        }
        int windowPosition[] = getWindowRect(hwnd);
        windowSize = new int[]{windowPosition[2] - windowPosition[0], windowPosition[3] - windowPosition[1]};
        windowStartPoint = new int[]{windowPosition[0], windowPosition[1]};
        pressed = true;
        preventDefault(pressEvent);
        logger.info("Start drag, mouse position:{}x{}, window position:{}x{}x{}x{}"
                , mouseStartPoint[0], mouseStartPoint[1],
                windowPosition[0], mouseStartPoint[1], mouseStartPoint[2], mouseStartPoint[3]);

    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeEvent) {
        if (pressed) {
            nativeMouseDragged(nativeEvent);
        }
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent dragEvent) {
        if (hwnd != null && pressed) {
            // search and cancel unfinished tasks
            dragQueue.parallelStream().filter(f -> !f.isDone())
                    .forEach(f -> f.cancel(true));
            dragQueue.clear();
            // maximize the window while mouse moves to top of screen
            if (dragEvent.getY() < 0) {
                if (!isMaximum(hwnd)) {
                    maximize(hwnd);
                    logger.info("Maximize the window, mouse position:{}x{}", mouseStartPoint[0], mouseStartPoint[1]);
                }
                return;
            }
            // if the window is maximum, restore it to the position
            if (isMaximum(hwnd)) {
                restoreToMouse(hwnd);
                logger.info("Restore the window, mouse position:{}x{}", mouseStartPoint[0], mouseStartPoint[1]);
                return;
            }
            int offsetX = dragEvent.getX() - mouseStartPoint[0];
            int offsetY = dragEvent.getY() - mouseStartPoint[1];
            int x = windowStartPoint[0] + offsetX;
            int y = windowStartPoint[1] + offsetY;
            Future<?> submit = dragSingleExecutor.submit(() -> {
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
