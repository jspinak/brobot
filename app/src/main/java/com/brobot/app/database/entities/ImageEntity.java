package com.brobot.app.database.entities;

import jakarta.persistence.*;

/**
 * The Image Data Transfer Object converts an Image to a byte array when storing in a database,
 * and back to a BufferedImage after retrieving from the database.
 */
@Entity
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    @Lob
    private byte[] bytes; // BGR image as a byte array for persistence

}
