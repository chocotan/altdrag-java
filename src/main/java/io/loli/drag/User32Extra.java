package io.loli.drag;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.win32.W32APIOptions;

public interface User32Extra extends User32 {
    User32Extra INSTANCE = (User32Extra) Native.loadLibrary("user32",
            User32Extra.class, W32APIOptions.DEFAULT_OPTIONS);

    HDC GetWindowDC(HWND hWnd);

    int GetWindowRect(HWND handle, int[] rect);

    int GetClientRect(HWND handle, int[] rect);

    int ClientToScreen(HWND hWnd, int[] pt);

    boolean SetWindowPos(HWND hWnd, int hWndInsertAfter, int X, int Y, int cx, int cy, int uFlags);

    HWND WindowFromPoint(int xPoint, int yPoint);

    HWND WindowFromPoint(POINT point);

    HWND ChildWindowFromPoint(HWND hwnd, int[] pt);

//    HWND ChildWindowFromPointEx(HWND hwnd, int[] pt);

    boolean GetCursorPos(int[] p);



}