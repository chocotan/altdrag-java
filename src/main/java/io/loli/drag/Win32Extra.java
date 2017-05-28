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
