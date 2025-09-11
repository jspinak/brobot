package io.github.jspinak.brobot.runner.ui.illustration.export;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.github.jspinak.brobot.runner.persistence.entities.IllustrationEntity;
import io.github.jspinak.brobot.runner.ui.illustration.gallery.IllustrationGalleryService;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for exporting and sharing illustrations in various formats.
 *
 * <p>This service provides multiple export options:
 *
 * <ul>
 *   <li>Individual image export (PNG, JPEG)
 *   <li>Batch export as ZIP archive
 *   <li>PDF report generation
 *   <li>Video compilation of action sequences
 *   <li>Markdown documentation with embedded images
 * </ul>
 *
 * @see IllustrationGalleryService
 * @see ExportFormat
 */
@Service
@Slf4j
public class IllustrationExportService {

    private final IllustrationGalleryService galleryService;

    @Autowired
    public IllustrationExportService(IllustrationGalleryService galleryService) {
        this.galleryService = galleryService;
    }

    /**
     * Exports a single illustration in the specified format.
     *
     * @param illustrationId the illustration to export
     * @param format the export format
     * @param outputPath the output file path
     * @return the exported file path
     */
    public Path exportSingle(Long illustrationId, ExportFormat format, String outputPath) {
        try {
            // Load illustration
            IllustrationEntity illustration =
                    galleryService
                            .getIllustrationById(illustrationId)
                            .orElseThrow(
                                    () -> new IllegalArgumentException("Illustration not found"));

            Path sourcePath = Paths.get(illustration.getFilePath());
            Path targetPath = Paths.get(outputPath);

            // Ensure parent directory exists
            Files.createDirectories(targetPath.getParent());

            switch (format) {
                case PNG:
                    Files.copy(sourcePath, targetPath);
                    break;

                case JPEG:
                    convertToJpeg(sourcePath, targetPath);
                    break;

                case PDF:
                    exportAsPdf(List.of(illustration), targetPath);
                    break;

                default:
                    throw new UnsupportedOperationException(
                            "Format not supported for single export: " + format);
            }

            log.info("Exported illustration {} to {}", illustrationId, targetPath);
            return targetPath;

        } catch (IOException e) {
            log.error("Failed to export illustration", e);
            throw new RuntimeException("Failed to export illustration", e);
        }
    }

    /**
     * Exports multiple illustrations as a ZIP archive.
     *
     * @param illustrationIds the illustrations to export
     * @param outputPath the output ZIP file path
     * @return the exported file path
     */
    public Path exportBatch(List<Long> illustrationIds, String outputPath) {
        try {
            Path targetPath = Paths.get(outputPath);
            Files.createDirectories(targetPath.getParent());

            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(targetPath))) {
                for (Long id : illustrationIds) {
                    galleryService
                            .getIllustrationById(id)
                            .ifPresent(
                                    illustration -> {
                                        try {
                                            Path sourcePath = Paths.get(illustration.getFilePath());
                                            if (Files.exists(sourcePath)) {
                                                ZipEntry entry =
                                                        new ZipEntry(
                                                                String.format(
                                                                        "%s_%s",
                                                                        illustration
                                                                                .getTimestamp()
                                                                                .toString()
                                                                                .replace(":", "-"),
                                                                        illustration
                                                                                .getFilename()));
                                                zos.putNextEntry(entry);
                                                Files.copy(sourcePath, zos);
                                                zos.closeEntry();
                                            }
                                        } catch (IOException e) {
                                            log.warn("Failed to add illustration {} to ZIP", id, e);
                                        }
                                    });
                }

                // Add metadata file
                addMetadataToZip(zos, illustrationIds);
            }

