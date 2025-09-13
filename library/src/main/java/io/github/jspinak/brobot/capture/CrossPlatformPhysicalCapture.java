package io.github.jspinak.brobot.capture;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.github.jspinak.brobot.config.dpi.PhysicalResolutionCapture;

/**
 * Automatic cross-platform physical resolution capture.
 *
 * <p>Automatically detects and uses the best available tool for physical resolution capture: 1.
 * FFmpeg (preferred - most reliable) 2. ImageMagick 3. Platform-specific tools (screencapture,
 * scrot, etc.) 4. Java Robot with scaling (fallback)
 *
 * @since 1.1.0
 */
@Component
@ConditionalOnProperty(name = "brobot.mock", havingValue = "false", matchIfMissing = true)
public class CrossPlatformPhysicalCapture {

    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static CaptureMethod preferredMethod = null;

    public enum CaptureMethod {
        JAVACV_FFMPEG("JavaCV FFmpeg (bundled)", true), // No external installation needed!
        FFMPEG("FFmpeg (external)", true),
        IMAGEMAGICK("ImageMagick", true),
        SCREENCAPTURE("screencapture (macOS)", false),
        SCROT("scrot (Linux)", false),
        GNOME_SCREENSHOT("gnome-screenshot", false),
        POWERSHELL("PowerShell (Windows)", false),
        JAVA_SCALED("Java Robot (scaled)", true);

        private final String name;
        private final boolean crossPlatform;

