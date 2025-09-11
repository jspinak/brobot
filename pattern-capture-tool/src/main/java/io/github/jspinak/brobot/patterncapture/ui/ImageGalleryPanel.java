package io.github.jspinak.brobot.patterncapture.ui;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Panel that displays captured pattern images in a grid layout.
 *
 * <p>Features: - Thumbnail view of captured patterns - Click to copy path - Right-click context
 * menu - Auto-refresh when new images are added
 */
public class ImageGalleryPanel extends JPanel {

    private static final int THUMBNAIL_SIZE = 150;
    private static final int PADDING = 10;

    private List<ImageThumbnail> thumbnails = new ArrayList<>();
    private File currentFolder;

    public ImageGalleryPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, PADDING, PADDING));
        setBackground(Color.WHITE);
    }

    /** Load images from specified folder */
    public void loadImagesFromFolder(File folder) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return;
        }

        currentFolder = folder;

        // Clear existing thumbnails
        removeAll();
        thumbnails.clear();

        // Get all PNG files
        File[] imageFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));

        if (imageFiles != null) {
            // Sort by modification date (newest first)
            Arrays.sort(imageFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));

            // Add thumbnails
            for (File imageFile : imageFiles) {
                addImage(imageFile);
            }
        }

        revalidate();
        repaint();
    }

    /** Add a single image to the gallery */
    public void addImage(File imageFile) {
        try {
            ImageThumbnail thumbnail = new ImageThumbnail(imageFile);
            thumbnails.add(0, thumbnail); // Add to beginning
            add(thumbnail, 0); // Add to beginning of panel

            revalidate();
            repaint();
        } catch (IOException e) {
            System.err.println("Failed to load image: " + imageFile.getName());
        }
    }

    /** Refresh the current folder */
    public void refresh() {
        if (currentFolder != null) {
            loadImagesFromFolder(currentFolder);
        }
    }

    /** Individual thumbnail component */
    private class ImageThumbnail extends JPanel {
        private File imageFile;
        private BufferedImage image;
        private BufferedImage thumbnail;
        private JLabel label;

        public ImageThumbnail(File imageFile) throws IOException {
            this.imageFile = imageFile;
            this.image = ImageIO.read(imageFile);
            this.thumbnail = createThumbnail(image);

            setLayout(new BorderLayout());
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            setPreferredSize(new Dimension(THUMBNAIL_SIZE + 20, THUMBNAIL_SIZE + 40));

            // Image label
            JLabel imageLabel = new JLabel(new ImageIcon(thumbnail));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            add(imageLabel, BorderLayout.CENTER);

            // Filename label
            label = new JLabel(imageFile.getName(), JLabel.CENTER);
            label.setFont(new Font("Arial", Font.PLAIN, 10));
            label.setToolTipText(imageFile.getAbsolutePath());
            add(label, BorderLayout.SOUTH);

            // Mouse listeners
            addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getButton() == MouseEvent.BUTTON1) {
                                // Left click - copy path to clipboard
                                copyPathToClipboard();
                            } else if (e.getButton() == MouseEvent.BUTTON3) {
                                // Right click - show context menu
                                showContextMenu(e.getX(), e.getY());
                            }
                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {
                            setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                        }
                    });

            // Double-click to open in default viewer
            addMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 2) {
                                openInViewer();
                            }
                        }
                    });
        }

        private BufferedImage createThumbnail(BufferedImage original) {
            int width = original.getWidth();
            int height = original.getHeight();

            // Calculate scaling to fit in thumbnail size
            double scale =
                    Math.min((double) THUMBNAIL_SIZE / width, (double) THUMBNAIL_SIZE / height);

            int newWidth = (int) (width * scale);
            int newHeight = (int) (height * scale);

            BufferedImage thumbnail =
                    new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

            Graphics2D g = thumbnail.createGraphics();
            g.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, newWidth, newHeight, null);
            g.dispose();

            return thumbnail;
        }

        private void copyPathToClipboard() {
            String path = imageFile.getAbsolutePath();
            StringSelection selection = new StringSelection(path);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);

            // Show feedback
            String originalText = label.getText();
            label.setText("Path copied!");
            Timer timer = new Timer(1500, e -> label.setText(originalText));
            timer.setRepeats(false);
            timer.start();
        }

        private void showContextMenu(int x, int y) {
            JPopupMenu menu = new JPopupMenu();

            JMenuItem copyPathItem = new JMenuItem("Copy Path");
            copyPathItem.addActionListener(e -> copyPathToClipboard());
            menu.add(copyPathItem);

            JMenuItem copyNameItem = new JMenuItem("Copy Filename");
            copyNameItem.addActionListener(
                    e -> {
                        StringSelection selection = new StringSelection(imageFile.getName());
                        Toolkit.getDefaultToolkit()
                                .getSystemClipboard()
                                .setContents(selection, null);
                    });
            menu.add(copyNameItem);

            menu.addSeparator();

            JMenuItem openItem = new JMenuItem("Open in Viewer");
            openItem.addActionListener(e -> openInViewer());
            menu.add(openItem);

            JMenuItem showInFolderItem = new JMenuItem("Show in Folder");
            showInFolderItem.addActionListener(e -> showInFolder());
            menu.add(showInFolderItem);

            menu.addSeparator();

            JMenuItem deleteItem = new JMenuItem("Delete");
            deleteItem.addActionListener(e -> deleteImage());
            menu.add(deleteItem);

            menu.show(this, x, y);
        }

        private void openInViewer() {
            try {
                Desktop.getDesktop().open(imageFile);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        ImageGalleryPanel.this,
                        "Failed to open image: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showInFolder() {
            try {
                Desktop.getDesktop().open(imageFile.getParentFile());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        ImageGalleryPanel.this,
                        "Failed to open folder: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        private void deleteImage() {
            int result =
                    JOptionPane.showConfirmDialog(
                            ImageGalleryPanel.this,
                            "Delete " + imageFile.getName() + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                if (imageFile.delete()) {
                    ImageGalleryPanel.this.remove(this);
                    thumbnails.remove(this);
                    ImageGalleryPanel.this.revalidate();
                    ImageGalleryPanel.this.repaint();
                } else {
                    JOptionPane.showMessageDialog(
                            ImageGalleryPanel.this,
                            "Failed to delete image",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}
