package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.recorder;

import org.apache.commons.lang3.StringUtils;
import com.github.kwhat.jnativehook.*;
import com.github.kwhat.jnativehook.keyboard.*;
import com.github.kwhat.jnativehook.mouse.*;
//import org.jnativehook.GlobalScreen;
//import org.jnativehook.NativeHookException;
//import org.jnativehook.keyboard.NativeKeyEvent;
//import org.jnativehook.keyboard.NativeKeyListener;
//import org.jnativehook.mouse.*;
import org.sikuli.basics.Debug;
import org.sikuli.basics.Settings;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class CaptureUserInputs implements NativeKeyListener, NativeMouseListener, NativeMouseMotionListener, NativeMouseWheelListener {

    private RecordInputs recordInputs;

    public CaptureUserInputs(RecordInputs recordInputs) {
        this.recordInputs = recordInputs;
    }

    /**
     * Setting the level to OFF disables console logs.
     */
    private void registerNativeHook() {
        try {
            Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
            logger.setUseParentHandlers(false);
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            Debug.error("Error registering native hook: %s", e.getMessage());
        }
    }

    /**
     * The NativeHook in Windows blocks some special keys when registered.
     * Unregistering it on Linux terminates the JVM.
     * Leaving the hook registered on Mac and Linux is not a problem.
     */
    public void unregisterNativeHook() {
        if (Settings.isWindows()) {
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException e) {
                Debug.error("Error unregistering native hook: %s", e.getMessage());
            }
        }
    }

    public void startRecording() {
        recordInputs.initDocument();
        registerNativeHook();
        GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseListener(this);
        GlobalScreen.addNativeMouseMotionListener(this);
        GlobalScreen.addNativeMouseWheelListener(this);
    }

    public Document finalizeRecording() {
        GlobalScreen.removeNativeKeyListener(this);
        GlobalScreen.removeNativeMouseListener(this);
        GlobalScreen.removeNativeMouseMotionListener(this);
        GlobalScreen.removeNativeMouseWheelListener(this);
        unregisterNativeHook();
        return recordInputs.getDoc();
    }

    /**
     * @see NativeKeyListener#nativeKeyPressed(NativeKeyEvent)
     */
    public void nativeKeyPressed(NativeKeyEvent e) {
        String key = StringUtils.substringAfterLast(e.paramString(),"rawCode=");
        recordInputs.addElement("KEY_DOWN", key, e.paramString());
    }

    /**
     * @see NativeKeyListener#nativeKeyReleased(NativeKeyEvent)
     */
    public void nativeKeyReleased(NativeKeyEvent e) {
        String key = StringUtils.substringAfterLast(e.paramString(),"rawCode=");
        recordInputs.addElement("KEY_UP", key, e.paramString());
    }

    /**
     * @see NativeKeyListener#nativeKeyTyped(NativeKeyEvent)
     */
    public void nativeKeyTyped(NativeKeyEvent e) {
        String keyPressedAsNumber = StringUtils.substringAfterLast(e.paramString(),"rawCode=");
        recordInputs.addElement("TYPE", keyPressedAsNumber, e.paramString());
    }

    /**
     * @see NativeMouseListener#nativeMouseClicked(NativeMouseEvent)
     */
    public void nativeMouseClicked(NativeMouseEvent e) {
        recordInputs.addElement("CLICK","", e.paramString());
    }

    /**
     * @see NativeMouseListener#nativeMousePressed(NativeMouseEvent)
     */
    public void nativeMousePressed(NativeMouseEvent e) {
        recordInputs.addElement("MOUSE_DOWN", "", e.paramString());
    }

    /**
     * @see NativeMouseListener#nativeMouseReleased(NativeMouseEvent)
     */
    public void nativeMouseReleased(NativeMouseEvent e) {
        recordInputs.addElement("MOUSE_UP", "", e.paramString());
    }

    /**
     * @see NativeMouseMotionListener#nativeMouseMoved(NativeMouseEvent)
     */
    public void nativeMouseMoved(NativeMouseEvent e) {
        recordInputs.addElement("MOVE", "", e.paramString());
    }

    /**
     * @see NativeMouseMotionListener#nativeMouseDragged(NativeMouseEvent)
     */
    public void nativeMouseDragged(NativeMouseEvent e) {
        recordInputs.addElement("DRAG","", e.paramString());
    }

    /**
     * @see NativeMouseWheelListener#nativeMouseWheelMoved(NativeMouseWheelEvent)
     */
    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
        recordInputs.addElement("WHEEL", "", e.paramString());
    }


}
