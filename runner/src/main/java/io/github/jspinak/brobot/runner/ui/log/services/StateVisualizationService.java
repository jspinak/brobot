package io.github.jspinak.brobot.runner.ui.log.services;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for creating and managing state transition visualizations.
 * Provides various visualization styles and layouts for state diagrams.
 */
@Slf4j
@Service
public class StateVisualizationService {
    
    private static final double DEFAULT_NODE_SIZE = 60;
    private static final double DEFAULT_SPACING = 80;
    private static final double ARROW_HEAD_SIZE = 10;
    
    /**
     * Visualization types available.
     */
    public enum VisualizationType {
        CIRCLE_NODES("Circle Nodes", "Traditional state diagram with circular nodes"),
        RECTANGLE_NODES("Rectangle Nodes", "State diagram with rectangular nodes"),
        HIERARCHICAL("Hierarchical", "Tree-like hierarchical layout"),
        RADIAL("Radial", "Radial layout with central node"),
        FLOW("Flow", "Flow chart style visualization");
        
        private final String displayName;
        private final String description;
        
        VisualizationType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    /**
     * Theme configuration for visualizations.
     */
    public static class VisualizationTheme {
        private Color nodeColor = Color.LIGHTSKYBLUE;
        private Color nodeStrokeColor = Color.STEELBLUE;
        private Color textColor = Color.BLACK;
        private Color arrowColor = Color.BLACK;
        private Color backgroundColor = Color.WHITE;
        private double nodeStrokeWidth = 2.0;
        private double arrowStrokeWidth = 2.0;
        private Font nodeFont = Font.font("System", 12);
        private Font titleFont = Font.font("System", FontWeight.BOLD, 14);
        
        public static VisualizationThemeBuilder builder() {
            return new VisualizationThemeBuilder();
        }
        
        public static class VisualizationThemeBuilder {
            private VisualizationTheme theme = new VisualizationTheme();
            
            public VisualizationThemeBuilder nodeColor(Color color) {
                theme.nodeColor = color;
                return this;
            }
            
            public VisualizationThemeBuilder nodeStrokeColor(Color color) {
                theme.nodeStrokeColor = color;
                return this;
            }
            
            public VisualizationThemeBuilder textColor(Color color) {
                theme.textColor = color;
                return this;
            }
            
            public VisualizationThemeBuilder arrowColor(Color color) {
                theme.arrowColor = color;
                return this;
            }
            
            public VisualizationThemeBuilder backgroundColor(Color color) {
                theme.backgroundColor = color;
                return this;
            }
            
            public VisualizationTheme build() {
                return theme;
            }
        }
        
        // Getters
        public Color getNodeColor() { return nodeColor; }
        public Color getNodeStrokeColor() { return nodeStrokeColor; }
        public Color getTextColor() { return textColor; }
        public Color getArrowColor() { return arrowColor; }
        public Color getBackgroundColor() { return backgroundColor; }
        public double getNodeStrokeWidth() { return nodeStrokeWidth; }
        public double getArrowStrokeWidth() { return arrowStrokeWidth; }
        public Font getNodeFont() { return nodeFont; }
        public Font getTitleFont() { return titleFont; }
    }
    
    /**
     * State visualization panel implementation.
     */
    public static class StateVisualizationPanel extends VBox {
        private final Pane canvas;
        private final Label titleLabel;
        private VisualizationType visualizationType = VisualizationType.CIRCLE_NODES;
        private VisualizationTheme theme = new VisualizationTheme();
        
        public StateVisualizationPanel() {
            setPadding(new Insets(10));
            setSpacing(10);
            
            titleLabel = new Label("State Visualization");
            titleLabel.setFont(theme.getTitleFont());
            
            canvas = new Pane();
            canvas.setMinHeight(200);
            canvas.setPrefHeight(300);
            updateCanvasStyle();
            
            getChildren().addAll(titleLabel, canvas);
        }
        
        private void updateCanvasStyle() {
            String bgColor = toHexString(theme.getBackgroundColor());
            canvas.setStyle(String.format(
                "-fx-border-color: #cccccc; -fx-border-radius: 5; -fx-background-color: %s;",
                bgColor
            ));
        }
        
        private String toHexString(Color color) {
            return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
        }
        
        public void setVisualizationType(VisualizationType type) {
            this.visualizationType = type;
        }
        
        public void setTheme(VisualizationTheme theme) {
            this.theme = theme;
            titleLabel.setFont(theme.getTitleFont());
            titleLabel.setTextFill(theme.getTextColor());
            updateCanvasStyle();
        }
        
