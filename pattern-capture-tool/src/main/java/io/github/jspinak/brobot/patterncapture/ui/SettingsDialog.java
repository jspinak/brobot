package io.github.jspinak.brobot.patterncapture.ui;

import io.github.jspinak.brobot.capture.CaptureConfiguration;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.Map;

/**
 * Settings dialog for configuring capture options.
 */
public class SettingsDialog extends JDialog {
    
    private CaptureConfiguration captureConfig;
    private JTextArea propertiesArea;
    
    public SettingsDialog(Frame parent, CaptureConfiguration config) {
        super(parent, "Capture Settings", true);
        this.captureConfig = config;
        initializeUI();
        loadSettings();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setSize(600, 500);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Provider selection panel
        JPanel providerPanel = new JPanel(new GridBagLayout());
        providerPanel.setBorder(new TitledBorder("Capture Provider"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        ButtonGroup providerGroup = new ButtonGroup();
        
        JRadioButton sikulixRadio = new JRadioButton("SikuliX (Default - Maximum Compatibility)");
        JRadioButton robotRadio = new JRadioButton("Robot (Java Built-in with DPI Scaling)");
        JRadioButton ffmpegRadio = new JRadioButton("JavaCV FFmpeg (Physical Resolution - Bundled)");
        JRadioButton autoRadio = new JRadioButton("Auto (System Selects Best Available)");
        
        providerGroup.add(sikulixRadio);
        providerGroup.add(robotRadio);
        providerGroup.add(ffmpegRadio);
        providerGroup.add(autoRadio);
        
        // Select current provider
        String currentProvider = captureConfig.getCurrentProvider();
        switch (currentProvider.toUpperCase()) {
            case "SIKULIX":
                sikulixRadio.setSelected(true);
                break;
            case "ROBOT":
                robotRadio.setSelected(true);
                break;
            case "FFMPEG":
            case "JAVACV_FFMPEG":
                ffmpegRadio.setSelected(true);
                break;
            default:
                autoRadio.setSelected(true);
        }
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        providerPanel.add(sikulixRadio, gbc);
        gbc.gridy++;
        providerPanel.add(robotRadio, gbc);
        gbc.gridy++;
        providerPanel.add(ffmpegRadio, gbc);
        gbc.gridy++;
        providerPanel.add(autoRadio, gbc);
        
        mainPanel.add(providerPanel, BorderLayout.NORTH);
        
        // Configuration details panel
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(new TitledBorder("Current Configuration"));
        
        propertiesArea = new JTextArea();
        propertiesArea.setEditable(false);
        propertiesArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(propertiesArea);
        detailsPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(detailsPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> {
            // Apply selected provider
            if (sikulixRadio.isSelected()) {
                captureConfig.useSikuliX();
            } else if (robotRadio.isSelected()) {
                captureConfig.useRobot();
            } else if (ffmpegRadio.isSelected()) {
                try {
                    captureConfig.useFFmpeg();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "FFmpeg not available: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                captureConfig.useAuto();
            }
            loadSettings(); // Refresh display
        });
        
        JButton validateButton = new JButton("Validate");
        validateButton.addActionListener(e -> {
            boolean valid = captureConfig.validateConfiguration();
            if (valid) {
                JOptionPane.showMessageDialog(this,
                    "Configuration is valid and working!",
                    "Validation Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Configuration validation failed. Check settings.",
                    "Validation Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(applyButton);
        buttonPanel.add(validateButton);
        buttonPanel.add(closeButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadSettings() {
        StringBuilder sb = new StringBuilder();
        
        // Current provider info
        sb.append("Active Provider: ").append(captureConfig.getCurrentProvider()).append("\n");
        sb.append("Resolution Type: ").append(
            captureConfig.isCapturingPhysicalResolution() ? "PHYSICAL" : "LOGICAL").append("\n");
        sb.append("\n");
        
        // All properties
        sb.append("Capture Properties:\n");
        sb.append("==================\n");
        
        Map<String, String> properties = captureConfig.getAllCaptureProperties();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            sb.append(String.format("%-45s = %s\n", entry.getKey(), entry.getValue()));
        }
        
        propertiesArea.setText(sb.toString());
        propertiesArea.setCaretPosition(0);
    }
}