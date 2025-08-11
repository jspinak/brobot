package io.github.jspinak.brobot.model.element;

import lombok.extern.slf4j.Slf4j;
import static io.github.jspinak.brobot.model.element.Positions.Name.*;

/**
 * Builder for creating Region objects with screen-size awareness and flexible adjustments.
 * 
 * <p>This builder provides multiple ways to define regions:
 * <ul>
 *   <li>Absolute coordinates and dimensions</li>
 *   <li>Percentage-based positioning relative to screen size</li>
 *   <li>Anchor-based positioning (center, corners, edges)</li>
 *   <li>Adjustments to existing regions</li>
 *   <li>Screen-relative sizing with aspect ratio preservation</li>
 * </ul>
 * </p>
 * 
 * <p>Example usage:
 * <pre>{@code
 * // Create a region in the center of the screen, 50% of screen size
 * Region centerRegion = new RegionBuilder()
 *     .withScreenPercentage(0.5, 0.5)
 *     .centerOnScreen()
 *     .build();
 * 
 * // Create a region with specific coordinates
 * Region searchArea = new RegionBuilder()
 *     .x(100).y(100)
 *     .width(200).height(150)
 *     .build();
 * 
 * // Create a region with adjustments
 * Region expanded = new RegionBuilder()
 *     .fromRegion(existingRegion)
 *     .adjustX(10)
 *     .adjustY(-5)
 *     .adjustWidth(20)
 *     .adjustHeight(20)
 *     .build();
 * }</pre>
 * </p>
 * 
 * @since 1.2.0
 */
@Slf4j
public class RegionBuilder {
    
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
    
    // Screen-relative positioning
    private Double xPercent;
    private Double yPercent;
    private Double widthPercent;
    private Double heightPercent;
    
    // Adjustments
    private int xAdjustment = 0;
    private int yAdjustment = 0;
    private int widthAdjustment = 0;
    private int heightAdjustment = 0;
    
    // Current screen dimensions (for percentage calculations only)
    private int currentScreenWidth = 0;
    private int currentScreenHeight = 0;
    
    // Anchor positioning using Position
    private Position anchorPosition = null;
    private Positions.Name anchorName = TOPLEFT;
    
    // Position-based placement
    private Position relativePosition = null;
    private Region relativeToRegion = null;
    
    // Constraints
    private boolean constrainToScreen = true;
    private boolean maintainAspectRatio = false;
    private double aspectRatio = 0;
    
    /**
     * Creates a new RegionBuilder with current screen dimensions detected.
     */
    public RegionBuilder() {
        detectCurrentScreenSize();
    }
    
    /**
     * Sets the base region using absolute coordinates.
     * 
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param width the width
     * @param height the height
     * @return this builder
     */
    public RegionBuilder withRegion(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }
    
    /**
     * Creates a builder from an existing region.
     * 
     * @param region the source region
     * @return this builder
     */
    public RegionBuilder fromRegion(Region region) {
        if (region != null) {
            this.x = region.getX();
            this.y = region.getY();
            this.width = region.getW();
            this.height = region.getH();
        }
        return this;
    }
    
