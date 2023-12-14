package io.github.jspinak.brobot.actions.methods.basicactions.captureAndReplay.capture;

/* This class is modified from a demo on the Web. The comments header from the original file is the following:
 *
 * JNativeHook: Global keyboard and mouse listeners for Java.
 * Copyright (C) 2006-2021 Alexander Barker.  All Rights Reserved.
 * https://github.com/kwhat/jnativehook/
 *
 * JNativeHook is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JNativeHook is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.dispatcher.SwingDispatchService;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A demonstration of how to use the JNativeHook library.
 *
 * @author Alexander Barker (<a href="mailto:alex@1stleg.com">alex@1stleg.com</a>)
 * @version 2.0
 * @since 1.0
 * @see GlobalScreen
 * @see NativeKeyListener
 */
// todo: see class CaptureScenesAndInputs
@Component
public class NativeHookDemo extends JFrame implements ActionListener, ItemListener,
        NativeKeyListener, NativeMouseInputListener, NativeMouseWheelListener, WindowListener {
    private WriteXmlDomActions writeXmlDomActions;
    /**
     * The Constant serialVersionUID.
    **/
    private static final long serialVersionUID = 1541183202160543102L;

    /**
     * Menu Items
     **/
    private JMenu menuSubListeners;
    private JMenuItem menuItemQuit, menuItemClear;
    private JCheckBoxMenuItem menuItemEnable, menuItemKeyboardEvents, menuItemButtonEvents, menuItemMotionEvents, menuItemWheelEvents;

    /**
     * The text area to display event info.
     **/
    private JTextArea txtEventInfo;

    /**
     * Logging
     **/
    private static final Logger log = Logger.getLogger(GlobalScreen.class.getPackage().getName());

    private boolean windowClosed = false;

    public NativeHookDemo(WriteXmlDomActions writeXmlDomActions) {
        this.writeXmlDomActions = writeXmlDomActions;
    }

    /**
     * Instantiates a new native hook demo.
     **/
    public void start() {
        // Setup the main window.
        setTitle("JNativeHook Demo");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(600, 300);
        addWindowListener(this);

        JMenuBar menuBar = new JMenuBar();

        // Create the file menu.
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menuFile);

        menuItemQuit = new JMenuItem("Quit", KeyEvent.VK_Q);
        menuItemQuit.addActionListener(this);
        menuItemQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
        menuItemQuit.getAccessibleContext().setAccessibleDescription("Exit the program");
        menuFile.add(menuItemQuit);

        // Create the view.
        JMenu menuView = new JMenu("View");
        menuView.setMnemonic(KeyEvent.VK_V);
        menuBar.add(menuView);

        menuItemClear = new JMenuItem("Clear", KeyEvent.VK_C);
        menuItemClear.addActionListener(this);
        menuItemClear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
        menuItemClear.getAccessibleContext().setAccessibleDescription("Clear the screen");
        menuView.add(menuItemClear);

        menuView.addSeparator();

        menuItemEnable = new JCheckBoxMenuItem("Enable Native Hook");
        menuItemEnable.addItemListener(this);
        menuItemEnable.setMnemonic(KeyEvent.VK_H);
        menuItemEnable.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
        menuView.add(menuItemEnable);

        // Create the listeners sub menu.
        menuSubListeners = new JMenu("Listeners");
        menuSubListeners.setMnemonic(KeyEvent.VK_L);
        menuView.add(menuSubListeners);

        menuItemKeyboardEvents = new JCheckBoxMenuItem("Keyboard Events");
        menuItemKeyboardEvents.addItemListener(this);
        menuItemKeyboardEvents.setMnemonic(KeyEvent.VK_K);
        menuItemKeyboardEvents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
        menuSubListeners.add(menuItemKeyboardEvents);

        menuItemButtonEvents = new JCheckBoxMenuItem("Button Events");
        menuItemButtonEvents.addItemListener(this);
        menuItemButtonEvents.setMnemonic(KeyEvent.VK_B);
        menuItemButtonEvents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
        menuSubListeners.add(menuItemButtonEvents);

        menuItemMotionEvents = new JCheckBoxMenuItem("Motion Events");
        menuItemMotionEvents.addItemListener(this);
        menuItemMotionEvents.setMnemonic(KeyEvent.VK_M);
        menuItemMotionEvents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
        menuSubListeners.add(menuItemMotionEvents);

        menuItemWheelEvents = new JCheckBoxMenuItem("Wheel Events");
        menuItemWheelEvents.addItemListener(this);
        menuItemWheelEvents.setMnemonic(KeyEvent.VK_W);
        menuItemWheelEvents.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
        menuSubListeners.add(menuItemWheelEvents);

        setJMenuBar(menuBar);

        // Create feedback area.
        txtEventInfo = new JTextArea();
        txtEventInfo.setEditable(false);
        txtEventInfo.setBackground(new Color(0xFF, 0xFF, 0xFF));
        txtEventInfo.setForeground(new Color(0x00, 0x00, 0x00));
        txtEventInfo.setText("");

        JScrollPane scrollPane = new JScrollPane(txtEventInfo);
        scrollPane.setPreferredSize(new Dimension(375, 125));
        add(scrollPane, BorderLayout.CENTER);

        // Disable parent logger and set the desired level.
        log.setUseParentHandlers(false);
        log.setLevel(Level.INFO);

        // Setup a generic ConsoleHandler
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        log.addHandler(handler);

        /* Note: JNativeHook does *NOT* operate on the event dispatching thread.
         * Because Swing components must be accessed on the event dispatching
         * thread, you *MUST* wrap access to Swing components using the
         * SwingUtilities.invokeLater() or EventQueue.invokeLater() methods.
        */
        GlobalScreen.setEventDispatcher(new SwingDispatchService());

        setVisible(true);

        writeXmlDomActions.initDocument();
    }

    /**
     * @see ActionListener#actionPerformed(ActionEvent)
    **/
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == menuItemQuit) {
            this.dispose();
        } else if (e.getSource() == menuItemClear) {
            txtEventInfo.setText("");
        }
    }

    /**
     * @see ItemListener#itemStateChanged(ItemEvent)
     **/
    public void itemStateChanged(ItemEvent e) {
        ItemSelectable item = e.getItemSelectable();

        if (item == menuItemEnable) {
            try {
                // Keyboard checkbox was changed, adjust listeners accordingly.
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    // Initialize native hook.  This is done on window open because the
                    // listener requires the txtEventInfo object to be constructed.
                    GlobalScreen.registerNativeHook();
                } else {
                    GlobalScreen.unregisterNativeHook();
                }
            } catch (NativeHookException ex) {
                appendDisplay("Error: " + ex.getMessage());
            }

            // Set the enable menu item to the state of the hook.
            menuItemEnable.setState(GlobalScreen.isNativeHookRegistered());

            // Set enable/disable the sub-menus based on the enable menu item's state.
            menuSubListeners.setEnabled(menuItemEnable.getState());
        } else if (item == menuItemKeyboardEvents) {
            // Keyboard checkbox was changed, adjust listeners accordingly
            if (e.getStateChange() == ItemEvent.SELECTED) {
                GlobalScreen.addNativeKeyListener(this);
            } else {
                GlobalScreen.removeNativeKeyListener(this);
            }
        } else if (item == menuItemButtonEvents) {
            // Button checkbox was changed, adjust listeners accordingly
            if (e.getStateChange() == ItemEvent.SELECTED) {
                GlobalScreen.addNativeMouseListener(this);
            } else {
                GlobalScreen.removeNativeMouseListener(this);
            }
        } else if (item == menuItemMotionEvents) {
            // Motion checkbox was changed, adjust listeners accordingly
            if (e.getStateChange() == ItemEvent.SELECTED) {
                GlobalScreen.addNativeMouseMotionListener(this);
            } else {
                GlobalScreen.removeNativeMouseMotionListener(this);
            }
        } else if (item == menuItemWheelEvents) {
            // Motion checkbox was changed, adjust listeners accordingly
            if (e.getStateChange() == ItemEvent.SELECTED) {
                GlobalScreen.addNativeMouseWheelListener(this);
            } else {
                GlobalScreen.removeNativeMouseWheelListener(this);
            }
        }
    }

    /**
     * @see NativeKeyListener#nativeKeyPressed(NativeKeyEvent)
     **/
    public void nativeKeyPressed(NativeKeyEvent e) {
        appendDisplay(e.paramString());
        String key = StringUtils.substringAfterLast(e.paramString(),"rawCode=");
        writeXmlDomActions.addElement("input", "KEY_DOWN", "key-pressed", key, e.paramString());
    }

    /**
     * @see NativeKeyListener#nativeKeyReleased(NativeKeyEvent)
     **/
    public void nativeKeyReleased(NativeKeyEvent e) {
        appendDisplay(e.paramString());
        String key = StringUtils.substringAfterLast(e.paramString(),"rawCode=");
        writeXmlDomActions.addElement("input", "KEY_UP", "key-released", key, e.paramString());
    }

    /**
     * @see NativeKeyListener#nativeKeyTyped(NativeKeyEvent)
     **/
    public void nativeKeyTyped(NativeKeyEvent e) {
        appendDisplay(e.paramString());
        String keyPressedAsNumber = StringUtils.substringAfterLast(e.paramString(),"rawCode=");
        writeXmlDomActions.addElement("input", "TYPE", "key-tyoed", keyPressedAsNumber, e.paramString());
    }

    /**
     * @see NativeMouseListener#nativeMouseClicked(NativeMouseEvent)
     **/
    public void nativeMouseClicked(NativeMouseEvent e) {
        appendDisplay(e.paramString());
        writeXmlDomActions.addElement("input", "CLICK", "mouse-clicked", "", e.paramString());
    }

    /**
     * @see NativeMouseListener#nativeMousePressed(NativeMouseEvent)
     **/
    public void nativeMousePressed(NativeMouseEvent e) {
        writeXmlDomActions.addElement("input", "MOUSE_DOWN", "mouse-pressed", "", e.paramString());
        appendDisplay(e.paramString());
    }

    /**
     * @see NativeMouseListener#nativeMouseReleased(NativeMouseEvent)
     **/
    public void nativeMouseReleased(NativeMouseEvent e) {
        writeXmlDomActions.addElement("input", "MOUSE_UP", "mouse-released", "", e.paramString());
        appendDisplay(e.paramString());
    }

    /**
     * @see NativeMouseMotionListener#nativeMouseMoved(NativeMouseEvent)
     **/
    public void nativeMouseMoved(NativeMouseEvent e) {
        appendDisplay(e.paramString());
        writeXmlDomActions.addElement("input", "MOVE", "mouse-moved", "", e.paramString());
    }

    /**
     * @see NativeMouseMotionListener#nativeMouseDragged(NativeMouseEvent)
     **/
    public void nativeMouseDragged(NativeMouseEvent e) {
        appendDisplay(e.paramString());
        writeXmlDomActions.addElement("input", "DRAG", "mouse-dragged", "", e.paramString());
    }

    /**
     * @see NativeMouseWheelListener#nativeMouseWheelMoved(NativeMouseWheelEvent)
     **/
    public void nativeMouseWheelMoved(NativeMouseWheelEvent e) {
        appendDisplay(e.paramString());
    }

    /**
     * Write information about the <code>NativeInputEvent</code> to the text window.
     *
     * @param output appended to textEventInfo
     **/
    private void appendDisplay(final String output) {
        txtEventInfo.append("\n" + output);

        try {
            //Clean up the history to reduce memory consumption.
            if (txtEventInfo.getLineCount() > 100) {
                txtEventInfo.replaceRange("", 0, txtEventInfo.getLineEndOffset(txtEventInfo.getLineCount() - 1 - 100));
            }

            txtEventInfo.setCaretPosition(txtEventInfo.getLineStartOffset(txtEventInfo.getLineCount() - 1));
        } catch (BadLocationException ex) {
            txtEventInfo.setCaretPosition(txtEventInfo.getDocument().getLength());
        }
    }

    /**
     * Unimplemented
     *
     * @see WindowListener#windowActivated(WindowEvent)
     **/
    public void windowActivated(WindowEvent e) { /* Do Nothing */ }

    /**
     * Unimplemented
     *
     * @see WindowListener#windowClosing(WindowEvent)
     **/
    public void windowClosing(WindowEvent e) { /* Do Nothing */ }

    /**
     * Unimplemented
     *
     * @see WindowListener#windowDeactivated(WindowEvent)
    **/
    public void windowDeactivated(WindowEvent e) { /* Do Nothing */ }

    /**
     * Unimplemented
     *
     * @see WindowListener#windowDeiconified(WindowEvent)
    **/
    public void windowDeiconified(WindowEvent e) { /* Do Nothing */ }

    /**
     * Unimplemented
     *
     * @see WindowListener#windowIconified(WindowEvent)
     **/
    public void windowIconified(WindowEvent e) { /* Do Nothing */ }

    /**
     * Display information about the native keyboard and mouse along with any errors that may have
     * occurred.
     *
     * @see WindowListener#windowOpened(WindowEvent)
     **/
    public void windowOpened(WindowEvent e) {
        // Return the focus to the window.
        requestFocusInWindow();

        // Please note that these properties are not available until after the GlobalScreen class is initialized.
        txtEventInfo.setText("Auto Repeat Rate: " + System.getProperty("jnativehook.key.repeat.rate"));
        appendDisplay("Auto Repeat Delay: " + System.getProperty("jnativehook.key.repeat.delay"));
        appendDisplay("Double Click Time: " + System.getProperty("jnativehook.button.multiclick.iterval"));
        appendDisplay("Pointer Sensitivity: " + System.getProperty("jnativehook.pointer.sensitivity"));
        appendDisplay("Pointer Acceleration Multiplier: " + System.getProperty("jnativehook.pointer.acceleration.multiplier"));
        appendDisplay("Pointer Acceleration Threshold: " + System.getProperty("jnativehook.pointer.acceleration.threshold"));

        // Enable the hook, this will cause the GlobalScreen to be initialized.
        menuItemEnable.setSelected(true);

        try {
            txtEventInfo.setCaretPosition(txtEventInfo.getLineStartOffset(txtEventInfo.getLineCount() - 1));
        } catch (BadLocationException ex) {
            txtEventInfo.setCaretPosition(txtEventInfo.getDocument().getLength());
        }

        // Enable all listeners.
        menuItemKeyboardEvents.setSelected(true);
        menuItemButtonEvents.setSelected(true);
        menuItemMotionEvents.setSelected(true);
        menuItemWheelEvents.setSelected(true);
    }

    /**
     * Finalize and exit the program.
     *
     * @see WindowListener#windowClosed(WindowEvent)
     **/
    public void windowClosed(WindowEvent e) {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException ex) {
            ex.printStackTrace();
        }
        windowClosed = true;

    }

    private int getMillis(String str) {
        String secondCut = StringUtils.substringBetween(str, "when=", ",");
        //Report.println("secondcut: " + secondCut);
        return Integer.parseInt(secondCut);
    }

    public boolean isWindowClosed() {
        return windowClosed;
    }
}