package io.loli.drag;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.W32APIOptions;

import static com.sun.jna.platform.win32.WinDef.*;
import static io.loli.drag.Win32Extra.CWP_ALL;

/**
 * @author chocotan
 */
public interface Win32Extra extends W32APIOptions {
    public static int CWP_ALL = 0x0000;
    Win32Extra INSTANCE = (Win32Extra) Native.loadLibrary("user32", Win32Extra.class, DEFAULT_OPTIONS);

    boolean GetCursorPos(long[] lpPoint);

    HWND WindowFromPoint(long point);

    boolean ScreenToClient(HWND hWnd, long[] lpPoint);//use macros POINT_X() and POINT_Y() on long lpPoint[0]

    HWND ChildWindowFromPointEx(HWND hwndParent, long point, int uFlags);
}

class Win32Utils {
    public static HWND getWindowFromCursorPos() {
        long[] getPos = new long[1];
        Win32Extra.INSTANCE.GetCursorPos(getPos);
        HWND hwnd = Win32Extra.INSTANCE.WindowFromPoint(getPos[0]);
//        HWND childHwnd = getHiddenChildWindowFromPoint(hwnd, getPos[0]);
//        hwnd = childHwnd;
        return hwnd;
    }

    public static HWND getHiddenChildWindowFromPoint(HWND inHwnd, long point) {
        long[] getPos = new long[1];
        getPos[0] = point;
        if (!Win32Extra.INSTANCE.ScreenToClient(inHwnd, getPos))
            return inHwnd; // if point is not correct use original hwnd.
        HWND childHwnd = Win32Extra.INSTANCE.ChildWindowFromPointEx(inHwnd, getPos[0], CWP_ALL);
        if (childHwnd == null) // if childHwnd is not correct use original hwnd.
            return inHwnd;
        return childHwnd;
    }
}