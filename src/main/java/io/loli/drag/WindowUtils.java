package io.loli.drag;

import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser;

import static com.sun.jna.platform.win32.WinUser.GA_ROOT;
import static com.sun.jna.platform.win32.WinUser.SW_MAXIMIZE;
import static com.sun.jna.platform.win32.WinUser.SW_RESTORE;
import static io.loli.drag.Win32Extra.CWP_ALL;

public class WindowUtils {
    public static HWND getWindowAtMouse() {
        long[] getPos = new long[1];
        Win32Extra.INSTANCE.GetCursorPos(getPos);
        HWND hwnd = Win32Extra.INSTANCE.WindowFromPoint(getPos[0]);
        hwnd = User32Extra.INSTANCE.GetAncestor(hwnd, GA_ROOT);
        return hwnd;
    }

    public static long[] getMousePosition() {
        long[] getPos = new long[1];
        Win32Extra.INSTANCE.GetCursorPos(getPos);
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
        long[] getPos = new long[2];
        Win32Extra.INSTANCE.GetCursorPos(getPos);
        restore(hwnd);
        int position[] = getWindowRect(hwnd);
        // 设置window的新的坐标
        int x = (int) getPos[0] - position[2] / 2;
        int y = (int) getPos[2] - position[3] / 2;
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
}