    /**
     * Sets the position using absolute coordinates.
     * 
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return this builder
     */
    public RegionBuilder withPosition(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    /**
     * Sets the size using absolute dimensions.
     * 
     * @param width the width
     * @param height the height
     * @return this builder
     */
    public RegionBuilder withSize(int width, int height) {
        this.width = width;
        this.height = height;
        if (maintainAspectRatio && width > 0 && height > 0) {
            this.aspectRatio = (double) width / height;
        }
        return this;
    }
    
    /**
     * Sets the width directly.
     * 
     * @param width the width
     * @return this builder
     */
    public RegionBuilder withWidth(int width) {
        this.width = width;
        if (maintainAspectRatio && height != null && height > 0) {
            this.aspectRatio = (double) width / height;
        }
        return this;
    }
    
    /**
     * Sets the height directly.
     * 
     * @param height the height
     * @return this builder
     */
    public RegionBuilder withHeight(int height) {
        this.height = height;
        if (maintainAspectRatio && width != null && width > 0) {
            this.aspectRatio = (double) width / height;
        }
        return this;
    }
    
    /**
     * Sets the position using screen percentage (0.0 to 1.0).
     * 
     * @param xPercent x position as percentage of screen width
     * @param yPercent y position as percentage of screen height
     * @return this builder
     */
    public RegionBuilder withScreenPercentagePosition(double xPercent, double yPercent) {
        this.xPercent = xPercent;
        this.yPercent = yPercent;
        return this;
    }
    
    /**
     * Sets the size using screen percentage (0.0 to 1.0).
     * 
     * @param widthPercent width as percentage of screen width
     * @param heightPercent height as percentage of screen height
     * @return this builder
     */
    public RegionBuilder withScreenPercentageSize(double widthPercent, double heightPercent) {
        this.widthPercent = widthPercent;
        this.heightPercent = heightPercent;
        return this;
    }
    
    /**
     * Sets both position and size using screen percentages.
     * 
     * @param xPercent x position as percentage
     * @param yPercent y position as percentage
     * @param widthPercent width as percentage
     * @param heightPercent height as percentage
     * @return this builder
     */
    public RegionBuilder withScreenPercentage(double xPercent, double yPercent, 
                                             double widthPercent, double heightPercent) {
        this.xPercent = xPercent;
        this.yPercent = yPercent;
        this.widthPercent = widthPercent;
        this.heightPercent = heightPercent;
        return this;
    }
    
    /**
     * Convenience method to set size using screen percentage.
     * 
     * @param widthPercent width as percentage of screen
     * @param heightPercent height as percentage of screen
     * @return this builder
     */
    public RegionBuilder withScreenPercentage(double widthPercent, double heightPercent) {
        this.widthPercent = widthPercent;
        this.heightPercent = heightPercent;
        return this;
    }
    
    /**
     * Adds an adjustment to the x-coordinate.
     * 
     * @param adjustment pixels to add (negative to subtract)
     * @return this builder
     */
    public RegionBuilder adjustX(int adjustment) {
        this.xAdjustment = adjustment;
        return this;
    }
    
    /**
     * Adds an adjustment to the y-coordinate.
     * 
     * @param adjustment pixels to add (negative to subtract)
     * @return this builder
     */
    public RegionBuilder adjustY(int adjustment) {
        this.yAdjustment = adjustment;
        return this;
    }
    
    /**
     * Adds an adjustment to the width.
     * 
     * @param adjustment pixels to add (negative to subtract)
     * @return this builder
     */
    public RegionBuilder adjustWidth(int adjustment) {
        this.widthAdjustment = adjustment;
        return this;
    }
    
    /**
     * Adds an adjustment to the height.
     * 
     * @param adjustment pixels to add (negative to subtract)
     * @return this builder
     */
    public RegionBuilder adjustHeight(int adjustment) {
        this.heightAdjustment = adjustment;
        return this;
    }
    
    /**
     * Adds adjustments to all dimensions.
     * 
     * @param x x adjustment
     * @param y y adjustment
     * @param width width adjustment
     * @param height height adjustment
     * @return this builder
     */
    public RegionBuilder adjustBy(int x, int y, int width, int height) {
        this.xAdjustment = x;
        this.yAdjustment = y;
        this.widthAdjustment = width;
        this.heightAdjustment = height;
        return this;
    }
    
    /**
     * Expands or contracts the region by the specified amount on all sides.
     * 
     * @param pixels pixels to expand (negative to contract)
     * @return this builder
     */
    public RegionBuilder expand(int pixels) {
        this.xAdjustment = -pixels;
        this.yAdjustment = -pixels;
        this.widthAdjustment = pixels * 2;
        this.heightAdjustment = pixels * 2;
        return this;
    }
    
    
    /**
     * Sets the anchor point using Positions.Name enum.
     * 
     * @param anchorName the anchor point from Positions.Name
     * @return this builder
     */
    public RegionBuilder withAnchor(Positions.Name anchorName) {
        this.anchorName = anchorName;
        this.anchorPosition = new Position(anchorName);
        return this;
    }
    
    /**
     * Sets the anchor point using a custom Position.
     * 
     * @param position the custom position for anchoring (0.0 to 1.0 relative positioning)
     * @return this builder
     */
    public RegionBuilder withAnchor(Position position) {
        this.anchorPosition = position;
        this.anchorName = null;
        return this;
    }
    
    /**
     * Positions this region relative to another region using a Position.
     * The Position determines where in the reference region this region's anchor point will be placed.
     * 
     * @param referenceRegion the region to position relative to
     * @param position the position within the reference region (0.0 to 1.0 relative positioning)
     * @return this builder
     */
    public RegionBuilder positionRelativeTo(Region referenceRegion, Position position) {
        this.relativeToRegion = referenceRegion;
        this.relativePosition = position;
        return this;
    }
    
    /**
     * Positions this region relative to another region using a named position.
     * 
     * @param referenceRegion the region to position relative to
     * @param positionName the named position from Positions.Name
     * @return this builder
     */
    public RegionBuilder positionRelativeTo(Region referenceRegion, Positions.Name positionName) {
        return positionRelativeTo(referenceRegion, new Position(positionName));
    }
    
    /**
     * Creates a region at a specific position within the screen using Position percentages.
     * 
     * @param position the position defining where to place the region (0.0 to 1.0 scale)
     * @return this builder
     */
    public RegionBuilder withPosition(Position position) {
        if (position != null) {
            this.xPercent = position.getPercentW();
            this.yPercent = position.getPercentH();
        }
        return this;
    }
    
    /**
     * Creates a region at a named position on the screen.
     * 
     * @param positionName the named position from Positions.Name
     * @return this builder
     */
    public RegionBuilder withPosition(Positions.Name positionName) {
        return withPosition(new Position(positionName));
    }
    
    /**
     * Centers the region on the screen.
     * 
     * @return this builder
     */
    public RegionBuilder centerOnScreen() {
        this.anchorName = MIDDLEMIDDLE;
        this.anchorPosition = new Position(MIDDLEMIDDLE);
        return this;
    }
    
    /**
     * Positions the region at the top-left of the screen.
     * 
     * @return this builder
     */
    public RegionBuilder topLeft() {
        this.anchorName = TOPLEFT;
        this.anchorPosition = new Position(TOPLEFT);
        return this;
    }
    
    /**
     * Positions the region at the top-right of the screen.
     * 
     * @return this builder
     */
    public RegionBuilder topRight() {
        this.anchorName = TOPRIGHT;
        this.anchorPosition = new Position(TOPRIGHT);
        return this;
    }
    
    /**
     * Positions the region at the bottom-left of the screen.
     * 
     * @return this builder
     */
    public RegionBuilder bottomLeft() {
        this.anchorName = BOTTOMLEFT;
        this.anchorPosition = new Position(BOTTOMLEFT);
        return this;
    }
    
    /**
     * Positions the region at the bottom-right of the screen.
     * 
     * @return this builder
     */
    public RegionBuilder bottomRight() {
        this.anchorName = BOTTOMRIGHT;
        this.anchorPosition = new Position(BOTTOMRIGHT);
        return this;
    }
    
    /**
     * Positions the region at the top-center of the screen.
     * 
     * @return this builder
     */
    public RegionBuilder topCenter() {
        this.anchorName = TOPMIDDLE;
        this.anchorPosition = new Position(TOPMIDDLE);
        return this;
    }
    
    /**
     * Positions the region at the bottom-center of the screen.
     * 
     * @return this builder
     */
    public RegionBuilder bottomCenter() {
        this.anchorName = BOTTOMMIDDLE;
        this.anchorPosition = new Position(BOTTOMMIDDLE);
        return this;
    }
    
    /**
     * Positions the region at the left-center of the screen.
     * 
     * @return this builder
     */
    public RegionBuilder leftCenter() {
        this.anchorName = MIDDLELEFT;
        this.anchorPosition = new Position(MIDDLELEFT);
        return this;
    }
    
    /**
     * Positions the region at the right-center of the screen.
     * 
     * @return this builder
     */
    public RegionBuilder rightCenter() {
        this.anchorName = MIDDLERIGHT;
        this.anchorPosition = new Position(MIDDLERIGHT);
        return this;
    }
    
    /**
     * Ensures the region stays within screen bounds.
     * 
     * @param constrain true to constrain to screen
     * @return this builder
     */
    public RegionBuilder constrainToScreen(boolean constrain) {
        this.constrainToScreen = constrain;
        return this;
    }
    
    /**
     * Maintains the aspect ratio when resizing.
     * 
     * @param maintain true to maintain aspect ratio
     * @return this builder
     */
    public RegionBuilder maintainAspectRatio(boolean maintain) {
        this.maintainAspectRatio = maintain;
        if (maintain && width != null && height != null && width > 0 && height > 0) {
            this.aspectRatio = (double) width / height;
        }
        return this;
    }
    
    /**
     * Creates a region covering the full screen.
     * 
     * @return this builder
     */
    public RegionBuilder fullScreen() {
        this.x = 0;
        this.y = 0;
        this.width = currentScreenWidth;
        this.height = currentScreenHeight;
        return this;
    }
    
    /**
     * Creates a region for the top half of the screen.
     * 
     * @return this builder
     */
    public RegionBuilder topHalf() {
        this.x = 0;
        this.y = 0;
        this.width = currentScreenWidth;
        this.height = currentScreenHeight / 2;
        return this;
    }
    
    /**
     * Creates a region for the bottom half of the screen.
     * 
     * @return this builder
     */
    public RegionBuilder bottomHalf() {
        this.x = 0;
        this.y = currentScreenHeight / 2;
        this.width = currentScreenWidth;
        this.height = currentScreenHeight / 2;
        return this;
    }
    
    /**
     * Creates a region for the left half of the screen.
     * 
     * @return this builder
     */
    public RegionBuilder leftHalf() {
        this.x = 0;
        this.y = 0;
        this.width = currentScreenWidth / 2;
        this.height = currentScreenHeight;
        return this;
    }
    
    /**
     * Creates a region for the right half of the screen.
     * 
     * @return this builder
     */
    public RegionBuilder rightHalf() {
        this.x = currentScreenWidth / 2;
        this.y = 0;
        this.width = currentScreenWidth / 2;
        this.height = currentScreenHeight;
        return this;
    }
    
    /**
     * Builds the Region with all specified parameters.
     * 
     * @return the constructed Region
     */
    public Region build() {
        // Calculate final dimensions
        int finalX = calculateX();
        int finalY = calculateY();
        int finalWidth = calculateWidth();
        int finalHeight = calculateHeight();
        
        // Apply relative positioning if specified
        if (relativeToRegion != null && relativePosition != null) {
            finalX = (int) Math.round(relativeToRegion.getX() + 
                                     relativeToRegion.getW() * relativePosition.getPercentW());
            finalY = (int) Math.round(relativeToRegion.getY() + 
                                     relativeToRegion.getH() * relativePosition.getPercentH());
        }
        
        // Apply adjustments
        finalX += xAdjustment;
        finalY += yAdjustment;
        finalWidth += widthAdjustment;
        finalHeight += heightAdjustment;
        
        // Apply anchor positioning
        applyAnchor(finalX, finalY, finalWidth, finalHeight);
        
        // Constrain to screen if needed
        if (constrainToScreen) {
            finalX = Math.max(0, Math.min(finalX, currentScreenWidth - finalWidth));
            finalY = Math.max(0, Math.min(finalY, currentScreenHeight - finalHeight));
            finalWidth = Math.min(finalWidth, currentScreenWidth - finalX);
            finalHeight = Math.min(finalHeight, currentScreenHeight - finalY);
        }
        
        // Ensure positive dimensions
        finalWidth = Math.max(1, finalWidth);
        finalHeight = Math.max(1, finalHeight);
        
        log.debug("Built region: x={}, y={}, w={}, h={} (screen: {}x{})", 
                 finalX, finalY, finalWidth, finalHeight, currentScreenWidth, currentScreenHeight);
        
        return new Region(finalX, finalY, finalWidth, finalHeight);
    }
    
    private int calculateX() {
        if (xPercent != null) {
            return (int) Math.round(currentScreenWidth * xPercent);
        }
        return x != null ? x : 0;
    }
    
    private int calculateY() {
        if (yPercent != null) {
            return (int) Math.round(currentScreenHeight * yPercent);
        }
        return y != null ? y : 0;
    }
    
    private int calculateWidth() {
        if (widthPercent != null) {
            return (int) Math.round(currentScreenWidth * widthPercent);
        }
        if (width != null) {
            return width;
        }
        // Default to 100x100 if no size specified
        return 100;
    }
    
    private int calculateHeight() {
        if (heightPercent != null) {
            return (int) Math.round(currentScreenHeight * heightPercent);
        }
        if (height != null) {
            return height;
        }
        // If aspect ratio is set and width is known, calculate height
        if (maintainAspectRatio && aspectRatio > 0 && width != null) {
            return (int) Math.round(width / aspectRatio);
        }
        // Default to 100x100 if no size specified
        return 100;
    }
    
    private void applyAnchor(int x, int y, int width, int height) {
        // Use Position-based anchoring
        Position anchor = anchorPosition != null ? anchorPosition : new Position(anchorName);
        
        // Calculate position based on anchor percentages
        double anchorX = anchor.getPercentW();
        double anchorY = anchor.getPercentH();
        
        // Adjust position based on anchor point
        this.x = (int) Math.round((currentScreenWidth - width) * anchorX);
        this.y = (int) Math.round((currentScreenHeight - height) * anchorY);
        
        // If we have explicit x/y values and not using percentage positioning,
        // preserve them unless we're using named anchors
        if (xPercent == null && this.x != null && anchorName == TOPLEFT && anchorPosition == null) {
            this.x = x;
        }
        if (yPercent == null && this.y != null && anchorName == TOPLEFT && anchorPosition == null) {
            this.y = y;
        }
    }
    
    private void detectCurrentScreenSize() {
        try {
            // Try to get actual screen dimensions
            org.sikuli.script.Screen screen = new org.sikuli.script.Screen();
            if (screen.w > 0 && screen.h > 0) {
                currentScreenWidth = screen.w;
                currentScreenHeight = screen.h;
                return;
            }
        } catch (Exception e) {
            // Fall through to alternative methods
        }
        
        try {
            // Try GraphicsEnvironment
            java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
            java.awt.GraphicsDevice gd = ge.getDefaultScreenDevice();
            java.awt.DisplayMode dm = gd.getDisplayMode();
            if (dm.getWidth() > 0 && dm.getHeight() > 0) {
                currentScreenWidth = dm.getWidth();
                currentScreenHeight = dm.getHeight();
                return;
            }
        } catch (Exception e) {
            // Fall through to environment variables
        }
        
        // Fall back to environment variables or defaults
        currentScreenWidth = Integer.parseInt(System.getenv().getOrDefault("SCREEN_WIDTH", "1920"));
        currentScreenHeight = Integer.parseInt(System.getenv().getOrDefault("SCREEN_HEIGHT", "1080"));
    }
}