        CaptureMethod(String name, boolean crossPlatform) {
            this.name = name;
            this.crossPlatform = crossPlatform;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    static {
        detectBestMethod();
    }

    /** Captures the screen at physical resolution using the best available method. */
    public static BufferedImage capture() throws IOException {
        if (preferredMethod == null) {
            detectBestMethod();
        }

        System.out.println("[PhysicalCapture] Using: " + preferredMethod);

        switch (preferredMethod) {
            case JAVACV_FFMPEG:
                return captureWithJavaCVFFmpeg();
            case FFMPEG:
                return captureWithFFmpeg();
            case IMAGEMAGICK:
                return captureWithImageMagick();
            case SCREENCAPTURE:
                return captureWithScreencapture();
            case SCROT:
                return captureWithScrot();
            case GNOME_SCREENSHOT:
                return captureWithGnomeScreenshot();
            case POWERSHELL:
                return captureWithPowerShell();
            case JAVA_SCALED:
            default:
                return captureWithJavaScaled();
        }
    }

    /** Detects the best available capture method. */
    private static void detectBestMethod() {
        List<CaptureMethod> available = new ArrayList<>();

        // Check JavaCV FFmpeg first (bundled, no external installation needed!)
        if (JavaCVFFmpegCapture.isAvailable()) {
            available.add(CaptureMethod.JAVACV_FFMPEG);
        }

        // Check external FFmpeg
        if (isCommandAvailable("ffmpeg", "-version")) {
            available.add(CaptureMethod.FFMPEG);
        }

        // Check ImageMagick
        String magickCmd = OS.contains("win") ? "magick" : "convert";
        if (isCommandAvailable(magickCmd, "-version")) {
            available.add(CaptureMethod.IMAGEMAGICK);
        }

        // Platform-specific tools
        if (OS.contains("mac")) {
            if (isCommandAvailable("screencapture", "-h")) {
                available.add(CaptureMethod.SCREENCAPTURE);
            }
        } else if (OS.contains("nux")) {
            if (isCommandAvailable("scrot", "--version")) {
                available.add(CaptureMethod.SCROT);
            }
            if (isCommandAvailable("gnome-screenshot", "--version")) {
                available.add(CaptureMethod.GNOME_SCREENSHOT);
            }
        } else if (OS.contains("win")) {
            if (isCommandAvailable("powershell", "-Version")) {
                available.add(CaptureMethod.POWERSHELL);
            }
        }

        // Always available fallback
        available.add(CaptureMethod.JAVA_SCALED);

        // Select preferred (prioritize cross-platform tools)
        preferredMethod =
                available.stream()
                        .filter(m -> m.crossPlatform)
                        .findFirst()
                        .orElse(available.get(0));

        System.out.println("[PhysicalCapture] Available methods: " + available);
        System.out.println("[PhysicalCapture] Selected: " + preferredMethod);
    }

    /** Checks if a command is available on the system. */
    private static boolean isCommandAvailable(String command, String testArg) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command, testArg);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            Process p = pb.start();
            boolean finished = p.waitFor(2, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                return false;
            }
            return p.exitValue() == 0 || p.exitValue() == 1; // Some tools return 1 for help
        } catch (Exception e) {
            return false;
        }
    }

    private static BufferedImage captureWithJavaCVFFmpeg() throws IOException {
        return JavaCVFFmpegCapture.capture();
    }

    private static BufferedImage captureWithFFmpeg() throws IOException {
        return FFmpegPhysicalCapture.capture();
    }

    private static BufferedImage captureWithImageMagick() throws IOException {
        return ImageMagickPhysicalCapture.capture();
    }

    private static BufferedImage captureWithScreencapture() throws IOException {
        String tmpFile = getTempFile("screencapture");
        executeCommand("screencapture", "-x", tmpFile);
        return readAndDelete(tmpFile);
    }

    private static BufferedImage captureWithScrot() throws IOException {
        String tmpFile = getTempFile("scrot");
        executeCommand("scrot", tmpFile);
        return readAndDelete(tmpFile);
    }

    private static BufferedImage captureWithGnomeScreenshot() throws IOException {
        String tmpFile = getTempFile("gnome");
        executeCommand("gnome-screenshot", "-f", tmpFile);
        return readAndDelete(tmpFile);
    }

    private static BufferedImage captureWithPowerShell() throws IOException {
        String tmpFile = getTempFile("powershell");
        String script =
                String.format(
                        "Add-Type -AssemblyName System.Drawing; "
                                + "$bmp = New-Object System.Drawing.Bitmap("
                                + "[System.Windows.Forms.Screen]::PrimaryScreen.Bounds.Width, "
                                + "[System.Windows.Forms.Screen]::PrimaryScreen.Bounds.Height); "
                                + "$graphics = [System.Drawing.Graphics]::FromImage($bmp); "
                                + "$graphics.CopyFromScreen(0, 0, 0, 0, $bmp.Size); "
                                + "$bmp.Save('%s', [System.Drawing.Imaging.ImageFormat]::Png)",
                        tmpFile.replace("\\", "\\\\"));
        executeCommand("powershell", "-Command", script);
        return readAndDelete(tmpFile);
    }

    private static BufferedImage captureWithJavaScaled() throws IOException {
        // Use the existing physical capture implementation
        return PhysicalResolutionCapture.capturePhysical(new org.sikuli.script.Screen());
    }

    private static String getTempFile(String prefix) {
        return System.getProperty("java.io.tmpdir")
                + File.separator
                + "brobot_"
                + prefix
                + "_"
                + System.currentTimeMillis()
                + ".png";
    }

    private static void executeCommand(String... command) throws IOException {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();
            boolean finished = p.waitFor(10, TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly();
                throw new IOException("Command timed out: " + String.join(" ", command));
            }
            if (p.exitValue() != 0) {
                byte[] error = p.getErrorStream().readAllBytes();
                throw new IOException("Command failed: " + new String(error));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Command interrupted", e);
        }
    }

    private static BufferedImage readAndDelete(String file) throws IOException {
        File f = new File(file);
        if (!f.exists()) {
            throw new IOException("Capture file not created: " + file);
        }
        BufferedImage image = ImageIO.read(f);
        f.delete();
        System.out.println(
                "[PhysicalCapture] Captured: " + image.getWidth() + "x" + image.getHeight());
        return image;
    }

    /** Gets information about available capture methods. */
    public static String getInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== Physical Capture Methods ===\n");
        info.append("Current OS: ").append(OS).append("\n");
        info.append("Preferred Method: ").append(preferredMethod).append("\n\n");

        info.append("Available Tools:\n");
        if (JavaCVFFmpegCapture.isAvailable()) {
            info.append("  ✓ JavaCV FFmpeg (bundled - no installation needed!)\n");
        } else {
            info.append("  ✗ JavaCV FFmpeg not available\n");
        }

        if (isCommandAvailable("ffmpeg", "-version")) {
            info.append("  ✓ FFmpeg (external installation)\n");
        } else {
            info.append("  ✗ FFmpeg (external) not found\n");
        }

        String magick = OS.contains("win") ? "magick" : "convert";
        if (isCommandAvailable(magick, "-version")) {
            info.append("  ✓ ImageMagick (cross-platform)\n");
        } else {
            info.append("  ✗ ImageMagick not found\n");
        }

        if (OS.contains("mac") && isCommandAvailable("screencapture", "-h")) {
            info.append("  ✓ screencapture (macOS native)\n");
        }
        if (OS.contains("nux")) {
            if (isCommandAvailable("scrot", "--version")) {
                info.append("  ✓ scrot (Linux)\n");
            }
            if (isCommandAvailable("gnome-screenshot", "--version")) {
                info.append("  ✓ gnome-screenshot (Linux/GNOME)\n");
            }
        }
        if (OS.contains("win") && isCommandAvailable("powershell", "-Version")) {
            info.append("  ✓ PowerShell (Windows)\n");
        }

        info.append("  ✓ Java Robot with scaling (always available)\n");

        return info.toString();
    }

    /** Forces use of a specific capture method. */
    public static void setPreferredMethod(CaptureMethod method) {
        preferredMethod = method;
        System.out.println("[PhysicalCapture] Method set to: " + method);
    }
}
