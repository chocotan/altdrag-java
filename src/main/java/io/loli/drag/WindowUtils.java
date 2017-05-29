package io.loli.drag;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;
import org.jnativehook.NativeInputEvent;
import org.jnativehook.mouse.NativeMouseEvent;

import java.lang.reflect.Field;
import java.util.Arrays;

import static com.sun.jna.platform.win32.WinUser.*;
import static io.loli.drag.Win32Extra.CWP_ALL;

public class WindowUtils {
    public static HWND getWindowAtMouse() {
        long[] getPos = new long[1];
        Win32Extra.INSTANCE.GetCursorPos(getPos);
        HWND hwnd = Win32Extra.INSTANCE.WindowFromPoint(getPos[0]);
        hwnd = User32Extra.INSTANCE.GetAncestor(hwnd, GA_ROOT);
        return hwnd;
    }

    public static boolean isDesktop(HWND hwnd) {
//        alt-drag-test
//        HWND testHwnd = User32Extra.INSTANCE.FindWindow(null, "alt-drag-test");
//        HWND testParent = User32Extra.INSTANCE.GetAncestor(testHwnd, GA_PARENT);
//        HWND hwndParent = User32Extra.INSTANCE.GetAncestor(hwnd, GA_PARENT);
//        HWND root =  User32Extra.INSTANCE.GetAncestor(hwnd, GA_ROOT);
//        HWND ow =  User32Extra.INSTANCE.GetAncestor(hwnd, GA_ROOTOWNER);
//        System.out.println(hwnd);
//        System.out.println(hwndParent);
//        System.out.println(root);
//        System.out.println(ow);
////        System.out.println(testParent==hwnd);
        return false;
    }

    public static int[] getMousePosition() {
        int[] getPos = new int[2];
        User32Extra.INSTANCE.GetCursorPos(getPos);
        return getPos;
    }

    public static HWND getDesktop() {
        return User32Extra.INSTANCE.GetDesktopWindow();
    }


    public static int[] getWindowRect(HWND hwnd) {
        int position[] = new int[4];
        User32Extra.INSTANCE.GetWindowRect(hwnd, position);
        return position;
    }

    public static void restore(HWND hwnd) {
        User32Extra.INSTANCE.ShowWindow(hwnd, SW_RESTORE);
    }

    public static void maximize(HWND hwnd) {
        User32Extra.INSTANCE.ShowWindow(hwnd, SW_MAXIMIZE);
    }

    public static void restoreToMouse(HWND hwnd) {
        // 判断是否是最大化，如果是最大化的话，则还原
        // 窗口目前是最大化状态
        // 还原窗口并设置坐标为当前鼠标所在位置
        int[] getPos = getMousePosition();
        restore(hwnd);
        int position[] = getWindowRect(hwnd);
        // 设置window的新的坐标
        int x = getPos[0] - (position[2] - position[0]) / 2;
        int y = getPos[1] - ((position[3] - position[1]) / 2);
        System.out.println(getPos[1] + "," + Arrays.toString(position));
        move(hwnd, x, y,
                position[2] - position[0],
                position[3] - position[1],
                0x0010);
    }


    public static boolean isMaximum(HWND hwnd) {
        WinUser.WINDOWPLACEMENT lpwndpl = new WinUser.WINDOWPLACEMENT();
        User32Extra.INSTANCE.GetWindowPlacement(hwnd, lpwndpl);
        return SW_MAXIMIZE == lpwndpl.showCmd;
    }

    public static void move(HWND hwnd, int x, int y, int width, int height, int uFlags) {
        User32Extra.INSTANCE.SetWindowPos(hwnd, 0, x, y,
                width,
                height,
                uFlags);
    }

    public static void preventDefault(NativeMouseEvent event) {
        try {
            Field f = NativeInputEvent.class.getDeclaredField("reserved");
            f.setAccessible(true);
            f.setShort(event, (short) 0x01);
        } catch (Exception ignored) {
        }
    }
}