package io.github.jspinak.brobot.debug;

import io.github.jspinak.brobot.test.BrobotTestBase;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;

/**
 * Analyze SikuliX IDE saved images for embedded metadata
 * Check for any scaling, DPI, or other information embedded in the PNG
 */
public class SikuliXMetadataAnalyzer {
    
    @Test
    public void analyzeSikuliXImage() throws Exception {
        System.out.println("=== SIKULIX IMAGE METADATA ANALYSIS ===\n");
        
        // Check multiple possible locations
        String[] possiblePaths = {
            "images.sikuli/1755024811085.png",
            "images/prompt/claude-prompt-1.png",
            "images/working/claude-icon-1.png",
            "/home/jspinak/brobot_parent/claude-automator/matched-regions-comparison/1755024811085.png"
        };
        
        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists()) {
                System.out.println("Analyzing: " + path);
                System.out.println("=" + "=".repeat(60));
                analyzeImage(file);
                System.out.println();
            }
        }
    }
    
    private void analyzeImage(File file) throws Exception {
        System.out.println("\n1. FILE INFORMATION:");
        System.out.println("   Name: " + file.getName());
        System.out.println("   Size: " + file.length() + " bytes");
        System.out.println("   Last modified: " + new java.util.Date(file.lastModified()));
        
        // Load image to get basic properties
        BufferedImage img = ImageIO.read(file);
        System.out.println("\n2. IMAGE PROPERTIES:");
        System.out.println("   Dimensions: " + img.getWidth() + "x" + img.getHeight());
        System.out.println("   Type: " + getImageTypeName(img.getType()));
        System.out.println("   Color Model: " + img.getColorModel());
        
        // Analyze PNG metadata using ImageIO
        System.out.println("\n3. PNG METADATA (via ImageIO):");
        analyzePNGMetadata(file);
        
        // Analyze PNG chunks directly
        System.out.println("\n4. PNG CHUNKS (Direct Analysis):");
        analyzePNGChunks(file);
        
        // Check for SikuliX-specific patterns in the file
        System.out.println("\n5. SIKULIX-SPECIFIC PATTERNS:");
        checkForSikuliXPatterns(file);
    }
    
    private void analyzePNGMetadata(File file) throws Exception {
        ImageInputStream iis = ImageIO.createImageInputStream(file);
        Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
        
        if (readers.hasNext()) {
            ImageReader reader = readers.next();
            reader.setInput(iis);
            
            IIOMetadata metadata = reader.getImageMetadata(0);
            
            if (metadata != null) {
                String[] formatNames = metadata.getMetadataFormatNames();
                System.out.println("   Metadata formats: " + String.join(", ", formatNames));
                
                for (String formatName : formatNames) {
                    System.out.println("\n   Format: " + formatName);
                    IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(formatName);
                    displayMetadata(root, "     ");
                }
            } else {
                System.out.println("   No metadata found via ImageIO");
            }
            
            reader.dispose();
        }
        iis.close();
    }
    
    private void displayMetadata(Node node, String indent) {
        if (node == null) return;
        
        // Print current node
        System.out.print(indent + node.getNodeName());
        
        // Print attributes
        if (node.hasAttributes()) {
            var attrs = node.getAttributes();
            System.out.print(" [");
            for (int i = 0; i < attrs.getLength(); i++) {
                if (i > 0) System.out.print(", ");
                Node attr = attrs.item(i);
                System.out.print(attr.getNodeName() + "=" + attr.getNodeValue());
            }
            System.out.print("]");
        }
        
        // Print text content if any
        if (node.getNodeValue() != null) {
            System.out.print(" = " + node.getNodeValue());
        }
        
        System.out.println();
        
        // Process children
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            displayMetadata(children.item(i), indent + "  ");
        }
    }
    
    private void analyzePNGChunks(File file) throws Exception {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             FileChannel channel = raf.getChannel()) {
            
            ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
            channel.read(buffer);
            buffer.flip();
            
            // Check PNG signature
            byte[] signature = new byte[8];
            buffer.get(signature);
            System.out.println("   PNG Signature: " + bytesToHex(signature));
            
            // Read chunks
            while (buffer.remaining() > 0) {
                if (buffer.remaining() < 8) break; // Not enough for chunk header
                
                // Read chunk length and type
                int length = buffer.getInt();
                byte[] typeBytes = new byte[4];
                buffer.get(typeBytes);
                String type = new String(typeBytes);
                
                System.out.println("\n   Chunk: " + type);
                System.out.println("     Length: " + length + " bytes");
                
                // Read chunk data
                if (length > 0 && buffer.remaining() >= length) {
                    byte[] data = new byte[Math.min(length, 100)]; // Limit to first 100 bytes
                    buffer.get(data, 0, Math.min(length, data.length));
                    
                    // Skip remaining if chunk is larger
                    if (length > 100) {
                        buffer.position(buffer.position() + (length - 100));
                    }
                    
                    // Analyze specific chunks
                    analyzeChunkData(type, data, length);
                }
                
                // Skip CRC
                if (buffer.remaining() >= 4) {
                    buffer.getInt();
                }
            }
        }
    }
    
    private void analyzeChunkData(String type, byte[] data, int fullLength) {
        switch (type) {
            case "IHDR": // Image header
                if (data.length >= 13) {
                    ByteBuffer bb = ByteBuffer.wrap(data);
                    int width = bb.getInt();
                    int height = bb.getInt();
                    byte bitDepth = bb.get();
                    byte colorType = bb.get();
                    System.out.println("     Width: " + width);
                    System.out.println("     Height: " + height);
                    System.out.println("     Bit depth: " + bitDepth);
                    System.out.println("     Color type: " + colorType);
                }
                break;
                
            case "pHYs": // Physical pixel dimensions
                if (data.length >= 9) {
                    ByteBuffer bb = ByteBuffer.wrap(data);
                    int xPixelsPerUnit = bb.getInt();
                    int yPixelsPerUnit = bb.getInt();
                    byte unit = bb.get();
                    System.out.println("     X pixels/unit: " + xPixelsPerUnit);
                    System.out.println("     Y pixels/unit: " + yPixelsPerUnit);
                    System.out.println("     Unit: " + (unit == 1 ? "meter" : "unknown"));
                    
                    if (unit == 1) {
                        // Convert to DPI
                        double xDPI = xPixelsPerUnit * 0.0254;
                        double yDPI = yPixelsPerUnit * 0.0254;
                        System.out.println("     DPI: " + String.format("%.2f x %.2f", xDPI, yDPI));
                    }
                }
                break;
                
            case "tEXt": // Text data
            case "iTXt": // International text
            case "zTXt": // Compressed text
                System.out.println("     Text data found:");
                String text = new String(data).replace('\0', ' ');
                System.out.println("     \"" + text.substring(0, Math.min(text.length(), 100)) + "\"");
                
                // Check for SikuliX keywords
                if (text.toLowerCase().contains("sikuli") || 
                    text.toLowerCase().contains("scale") ||
                    text.toLowerCase().contains("dpi") ||
                    text.toLowerCase().contains("similarity")) {
                    System.out.println("     *** SIKULIX-RELATED METADATA FOUND! ***");
                }
                break;
                
            case "sRGB": // Standard RGB color space
                System.out.println("     sRGB chunk present");
                break;
                
            case "gAMA": // Gamma
                if (data.length >= 4) {
                    int gamma = ByteBuffer.wrap(data).getInt();
                    System.out.println("     Gamma: " + (gamma / 100000.0));
                }
                break;
                
            default:
                if (type.matches("[a-z].*")) {
                    System.out.println("     (Ancillary chunk - safe to ignore)");
                } else {
                    System.out.println("     (Critical chunk)");
                }
        }
    }
    
    private void checkForSikuliXPatterns(File file) throws Exception {
        // Check filename pattern
        String filename = file.getName();
        System.out.println("   Filename: " + filename);
        
        // SikuliX often uses timestamp-based names
        if (filename.matches("\\d{13}\\.png")) {
            long timestamp = Long.parseLong(filename.substring(0, 13));
            System.out.println("   Timestamp-based name: " + new java.util.Date(timestamp));
        }
        
        // Check parent directory
        File parent = file.getParentFile();
        if (parent != null) {
            String parentName = parent.getName();
            System.out.println("   Parent directory: " + parentName);
            
            if (parentName.endsWith(".sikuli")) {
                System.out.println("   *** This is a SikuliX project image ***");
                
                // Check for .py or other script files
                File[] scriptFiles = parent.listFiles((dir, name) -> 
                    name.endsWith(".py") || name.endsWith(".rb") || name.endsWith(".js"));
                
                if (scriptFiles != null && scriptFiles.length > 0) {
                    System.out.println("   Associated scripts:");
                    for (File script : scriptFiles) {
                        System.out.println("     - " + script.getName());
                        
                        // Check if script references this image
                        checkScriptForImageReference(script, filename);
                    }
                }
            }
        }
        
        // Check file header for any custom markers
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] header = new byte[Math.min(1000, (int)file.length())];
            fis.read(header);
            
            String headerStr = new String(header);
            if (headerStr.contains("Pattern") || headerStr.contains("similarity")) {
                System.out.println("   *** Found pattern-related text in file ***");
            }
        }
    }
    
    private void checkScriptForImageReference(File script, String imageName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(script))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                if (line.contains(imageName) || line.contains(imageName.replace(".png", ""))) {
                    System.out.println("       Line " + lineNum + ": " + line.trim());
                    
                    // Check for similarity settings
                    if (line.contains("similar(") || line.contains("Pattern(")) {
                        System.out.println("       *** Pattern configuration found ***");
                    }
                }
            }
        } catch (IOException e) {
            // Ignore
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
    
    private String getImageTypeName(int type) {
        switch(type) {
            case BufferedImage.TYPE_INT_RGB: return "TYPE_INT_RGB";
            case BufferedImage.TYPE_INT_ARGB: return "TYPE_INT_ARGB";
            case BufferedImage.TYPE_INT_ARGB_PRE: return "TYPE_INT_ARGB_PRE";
            case BufferedImage.TYPE_INT_BGR: return "TYPE_INT_BGR";
            case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR";
            case BufferedImage.TYPE_4BYTE_ABGR: return "TYPE_4BYTE_ABGR";
            case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "TYPE_4BYTE_ABGR_PRE";
            default: return "Type " + type;
        }
    }
}