        public Pane getCanvas() {
            return canvas;
        }
        
        public void setTitle(String title) {
            titleLabel.setText(title);
        }
    }
    
    /**
     * Creates a new state visualization panel.
     */
    public StateVisualizationPanel createVisualizationPanel() {
        return new StateVisualizationPanel();
    }
    
    /**
     * Updates the visualization with state transition.
     */
    public void visualizeTransition(StateVisualizationPanel panel, 
                                  List<String> fromStates, 
                                  List<String> toStates) {
        Platform.runLater(() -> {
            panel.getCanvas().getChildren().clear();
            panel.setTitle("State Transition: " + 
                String.join(", ", fromStates) + " → " + 
                String.join(", ", toStates));
            
            double canvasWidth = Math.max(panel.getCanvas().getWidth(), 600);
            double canvasHeight = Math.max(panel.getCanvas().getHeight(), 200);
            
            switch (panel.visualizationType) {
                case CIRCLE_NODES:
                    drawCircleTransition(panel, fromStates, toStates, canvasWidth, canvasHeight);
                    break;
                case RECTANGLE_NODES:
                    drawRectangleTransition(panel, fromStates, toStates, canvasWidth, canvasHeight);
                    break;
                case HIERARCHICAL:
                    drawHierarchicalTransition(panel, fromStates, toStates, canvasWidth, canvasHeight);
                    break;
                case RADIAL:
                    drawRadialTransition(panel, fromStates, toStates, canvasWidth, canvasHeight);
                    break;
                case FLOW:
                    drawFlowTransition(panel, fromStates, toStates, canvasWidth, canvasHeight);
                    break;
            }
        });
    }
    
    /**
     * Visualizes the current state.
     */
    public void visualizeCurrentState(StateVisualizationPanel panel, String stateName) {
        Platform.runLater(() -> {
            panel.getCanvas().getChildren().clear();
            panel.setTitle("Current State: " + stateName);
            
            double canvasWidth = Math.max(panel.getCanvas().getWidth(), 600);
            double canvasHeight = Math.max(panel.getCanvas().getHeight(), 200);
            
            switch (panel.visualizationType) {
                case CIRCLE_NODES:
                    drawCircleNode(panel, stateName, canvasWidth / 2, canvasHeight / 2, true);
                    break;
                case RECTANGLE_NODES:
                    drawRectangleNode(panel, stateName, canvasWidth / 2, canvasHeight / 2, true);
                    break;
                default:
                    drawCircleNode(panel, stateName, canvasWidth / 2, canvasHeight / 2, true);
            }
        });
    }
    
    /**
     * Clears the visualization.
     */
    public void clearVisualization(StateVisualizationPanel panel) {
        Platform.runLater(() -> {
            panel.getCanvas().getChildren().clear();
            panel.setTitle("State Visualization");
        });
    }
    
    // Circle node transition visualization
    private void drawCircleTransition(StateVisualizationPanel panel,
                                    List<String> fromStates,
                                    List<String> toStates,
                                    double canvasWidth,
                                    double canvasHeight) {
        double leftX = canvasWidth * 0.25;
        double rightX = canvasWidth * 0.75;
        
        // Draw from states
        List<Node> fromNodes = drawStateGroup(panel, fromStates, leftX, canvasHeight / 2, 
            this::drawCircleNode);
        
        // Draw to states
        List<Node> toNodes = drawStateGroup(panel, toStates, rightX, canvasHeight / 2, 
            this::drawCircleNode);
        
        // Draw arrows
        drawArrows(panel, fromNodes, toNodes);
    }
    
    // Rectangle node transition visualization
    private void drawRectangleTransition(StateVisualizationPanel panel,
                                       List<String> fromStates,
                                       List<String> toStates,
                                       double canvasWidth,
                                       double canvasHeight) {
        double leftX = canvasWidth * 0.25;
        double rightX = canvasWidth * 0.75;
        
        // Draw from states
        List<Node> fromNodes = drawStateGroup(panel, fromStates, leftX, canvasHeight / 2, 
            this::drawRectangleNode);
        
        // Draw to states
        List<Node> toNodes = drawStateGroup(panel, toStates, rightX, canvasHeight / 2, 
            this::drawRectangleNode);
        
        // Draw arrows
        drawArrows(panel, fromNodes, toNodes);
    }
    
