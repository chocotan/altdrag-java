package io.loli.drag;

//import com.sun.jna.platform.win32.Kernel32;
//import com.sun.jna.platform.win32.User32;
//import com.sun.jna.platform.win32.WinDef;
//import com.sun.jna.platform.win32.WinDef.HMODULE;
//import com.sun.jna.platform.win32.WinUser;
//
//import static com.sun.jna.platform.win32.Kernel32.*;
//
//class KeyHook {
//        private static WinUser.HHOOK hhk;
//        private static WinUser.LowLevelKeyboardProc keyboardHook;
//        private static User32 lib;
//
//        public static void blockWindowsKey() {
//            if (isWindows()) new Thread(new Runnable() {
//
//                @Override
//                public void run() {
//                    lib = User32.INSTANCE;
//                    HMODULE hMod = INSTANCE.GetModuleHandle(null);
//                    keyboardHook = new WinUser.LowLevelKeyboardProc() {
//                        public WinDef.LRESULT callback(int nCode, WinDef.WPARAM wParam, WinUser.KBDLLHOOKSTRUCT info) {
//                            if (nCode >= 0) {
//                                switch (info.vkCode) {
//                                    case 0x5B:
//                                    case 0x5C:
//                                        return new LRESULT(1);
//                                    default: //do nothing
//                                }
//                            }
//                            return lib.CallNextHookEx(hhk, nCode, wParam, info.getPointer());
//                        }
//                    };
//                    hhk = lib.SetWindowsHookEx(13, keyboardHook, hMod, 0);
//
//                    // This bit never returns from GetMessage
//                    int result;
//                    WinUser.MSG msg = new WinUser.MSG();
//                    while ((result = lib.GetMessage(msg, null, 0, 0)) != 0) {
//                        if (result == -1) {
//                            break;
//                        } else {
//                            lib.TranslateMessage(msg);
//                            lib.DispatchMessage(msg);
//                        }
//                    }
//                    lib.UnhookWindowsHookEx(hhk);
//                }
//            }).start();
//        }
//
//        public static void unblockWindowsKey() {
//            if (isWindows() && lib != null) {
//                lib.UnhookWindowsHookEx(hhk);
//            }
//        }
//
//        public static boolean isWindows(){
//            String os = System.getProperty("os.name").toLowerCase();
//            return (os.indexOf( "win" ) >= 0);
//        }
//    }
