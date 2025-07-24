package io.github.jspinak.brobot.runner.ui.illustration.gallery;

import io.github.jspinak.brobot.runner.persistence.entities.IllustrationEntity;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Exports illustration galleries as static HTML websites.
 * <p>
 * This exporter creates a self-contained website with:
 * <ul>
 * <li>Responsive grid layout for illustrations</li>
 * <li>Filtering by action type and success status</li>
 * <li>Lightbox for full-size viewing</li>
 * <li>Timeline view of actions</li>
 * </ul>
 *
 * @see IllustrationGalleryService
 */
@Slf4j
public class GalleryWebExporter {
    
    private static final String HTML_TEMPLATE = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Brobot Illustration Gallery - %s</title>
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                
                body {
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                    background-color: #f5f5f5;
                    color: #333;
                }
                
                header {
                    background-color: #2c3e50;
                    color: white;
                    padding: 1rem 2rem;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                }
                
                h1 { font-size: 1.8rem; margin-bottom: 0.5rem; }
                
                .subtitle {
                    color: #ecf0f1;
                    font-size: 0.9rem;
                }
                
                .filters {
                    background-color: white;
                    padding: 1rem 2rem;
                    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
                    display: flex;
                    gap: 1rem;
                    flex-wrap: wrap;
                    align-items: center;
                }
                
                .filter-group {
                    display: flex;
                    gap: 0.5rem;
                    align-items: center;
                }
                
                select, button {
                    padding: 0.5rem 1rem;
                    border: 1px solid #ddd;
                    border-radius: 4px;
                    background-color: white;
                    cursor: pointer;
                    font-size: 0.9rem;
                }
                
                button:hover { background-color: #f8f8f8; }
                
                .stats {
                    margin-left: auto;
                    font-size: 0.9rem;
                    color: #666;
                }
                
                .gallery {
                    padding: 2rem;
                    display: grid;
                    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
                    gap: 1.5rem;
                }
                
                .illustration-card {
                    background-color: white;
                    border-radius: 8px;
                    overflow: hidden;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                    transition: transform 0.2s, box-shadow 0.2s;
                }
                
                .illustration-card:hover {
                    transform: translateY(-2px);
                    box-shadow: 0 4px 16px rgba(0,0,0,0.15);
                }
                
                .illustration-image {
                    width: 100%;
                    height: 200px;
                    object-fit: cover;
                    cursor: pointer;
                    background-color: #f0f0f0;
                }
                
                .illustration-info {
                    padding: 1rem;
                }
                
                .action-type {
                    font-weight: 600;
                    color: #2c3e50;
                    margin-bottom: 0.5rem;
                }
                
                .state-name {
                    color: #7f8c8d;
                    font-size: 0.9rem;
                    margin-bottom: 0.5rem;
                }
                
                .timestamp {
                    color: #95a5a6;
                    font-size: 0.8rem;
                }
                
                .status {
                    display: inline-block;
                    padding: 0.2rem 0.6rem;
                    border-radius: 3px;
                    font-size: 0.8rem;
                    font-weight: 500;
                    margin-top: 0.5rem;
                }
                
                .status.success {
                    background-color: #d4edda;
                    color: #155724;
                }
                
                .status.failure {
                    background-color: #f8d7da;
                    color: #721c24;
                }
                
                .tags {
                    margin-top: 0.5rem;
                    display: flex;
                    gap: 0.3rem;
                    flex-wrap: wrap;
                }
                
                .tag {
                    background-color: #e9ecef;
                    color: #495057;
                    padding: 0.2rem 0.5rem;
                    border-radius: 3px;
                    font-size: 0.75rem;
                }
                
                /* Lightbox */
                .lightbox {
                    display: none;
                    position: fixed;
                    z-index: 1000;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    background-color: rgba(0,0,0,0.9);
                    cursor: pointer;
                }
                
                .lightbox-content {
                    position: absolute;
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);
                    max-width: 90%;
                    max-height: 90%;
                }
                
                .lightbox-image {
                    width: 100%;
                    height: 100%;
                    object-fit: contain;
                }
                
                .close {
                    position: absolute;
                    top: 20px;
                    right: 40px;
                    color: white;
                    font-size: 40px;
                    font-weight: bold;
                    cursor: pointer;
                }
                
