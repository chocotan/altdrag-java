package io.loli.drag;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyAdapter;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputAdapter;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import static com.sun.jna.platform.win32.WinUser.*;

public class AltDragApplication {
    private static ExecutorService dragSingleExecutor = Executors.newSingleThreadExecutor();
    private static ReentrantLock lock = new ReentrantLock();
    private static NativeMouseInputListener nativeMouseInputListener =
            new NativeMouseInputAdapter() {
                private WinDef.HWND hwnd;
                private Point mouseStartPoint;
                private Point windowStartPoint;
                private Point windowSize;

                @Override
                public void nativeMouseReleased(NativeMouseEvent nativeEvent) {
                    super.nativeMouseReleased(nativeEvent);
                }

                public void nativeMousePressed(NativeMouseEvent pressEvent) {
                    // TODO 判断是否到达屏幕边缘，到达屏幕边缘后触发最大化
                    // TODO 如果hwnd是桌面，则不处理
                    // TODO 判断是鼠标左键
                    // TODO 右键拖拽的处理
                    long[] getPos = new long[1];
                    Win32Extra.INSTANCE.GetCursorPos(getPos);
                    hwnd = Win32Extra.INSTANCE.WindowFromPoint(getPos[0]);
                    hwnd = User32Extra.INSTANCE.GetAncestor(hwnd, GA_ROOT);
                    WinUser.WINDOWPLACEMENT lpwndpl = new WinUser.WINDOWPLACEMENT();
                    User32Extra.INSTANCE.GetWindowPlacement(hwnd, lpwndpl);
                    // 记录鼠标点击时的坐标
                    mouseStartPoint = pressEvent.getPoint();
                    if (SW_MAXIMIZE == lpwndpl.showCmd) {
                        // 判断是否是最大化，如果是最大化的话，则还原
                        // 窗口目前是最大化状态
                        // 还原窗口并设置坐标为当前鼠标所在位置
                        User32Extra.INSTANCE.ShowWindow(hwnd, SW_RESTORE);
                        // TODO 获取还原之后的窗口大小
                        // TODO 计算目标坐标
                        int position[] = new int[4];
                        User32Extra.INSTANCE.GetWindowRect(hwnd, position);
                        // 设置window的新的坐标
                        int x = (int) mouseStartPoint.getX() - position[2] / 2;
                        int y = (int) mouseStartPoint.getY() - position[3] / 2;
                        User32Extra.INSTANCE.SetWindowPos(hwnd, 0, x, y,
                                position[2] - position[0],
                                position[3] - position[1],
                                // TODO 可配置 NO_ACTIVE
                                0x0010);
                    }

                    // 记录鼠标点击时的window初始坐标;
                    int windowPosition[] = new int[4];
                    int clientPosition[] = new int[4];
                    User32Extra.INSTANCE.GetWindowRect(hwnd, windowPosition);
                    User32Extra.INSTANCE.GetClientRect(hwnd, clientPosition);
                    windowSize = new Point(windowPosition[2] - windowPosition[0], windowPosition[3] - windowPosition[1]);
                    int[] rect = {0, 0};
                    User32Extra.INSTANCE.ClientToScreen(hwnd, rect);
                    windowStartPoint = new Point(windowPosition[0], windowPosition[1]);
                }

                @Override
                public void nativeMouseDragged(NativeMouseEvent dragEvent) {
                    // TODO 判断是鼠标左键
                    if (hwnd != null) {
//                    super.nativeMouseDragged(dragEvent);
                        // 查找未处理完成的任务，将其取消，然后将列表清空
                        dragQueue.parallelStream().filter(f -> !f.isDone())
                                .forEach(f -> f.cancel(true));
                        dragQueue.clear();
                        Future<?> submit = dragSingleExecutor.submit(() -> {
                            // 鼠标drag时计算移动距离，然后移动windows对象
                            // X轴偏移量
                            int offsetX = dragEvent.getX() - (int) mouseStartPoint.getX();
                            // Y轴偏移量
                            int offsetY = dragEvent.getY() - (int) mouseStartPoint.getY();
                            // 新的坐标
                            int x = (int) windowStartPoint.getX() + offsetX;
                            int y = (int) windowStartPoint.getY() + offsetY;
                            // 设置window的新的坐标
                            // moveWindow
                            User32Extra.INSTANCE.SetWindowPos(hwnd, 0, x, y,
                                    (int) windowSize.getX(),
                                    (int) windowSize.getY(),
                                    // TODO 可配置 NO_ACTIVE
                                    0x0010);
                            System.out.println(windowSize);
                        });
                        dragQueue.add(submit);
                    }
                }
            };

    private static List<Future<?>> dragQueue = new LinkedList<>();

    private static Queue<Future<?>> dragQueue2 = new LinkedList<>();

    private static final Logger logger = LoggerFactory.getLogger(AltDragApplication.class);

    public static void main(String[] args) throws InterruptedException {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        // Add the appropriate listeners.
        GlobalScreen.addNativeKeyListener(nativeKeyListener);
        Thread.sleep(Integer.MAX_VALUE);
    }

    public static final int POINT_Y(long i) {
        return (int) (i >> 32);
    }

    public static final int POINT_X(long i) {
        return (int) (i & 0xFFFF);
    }

    static NativeKeyListener nativeKeyListener = new NativeKeyAdapter() {
        @Override
        public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
            super.nativeKeyPressed(nativeKeyEvent);
            if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_ALT &&
                    !lock.isLocked()) {
                lock.tryLock();
                // 开始监听左键
                System.out.println(Thread.currentThread().getId() + "Alt pressed");
                GlobalScreen.addNativeMouseListener(nativeMouseInputListener);
                GlobalScreen.addNativeMouseMotionListener(nativeMouseInputListener);
            }
        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
            super.nativeKeyReleased(nativeKeyEvent);
            if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_ALT && lock.isLocked()) {
                lock.unlock();
                // 取消监听左键
                GlobalScreen.removeNativeMouseListener(nativeMouseInputListener);
                GlobalScreen.removeNativeMouseMotionListener(nativeMouseInputListener);
            }
        }
    };
}
