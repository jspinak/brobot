package io.github.jspinak.brobot.actions.methods.basicactions.capture;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.*;
import org.springframework.stereotype.Component;

@Component
public class CaptureInputs implements //ActionListener, ItemListener,
        NativeKeyListener, NativeMouseInputListener, NativeMouseWheelListener {
    private WriteXmlDomActions writeXmlDomActions; //, WindowListener { {

    public CaptureInputs(WriteXmlDomActions writeXmlDomActions) {
        this.writeXmlDomActions = writeXmlDomActions;
    }

    public void addListeners() {
        GlobalScreen.addNativeKeyListener(this);
        GlobalScreen.addNativeMouseListener(this);
        GlobalScreen.addNativeMouseMotionListener(this);
        GlobalScreen.addNativeMouseWheelListener(this);
    }

    public void removeListeners() {
        GlobalScreen.removeNativeKeyListener(this);
        GlobalScreen.removeNativeMouseListener(this);
        GlobalScreen.removeNativeMouseMotionListener(this);
        GlobalScreen.removeNativeMouseWheelListener(this);
    }

    /**
     * Write information about the <code>NativeInputEvent</code> to file.
     * XML format is used since it is a common format for storing data to be used for machine learning.
     *
     * @param category the id of the child element attribute
     * @param output the value of the child element attribute
     */
    private void appendToFile(String category, final String output) {
        writeXmlDomActions.addElement("input", "test", "", category, output);
    }

    /**
     * @see NativeKeyListener#nativeKeyPressed(NativeKeyEvent)
     */
    public void nativeKeyPressed(NativeKeyEvent e) {
        appendToFile("key-pressed", e.paramString());
    }

    /**
     * @see NativeKeyListener#nativeKeyReleased(NativeKeyEvent)
     */
    public void nativeKeyReleased(NativeKeyEvent e) {
        appendToFile("key-released", e.paramString());
    }

    /**
     * @see NativeKeyListener#nativeKeyTyped(NativeKeyEvent)
     */
    public void nativeKeyTyped(NativeKeyEvent e) {
        appendToFile("key-typed", e.paramString());
    }

    /**
     * @see NativeMouseListener#nativeMouseClicked(NativeMouseEvent)
     */
    public void nativeMouseClicked(NativeMouseEvent e) {
        appendToFile("mouse-clicked", e.paramString());
    }

    /**
     * @see NativeMouseListener#nativeMousePressed(NativeMouseEvent)
     */
    public void nativeMousePressed(NativeMouseEvent e) {
        appendToFile("mouse-pressed", e.paramString());
    }

    /**
     * @see NativeMouseListener#nativeMouseReleased(NativeMouseEvent)
     */
    public void nativeMouseReleased(NativeMouseEvent e) {
        appendToFile("mouse-released", e.paramString());
    }

    /**
     * @see NativeMouseMotionListener#nativeMouseMoved(NativeMouseEvent)
     */
    public void nativeMouseMoved(NativeMouseEvent e) {
        appendToFile("mouse-moved", e.paramString());
    }

    /**
     * @see NativeMouseMotionListener#nativeMouseDragged(NativeMouseEvent)
     */
    public void nativeMouseDragged(NativeMouseEvent e) {
        appendToFile("mouse-dragged", e.paramString());
    }

    /**
     * @see NativeMouseWheelListener#nativeMouseWheelMoved(NativeMouseWheelEvent)
     */
    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
        appendToFile("mouse-wheel", e.paramString());
    }

}