                .close:hover { color: #ccc; }
                
                /* Timeline View */
                .timeline-toggle {
                    position: fixed;
                    bottom: 20px;
                    right: 20px;
                    padding: 1rem 1.5rem;
                    background-color: #3498db;
                    color: white;
                    border: none;
                    border-radius: 50px;
                    box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
                    cursor: pointer;
                    font-weight: 500;
                }
                
                .timeline-toggle:hover {
                    background-color: #2980b9;
                }
                
                @media (max-width: 768px) {
                    .gallery {
                        grid-template-columns: 1fr;
                        padding: 1rem;
                    }
                }
            </style>
        </head>
        <body>
            <header>
                <h1>Brobot Illustration Gallery</h1>
                <div class="subtitle">Session: %s | %s</div>
            </header>
            
            <div class="filters">
                <div class="filter-group">
                    <label for="actionFilter">Action Type:</label>
                    <select id="actionFilter">
                        <option value="">All Actions</option>
                        %s
                    </select>
                </div>
                
                <div class="filter-group">
                    <label for="statusFilter">Status:</label>
                    <select id="statusFilter">
                        <option value="">All</option>
                        <option value="success">Success</option>
                        <option value="failure">Failure</option>
                    </select>
                </div>
                
                <button onclick="resetFilters()">Reset Filters</button>
                
                <div class="stats">
                    Showing <span id="visibleCount">%d</span> of %d illustrations
                </div>
            </div>
            
            <div class="gallery" id="gallery">
                %s
            </div>
            
            <div class="lightbox" id="lightbox" onclick="closeLightbox()">
                <span class="close">&times;</span>
                <div class="lightbox-content">
                    <img class="lightbox-image" id="lightboxImage" src="" alt="">
                </div>
            </div>
            
            <button class="timeline-toggle" onclick="toggleTimeline()">Timeline View</button>
            
            <script>
                function filterGallery() {
                    const actionFilter = document.getElementById('actionFilter').value;
                    const statusFilter = document.getElementById('statusFilter').value;
                    const cards = document.querySelectorAll('.illustration-card');
                    let visibleCount = 0;
                    
                    cards.forEach(card => {
                        const actionType = card.dataset.action;
                        const status = card.dataset.status;
                        
                        const actionMatch = !actionFilter || actionType === actionFilter;
                        const statusMatch = !statusFilter || status === statusFilter;
                        
                        if (actionMatch && statusMatch) {
                            card.style.display = 'block';
                            visibleCount++;
                        } else {
                            card.style.display = 'none';
                        }
                    });
                    
                    document.getElementById('visibleCount').textContent = visibleCount;
                }
                
                function resetFilters() {
                    document.getElementById('actionFilter').value = '';
                    document.getElementById('statusFilter').value = '';
                    filterGallery();
                }
                
                function openLightbox(imageSrc) {
                    document.getElementById('lightboxImage').src = imageSrc;
                    document.getElementById('lightbox').style.display = 'block';
                }
                
                function closeLightbox() {
                    document.getElementById('lightbox').style.display = 'none';
                }
                
                function toggleTimeline() {
                    // Timeline view implementation would go here
                    alert('Timeline view coming soon!');
                }
                
                // Add event listeners
                document.getElementById('actionFilter').addEventListener('change', filterGallery);
                document.getElementById('statusFilter').addEventListener('change', filterGallery);
                
                // ESC key to close lightbox
                document.addEventListener('keydown', function(e) {
                    if (e.key === 'Escape') closeLightbox();
                });
            </script>
        </body>
        </html>
        """;
    
    /**
     * Exports a gallery to static HTML.
     *
     * @param illustrations list of illustrations to export
     * @param exportPath target directory
     * @param sessionId session identifier
     * @return path to the generated index.html
     */
    public Path exportGallery(List<IllustrationEntity> illustrations, String exportPath, String sessionId) {
        try {
            Path exportDir = Paths.get(exportPath);
            Files.createDirectories(exportDir);
            
            // Copy images
            Path imagesDir = exportDir.resolve("images");
            Files.createDirectories(imagesDir);
            
            for (IllustrationEntity illustration : illustrations) {
                Path sourcePath = Paths.get(illustration.getFilePath());
                if (Files.exists(sourcePath)) {
                    Path targetPath = imagesDir.resolve(illustration.getFilename());
                    Files.copy(sourcePath, targetPath);
                }
            }
            
            // Generate HTML
            String html = generateHtml(illustrations, sessionId);
            
            // Write HTML file
            Path indexPath = exportDir.resolve("index.html");
            Files.writeString(indexPath, html);
            
            log.info("Exported gallery with {} illustrations to {}", illustrations.size(), exportPath);
            
            return indexPath;
            
        } catch (IOException e) {
            log.error("Failed to export gallery", e);
            throw new RuntimeException("Failed to export gallery", e);
        }
    }
    
    /**
     * Generates the HTML content for the gallery.
     */
    private String generateHtml(List<IllustrationEntity> illustrations, String sessionId) {
        String timestamp = illustrations.isEmpty() ? "No illustrations" : 
            illustrations.get(0).getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        // Get unique action types
        String actionOptions = illustrations.stream()
            .map(IllustrationEntity::getActionType)
            .distinct()
            .sorted()
            .map(action -> String.format("<option value=\"%s\">%s</option>", action, action))
            .collect(Collectors.joining("\n"));
        
        // Generate illustration cards
        String illustrationCards = illustrations.stream()
            .map(this::generateIllustrationCard)
            .collect(Collectors.joining("\n"));
        
        return String.format(HTML_TEMPLATE,
            sessionId,
            sessionId,
            timestamp,
            actionOptions,
            illustrations.size(),
            illustrations.size(),
            illustrationCards
        );
    }
    
    /**
     * Generates HTML for a single illustration card.
     */
    private String generateIllustrationCard(IllustrationEntity illustration) {
        String statusClass = illustration.isSuccess() ? "success" : "failure";
        String statusText = illustration.isSuccess() ? "Success" : "Failed";
        
        String tags = "";
        if (illustration.getTags() != null && !illustration.getTags().isEmpty()) {
            tags = "<div class=\"tags\">" +
                illustration.getTags().stream()
                    .map(tag -> String.format("<span class=\"tag\">%s</span>", tag))
                    .collect(Collectors.joining("")) +
                "</div>";
        }
        
        return String.format("""
            <div class="illustration-card" data-action="%s" data-status="%s">
                <img class="illustration-image" 
                     src="images/%s" 
                     alt="%s illustration"
                     onclick="openLightbox('images/%s')">
                <div class="illustration-info">
                    <div class="action-type">%s</div>
                    <div class="state-name">%s</div>
                    <div class="timestamp">%s</div>
                    <span class="status %s">%s</span>
                    %s
                </div>
            </div>
            """,
            illustration.getActionType(),
            statusClass,
            illustration.getFilename(),
            illustration.getActionType(),
            illustration.getFilename(),
            illustration.getActionType(),
            illustration.getStateName() != null ? illustration.getStateName() : "Unknown State",
            illustration.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            statusClass,
            statusText,
            tags
        );
    }
}