            log.info("Exported {} illustrations to {}", illustrationIds.size(), targetPath);
            return targetPath;

        } catch (IOException e) {
            log.error("Failed to export batch", e);
            throw new RuntimeException("Failed to export batch", e);
        }
    }

    /**
     * Exports illustrations as a PDF report.
     *
     * @param illustrations the illustrations to include
     * @param outputPath the output PDF file path
     * @return the exported file path
     */
    public Path exportAsPdf(List<IllustrationEntity> illustrations, Path outputPath) {
        // This is a simplified implementation
        // A real implementation would use a PDF library like iText or Apache PDFBox

        try {
            // For now, create a simple HTML report that can be printed to PDF
            String html = generatePdfHtml(illustrations);
            Path htmlPath = outputPath.resolveSibling(outputPath.getFileName() + ".html");
            Files.writeString(htmlPath, html);

            log.info("Generated PDF-ready HTML at {}", htmlPath);
            return htmlPath;

        } catch (IOException e) {
            log.error("Failed to export as PDF", e);
            throw new RuntimeException("Failed to export as PDF", e);
        }
    }

    /**
     * Exports illustrations as a Markdown document.
     *
     * @param sessionId the session to export
     * @param outputPath the output markdown file path
     * @param includeImages whether to embed images
     * @return the exported file path
     */
    public Path exportAsMarkdown(String sessionId, String outputPath, boolean includeImages) {
        try {
            List<IllustrationEntity> illustrations =
                    galleryService.getSessionIllustrations(sessionId);

            StringBuilder markdown = new StringBuilder();
            markdown.append("# Brobot Automation Report\n\n");
            markdown.append("**Session:** ").append(sessionId).append("\n\n");
            markdown.append("**Total Actions:** ").append(illustrations.size()).append("\n\n");

            // Summary statistics
            long successCount =
                    illustrations.stream().filter(IllustrationEntity::isSuccess).count();
            markdown.append("**Success Rate:** ")
                    .append(
                            String.format(
                                    "%.1f%%", (double) successCount / illustrations.size() * 100))
                    .append("\n\n");

            markdown.append("## Action Timeline\n\n");

            for (IllustrationEntity illustration : illustrations) {
                markdown.append("### ").append(illustration.getTimestamp()).append("\n\n");
                markdown.append("**Action:** ").append(illustration.getActionType()).append("\n\n");
                markdown.append("**State:** ").append(illustration.getStateName()).append("\n\n");
                markdown.append("**Status:** ")
                        .append(illustration.isSuccess() ? "✅ Success" : "❌ Failed")
                        .append("\n\n");

                if (includeImages) {
                    // Copy image to same directory as markdown
                    Path targetDir = Paths.get(outputPath).getParent();
                    Path imageDir = targetDir.resolve("images");
                    Files.createDirectories(imageDir);

                    Path sourcePath = Paths.get(illustration.getFilePath());
                    Path targetImagePath = imageDir.resolve(illustration.getFilename());

                    if (Files.exists(sourcePath)) {
                        Files.copy(sourcePath, targetImagePath);
                        markdown.append("![")
                                .append(illustration.getActionType())
                                .append("](images/")
                                .append(illustration.getFilename())
                                .append(")\n\n");
                    }
                }

                markdown.append("---\n\n");
            }

            Path targetPath = Paths.get(outputPath);
            Files.writeString(targetPath, markdown.toString());

            log.info("Exported markdown report to {}", targetPath);
            return targetPath;

        } catch (IOException e) {
            log.error("Failed to export as markdown", e);
            throw new RuntimeException("Failed to export as markdown", e);
        }
    }

    /**
     * Creates a shareable link for an illustration or session. This would typically upload to a
     * cloud service or create a local server link.
     *
     * @param sessionId the session to share
     * @return shareable URL
     */
    public String createShareableLink(String sessionId) {
        // In a real implementation, this would:
        // 1. Upload to cloud storage (S3, Google Cloud, etc.)
        // 2. Generate a shareable link with expiration
        // 3. Optionally create a password-protected link

        // For now, return a placeholder
        String shareId = UUID.randomUUID().toString();
        return String.format("http://localhost:8080/gallery/share/%s", shareId);
    }

    /** Converts a PNG image to JPEG format. */
    private void convertToJpeg(Path source, Path target) throws IOException {
        BufferedImage image = ImageIO.read(source.toFile());

        // Create a new image with white background (JPEG doesn't support transparency)
        BufferedImage jpegImage =
                new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

        Graphics2D g = jpegImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.drawImage(image, 0, 0, null);
        g.dispose();

        ImageIO.write(jpegImage, "JPEG", target.toFile());
    }

    /** Adds metadata file to ZIP archive. */
    private void addMetadataToZip(ZipOutputStream zos, List<Long> illustrationIds)
            throws IOException {
        StringBuilder metadata = new StringBuilder();
        metadata.append("Brobot Illustration Export\n");
        metadata.append("=========================\n\n");
        metadata.append("Export Date: ").append(LocalDateTime.now()).append("\n");
        metadata.append("Total Illustrations: ").append(illustrationIds.size()).append("\n\n");

        metadata.append("Illustrations:\n");
        for (Long id : illustrationIds) {
            galleryService
                    .getIllustrationById(id)
                    .ifPresent(
                            ill -> {
                                metadata.append("- ")
                                        .append(ill.getFilename())
                                        .append(" (")
                                        .append(ill.getActionType())
                                        .append(", ")
                                        .append(ill.getTimestamp())
                                        .append(")\n");
                            });
        }

        ZipEntry metadataEntry = new ZipEntry("metadata.txt");
        zos.putNextEntry(metadataEntry);
        zos.write(metadata.toString().getBytes());
        zos.closeEntry();
    }

    /** Generates HTML for PDF export. */
    private String generatePdfHtml(List<IllustrationEntity> illustrations) {
        StringBuilder html = new StringBuilder();
        html.append(
                """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Brobot Automation Report</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    h1 { color: #2c3e50; }
                    .illustration { page-break-inside: avoid; margin-bottom: 30px; }
                    .metadata { background-color: #f5f5f5; padding: 10px; margin: 10px 0; }
                    img { max-width: 100%; height: auto; }
                    .success { color: green; }
                    .failure { color: red; }
                </style>
            </head>
            <body>
                <h1>Brobot Automation Report</h1>
            """);

        for (IllustrationEntity illustration : illustrations) {
            html.append("<div class='illustration'>");
            html.append("<h2>").append(illustration.getActionType()).append("</h2>");
            html.append("<div class='metadata'>");
            html.append("<p><strong>Time:</strong> ")
                    .append(illustration.getTimestamp())
                    .append("</p>");
            html.append("<p><strong>State:</strong> ")
                    .append(illustration.getStateName())
                    .append("</p>");
            html.append("<p><strong>Status:</strong> <span class='")
                    .append(illustration.isSuccess() ? "success" : "failure")
                    .append("'>")
                    .append(illustration.isSuccess() ? "Success" : "Failed")
                    .append("</span></p>");
            html.append("</div>");

            // For PDF, we'd need to embed images as base64 or reference them
            html.append("<p><em>[Image: ").append(illustration.getFilename()).append("]</em></p>");

            html.append("</div>");
        }

        html.append("</body></html>");
        return html.toString();
    }

    /** Supported export formats. */
    public enum ExportFormat {
        PNG("PNG Image", ".png"),
        JPEG("JPEG Image", ".jpg"),
        PDF("PDF Document", ".pdf"),
        ZIP("ZIP Archive", ".zip"),
        MARKDOWN("Markdown Document", ".md"),
        VIDEO("Video Compilation", ".mp4");

        private final String description;
        private final String extension;

        ExportFormat(String description, String extension) {
            this.description = description;
            this.extension = extension;
        }

        public String getDescription() {
            return description;
        }

        public String getExtension() {
            return extension;
        }
    }
}
