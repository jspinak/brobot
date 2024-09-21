package io.github.jspinak.brobot.app.database.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;

/**
 * The Image Data Transfer Object converts an Image to a byte array when storing in a database,
 * and back to a BufferedImage after retrieving from the database.
 */
@Entity
@Table(name = "images")
@Data
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob
    @Column(name = "image_data")
    @JdbcTypeCode(java.sql.Types.BINARY)
    private byte[] imageData; // BGR image as a byte array for persistence
}