    // Hierarchical layout
    private void drawHierarchicalTransition(StateVisualizationPanel panel,
                                          List<String> fromStates,
                                          List<String> toStates,
                                          double canvasWidth,
                                          double canvasHeight) {
        // Simplified hierarchical layout
        double topY = canvasHeight * 0.3;
        double bottomY = canvasHeight * 0.7;
        
        // Draw parent states at top
        List<Node> fromNodes = new ArrayList<>();
        double fromSpacing = canvasWidth / (fromStates.size() + 1);
        for (int i = 0; i < fromStates.size(); i++) {
            Node node = drawCircleNode(panel, fromStates.get(i), 
                fromSpacing * (i + 1), topY, false);
            fromNodes.add(node);
        }
        
        // Draw child states at bottom
        List<Node> toNodes = new ArrayList<>();
        double toSpacing = canvasWidth / (toStates.size() + 1);
        for (int i = 0; i < toStates.size(); i++) {
            Node node = drawCircleNode(panel, toStates.get(i), 
                toSpacing * (i + 1), bottomY, false);
            toNodes.add(node);
        }
        
        // Draw hierarchical arrows
        drawArrows(panel, fromNodes, toNodes);
    }
    
    // Radial layout
    private void drawRadialTransition(StateVisualizationPanel panel,
                                    List<String> fromStates,
                                    List<String> toStates,
                                    double canvasWidth,
                                    double canvasHeight) {
        double centerX = canvasWidth / 2;
        double centerY = canvasHeight / 2;
        double radius = Math.min(canvasWidth, canvasHeight) * 0.3;
        
        // Draw center node (transition point)
        Node centerNode = drawCircleNode(panel, "→", centerX, centerY, true);
        
        // Draw from states in semi-circle on left
        List<Node> fromNodes = new ArrayList<>();
        double fromAngleStep = Math.PI / (fromStates.size() + 1);
        for (int i = 0; i < fromStates.size(); i++) {
            double angle = Math.PI - fromAngleStep * (i + 1);
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            Node node = drawCircleNode(panel, fromStates.get(i), x, y, false);
            fromNodes.add(node);
            drawArrow(panel, x, y, centerX, centerY);
        }
        
        // Draw to states in semi-circle on right
        double toAngleStep = Math.PI / (toStates.size() + 1);
        for (int i = 0; i < toStates.size(); i++) {
            double angle = toAngleStep * (i + 1);
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            drawCircleNode(panel, toStates.get(i), x, y, false);
            drawArrow(panel, centerX, centerY, x, y);
        }
    }
    
    // Flow chart style
    private void drawFlowTransition(StateVisualizationPanel panel,
                                  List<String> fromStates,
                                  List<String> toStates,
                                  double canvasWidth,
                                  double canvasHeight) {
        // Similar to rectangle but with different styling
        drawRectangleTransition(panel, fromStates, toStates, canvasWidth, canvasHeight);
    }
    
    // Helper method to draw state groups
    private List<Node> drawStateGroup(StateVisualizationPanel panel,
                                    List<String> states,
                                    double x,
                                    double y,
                                    StateNodeDrawer drawer) {
        List<Node> nodes = new ArrayList<>();
        if (states.isEmpty()) return nodes;
        
        double spacing = DEFAULT_SPACING;
        double startY = y - ((states.size() - 1) * spacing / 2);
        
        for (int i = 0; i < states.size(); i++) {
            Node node = drawer.draw(panel, states.get(i), x, startY + i * spacing, false);
            nodes.add(node);
        }
        
        return nodes;
    }
    
    // Functional interface for node drawing
    private interface StateNodeDrawer {
        Node draw(StateVisualizationPanel panel, String name, double x, double y, boolean highlight);
    }
    
    // Draw circle node
    private Node drawCircleNode(StateVisualizationPanel panel, 
                               String stateName, 
                               double x, 
                               double y,
                               boolean highlight) {
        VisualizationTheme theme = panel.theme;
        
        Circle circle = new Circle(DEFAULT_NODE_SIZE / 2);
        circle.setFill(highlight ? theme.getNodeStrokeColor() : theme.getNodeColor());
        circle.setStroke(theme.getNodeStrokeColor());
        circle.setStrokeWidth(theme.getNodeStrokeWidth());
        
        Label label = new Label(stateName);
        label.setFont(theme.getNodeFont());
        label.setTextFill(highlight ? Color.WHITE : theme.getTextColor());
        
        StackPane stack = new StackPane(circle, label);
        stack.setLayoutX(x - DEFAULT_NODE_SIZE / 2);
        stack.setLayoutY(y - DEFAULT_NODE_SIZE / 2);
        
        panel.getCanvas().getChildren().add(stack);
        return stack;
    }
    
