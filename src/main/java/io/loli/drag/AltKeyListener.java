package io.loli.drag;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyAdapter;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chocotan
 */
public class AltKeyListener extends NativeKeyAdapter {
    private static ReentrantLock lock = new ReentrantLock();
    private List<NativeMouseInputListener> nativeListeners;
    private static final Logger logger = LoggerFactory.getLogger(AltKeyListener.class);
    private List<NativeMouseInputListener> listeners = new ArrayList<>();

    public AltKeyListener(List<NativeMouseInputListener> listener) {
        this.nativeListeners = listener;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        super.nativeKeyPressed(nativeKeyEvent);
        if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_ALT) {
            listeners.addAll(nativeListeners);
            nativeListeners.forEach(listener -> {
                GlobalScreen.addNativeMouseListener(listener);
                GlobalScreen.addNativeMouseMotionListener(listener);
            });
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        super.nativeKeyReleased(nativeKeyEvent);
        if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_ALT) {
            listeners.parallelStream().forEach(listener -> {
                GlobalScreen.removeNativeMouseListener(listener);
                GlobalScreen.removeNativeMouseMotionListener(listener);
            });
            listeners.clear();
            MouseLeftDragListener.pressed = false;
            MouseRightResizeListener.pressed = false;

        }
    }
}
