package io.github.jspinak.brobot.patterncapture.ui;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.capture.CaptureConfiguration;
import io.github.jspinak.brobot.patterncapture.capture.CaptureController;

/** Main application window for the Brobot Pattern Capture Tool. */
@Component
public class MainWindow extends JFrame {

    @Value("${pattern.capture.default-folder:./patterns}")
    private String defaultFolder;

    @Value("${pattern.capture.capture-delay:500}")
    private int captureDelay;

    @Value("${pattern.capture.ui.window-width:900}")
    private int windowWidth;

    @Value("${pattern.capture.ui.window-height:600}")
    private int windowHeight;

    @Autowired private CaptureController captureController;

    @Autowired private CaptureConfiguration captureConfig;

    private File currentFolder;
    private ImageGalleryPanel galleryPanel;
    private JLabel statusLabel;
    private JLabel providerLabel;

    public void initialize() {
        setupWindow();
        setupUI();
        setupFolder();
        setVisible(true);
    }

    private void setupWindow() {
        setTitle("Brobot Pattern Capture Tool");
        setSize(windowWidth, windowHeight);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Application icon
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Icon not found, use default
        }

        // Window close handler
        addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        // Clean up resources if needed
                        System.exit(0);
                    }
                });
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Toolbar
        add(createToolbar(), BorderLayout.NORTH);

        // Main content area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Gallery panel
        galleryPanel = new ImageGalleryPanel();
        JScrollPane scrollPane = new JScrollPane(galleryPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // Status bar
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JToolBar createToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        // Capture button
        JButton captureBtn = new JButton("Capture");
        captureBtn.setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
        captureBtn.setToolTipText("Capture screen region (or press F1)");
        captureBtn.addActionListener(e -> startCapture());
        toolbar.add(captureBtn);

        toolbar.addSeparator();

        // Delay capture button
        JButton delayCaptureBtn = new JButton("Delayed Capture");
        delayCaptureBtn.setToolTipText("Capture with " + captureDelay + "ms delay");
        delayCaptureBtn.addActionListener(e -> startDelayedCapture());
        toolbar.add(delayCaptureBtn);

        toolbar.addSeparator();

        // Folder selection
        JButton folderBtn = new JButton("Select Folder");
        folderBtn.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        folderBtn.setToolTipText("Choose output folder");
        folderBtn.addActionListener(e -> selectFolder());
        toolbar.add(folderBtn);

        toolbar.addSeparator();

        // Refresh button
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setToolTipText("Refresh image gallery");
        refreshBtn.addActionListener(e -> refreshGallery());
        toolbar.add(refreshBtn);

        toolbar.addSeparator();

        // Provider selector
        JLabel providerLbl = new JLabel("Provider:");
        toolbar.add(providerLbl);

        String[] providers = {"Auto", "SikuliX", "Robot", "FFmpeg"};
        JComboBox<String> providerCombo = new JComboBox<>(providers);
        providerCombo.setSelectedItem(captureConfig.getCurrentProvider());
        providerCombo.addActionListener(
                e -> {
                    String selected = (String) providerCombo.getSelectedItem();
                    switchProvider(selected);
                });
        providerCombo.setMaximumSize(new Dimension(120, 30));
        toolbar.add(providerCombo);

        toolbar.add(Box.createHorizontalGlue());

        // Settings button
        JButton settingsBtn = new JButton("Settings");
        settingsBtn.setIcon(UIManager.getIcon("FileView.computerIcon"));
        settingsBtn.addActionListener(e -> showSettings());
        toolbar.add(settingsBtn);

        return toolbar;
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());

        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(new EmptyBorder(2, 5, 2, 5));
        statusBar.add(statusLabel, BorderLayout.WEST);

        providerLabel = new JLabel("Provider: " + captureConfig.getCurrentProvider());
        providerLabel.setBorder(new EmptyBorder(2, 5, 2, 5));
        statusBar.add(providerLabel, BorderLayout.EAST);

        return statusBar;
    }

    private void setupFolder() {
        currentFolder = new File(defaultFolder);
        if (!currentFolder.exists()) {
            currentFolder.mkdirs();
        }
        galleryPanel.loadImagesFromFolder(currentFolder);
        updateStatus("Folder: " + currentFolder.getAbsolutePath());
    }

    private void startCapture() {
        updateStatus("Starting capture...");
        captureController.startCapture(
                image -> {
                    if (image != null) {
                        saveImage(image);
                    } else {
                        updateStatus("Capture cancelled");
                    }
                });
    }

    private void startDelayedCapture() {
        updateStatus("Starting capture in " + captureDelay + "ms...");
        captureController.startCapture(
                image -> {
                    if (image != null) {
                        saveImage(image);
                    } else {
                        updateStatus("Capture cancelled");
                    }
                },
                captureDelay);
    }

    private void saveImage(BufferedImage image) {
        try {
            // Generate filename with timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timestamp = sdf.format(new Date());
            String filename = "pattern_" + timestamp + ".png";

            File outputFile = new File(currentFolder, filename);
            ImageIO.write(image, "PNG", outputFile);

            // Add to gallery
            SwingUtilities.invokeLater(
                    () -> {
                        galleryPanel.addImage(outputFile);
                        updateStatus("Saved: " + filename);
                    });

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to save image: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
            updateStatus("Failed to save image");
        }
    }

    private void selectFolder() {
        JFileChooser chooser = new JFileChooser(currentFolder);
        chooser.setDialogTitle("Select Output Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFolder = chooser.getSelectedFile();
            galleryPanel.loadImagesFromFolder(currentFolder);
            updateStatus("Folder: " + currentFolder.getAbsolutePath());
        }
    }

    private void refreshGallery() {
        galleryPanel.refresh();
        updateStatus("Gallery refreshed");
    }

    private void switchProvider(String provider) {
        try {
            switch (provider) {
                case "SikuliX":
                    captureConfig.useSikuliX();
                    break;
                case "Robot":
                    captureConfig.useRobot();
                    break;
                case "FFmpeg":
                    captureConfig.useFFmpeg();
                    break;
                default:
                    captureConfig.useAuto();
            }

            providerLabel.setText("Provider: " + captureConfig.getCurrentProvider());
            updateStatus("Switched to " + captureConfig.getCurrentProvider());

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to switch provider: " + e.getMessage(),
                    "Provider Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSettings() {
        SettingsDialog dialog = new SettingsDialog(this, captureConfig);
        dialog.setVisible(true);
        // Update provider label after settings
        providerLabel.setText("Provider: " + captureConfig.getCurrentProvider());
    }

    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }

    /** Set up global hotkeys for capture */
    public void setupHotkeys() {
        // F1 for capture
        KeyStroke captureKey = KeyStroke.getKeyStroke("F1");
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(captureKey, "capture");
        getRootPane()
                .getActionMap()
                .put(
                        "capture",
                        new AbstractAction() {
                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent e) {
                                startCapture();
                            }
                        });

        // F2 for delayed capture
        KeyStroke delayCaptureKey = KeyStroke.getKeyStroke("F2");
        getRootPane()
                .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(delayCaptureKey, "delayedCapture");
        getRootPane()
                .getActionMap()
                .put(
                        "delayedCapture",
                        new AbstractAction() {
                            @Override
                            public void actionPerformed(java.awt.event.ActionEvent e) {
                                startDelayedCapture();
                            }
                        });
    }
}