    // Draw rectangle node
    private Node drawRectangleNode(StateVisualizationPanel panel,
                                 String stateName,
                                 double x,
                                 double y,
                                 boolean highlight) {
        VisualizationTheme theme = panel.theme;
        
        Rectangle rect = new Rectangle(DEFAULT_NODE_SIZE * 1.5, DEFAULT_NODE_SIZE);
        rect.setFill(highlight ? theme.getNodeStrokeColor() : theme.getNodeColor());
        rect.setStroke(theme.getNodeStrokeColor());
        rect.setStrokeWidth(theme.getNodeStrokeWidth());
        rect.setArcWidth(10);
        rect.setArcHeight(10);
        
        Label label = new Label(stateName);
        label.setFont(theme.getNodeFont());
        label.setTextFill(highlight ? Color.WHITE : theme.getTextColor());
        
        StackPane stack = new StackPane(rect, label);
        stack.setLayoutX(x - DEFAULT_NODE_SIZE * 0.75);
        stack.setLayoutY(y - DEFAULT_NODE_SIZE / 2);
        
        panel.getCanvas().getChildren().add(stack);
        return stack;
    }
    
    // Draw arrows between node groups
    private void drawArrows(StateVisualizationPanel panel,
                          List<Node> fromNodes,
                          List<Node> toNodes) {
        for (Node fromNode : fromNodes) {
            for (Node toNode : toNodes) {
                double fromX = fromNode.getLayoutX() + fromNode.getBoundsInLocal().getWidth() / 2;
                double fromY = fromNode.getLayoutY() + fromNode.getBoundsInLocal().getHeight() / 2;
                double toX = toNode.getLayoutX() + toNode.getBoundsInLocal().getWidth() / 2;
                double toY = toNode.getLayoutY() + toNode.getBoundsInLocal().getHeight() / 2;
                
                // Adjust start and end points to node edges
                double angle = Math.atan2(toY - fromY, toX - fromX);
                fromX += (DEFAULT_NODE_SIZE / 2) * Math.cos(angle);
                fromY += (DEFAULT_NODE_SIZE / 2) * Math.sin(angle);
                toX -= (DEFAULT_NODE_SIZE / 2) * Math.cos(angle);
                toY -= (DEFAULT_NODE_SIZE / 2) * Math.sin(angle);
                
                drawArrow(panel, fromX, fromY, toX, toY);
            }
        }
    }
    
    // Draw single arrow
    private void drawArrow(StateVisualizationPanel panel,
                         double startX,
                         double startY,
                         double endX,
                         double endY) {
        VisualizationTheme theme = panel.theme;
        
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(theme.getArrowColor());
        line.setStrokeWidth(theme.getArrowStrokeWidth());
        
        // Calculate arrow head
        double angle = Math.atan2(endY - startY, endX - startX);
        
        Polygon arrowHead = new Polygon();
        arrowHead.getPoints().addAll(
            endX, endY,
            endX - ARROW_HEAD_SIZE * Math.cos(angle - Math.PI / 6),
            endY - ARROW_HEAD_SIZE * Math.sin(angle - Math.PI / 6),
            endX - ARROW_HEAD_SIZE * Math.cos(angle + Math.PI / 6),
            endY - ARROW_HEAD_SIZE * Math.sin(angle + Math.PI / 6)
        );
        arrowHead.setFill(theme.getArrowColor());
        
        panel.getCanvas().getChildren().addAll(line, arrowHead);
    }
    
    /**
     * Creates predefined themes.
     */
    public static class PredefinedThemes {
        public static VisualizationTheme light() {
            return VisualizationTheme.builder()
                .nodeColor(Color.LIGHTSKYBLUE)
                .nodeStrokeColor(Color.STEELBLUE)
                .textColor(Color.BLACK)
                .arrowColor(Color.BLACK)
                .backgroundColor(Color.WHITE)
                .build();
        }
        
        public static VisualizationTheme dark() {
            return VisualizationTheme.builder()
                .nodeColor(Color.DARKSLATEBLUE)
                .nodeStrokeColor(Color.LIGHTSTEELBLUE)
                .textColor(Color.WHITE)
                .arrowColor(Color.LIGHTGRAY)
                .backgroundColor(Color.rgb(30, 30, 30))
                .build();
        }
        
        public static VisualizationTheme highContrast() {
            return VisualizationTheme.builder()
                .nodeColor(Color.WHITE)
                .nodeStrokeColor(Color.BLACK)
                .textColor(Color.BLACK)
                .arrowColor(Color.BLACK)
                .backgroundColor(Color.WHITE)
                .build();
        }
    }
}