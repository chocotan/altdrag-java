package io.loli.drag;

import ch.qos.logback.classic.Level;
import com.sun.javaws.Globals;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseInputListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Arrays;
import java.util.logging.LogManager;

public class AltDragApplication {
    private static final Logger altDragLogger = LoggerFactory.getLogger(AltDragApplication.class);

    private static NativeMouseInputListener leftDragListener = new MouseLeftDragListener();
    private static NativeMouseInputListener rightDragListener = new MouseRightDragListener();
    private static NativeKeyListener nativeKeyListener = new AltKeyListener(Arrays.asList(leftDragListener, rightDragListener));

    public static void main(String[] args) throws InterruptedException {

        // Clear previous logging configurations.
        LogManager.getLogManager().reset();

        // Get the logger for "org.jnativehook" and set the level to off.
        Logger logger = LoggerFactory.getLogger(GlobalScreen.class.getPackage().getName());
        ((ch.qos.logback.classic.Logger) logger).setLevel(Level.OFF);
        // Add the appropriate listeners.
        GlobalScreen.addNativeKeyListener(nativeKeyListener);

        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.setEventDispatcher(new VoidDispatchService());
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        altDragLogger.info("Alt-drag started");
        Thread.sleep(Integer.MAX_VALUE);
    }

}
