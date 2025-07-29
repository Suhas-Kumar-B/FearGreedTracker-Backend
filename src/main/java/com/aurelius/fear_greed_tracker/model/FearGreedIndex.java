package com.aurelius.fear_greed_tracker.model;

import jakarta.persistence.*;       // JPA annotations for entity mapping
import lombok.Data;                 // Lombok: Generates getters, setters, equals, hashCode, toString
import lombok.NoArgsConstructor;    // Lombok: Generates a no-argument constructor
import lombok.AllArgsConstructor;   // Lombok: Generates a constructor with all arguments

import java.time.LocalDate;         // For storing date without time
import java.time.OffsetDateTime;    // For storing timestamp with time zone information

/**
 * Represents a single record of the Fear & Greed Index in the database.
 * This is a JPA Entity, mapped to the 'fear_greed_index' table.
 */
@Entity // Marks this class as a JPA entity
@Table(name = "fear_greed_index") // Specifies the database table name
@Data // Automatically generates boilerplate methods like getters, setters, etc.
@NoArgsConstructor // Required by JPA for entity instantiation
@AllArgsConstructor // Convenient constructor for creating instances
public class FearGreedIndex {

    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Configures ID to be auto-incremented by the database
    private Long id;

    @Column(name = "record_date", unique = true, nullable = false)
    private LocalDate recordDate; // The specific date for this FGI record (unique per day)

    @Column(name = "fgi_value", nullable = false)
    private Integer fgiValue; // The numerical Fear & Greed Index score (0-100)

    @Column(name = "sentiment", nullable = false, length = 50)
    private String sentiment; // The sentiment description (e.g., "Extreme Fear", "Greed")

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp; // The exact timestamp provided by the CNN API for this data point

    @Column(name = "created_at", nullable = false, updatable = false) // Timestamp when this record was first created in our DB
    private OffsetDateTime createdAt;

    @Column(name = "updated_at") // Timestamp when this record was last updated in our DB
    private OffsetDateTime updatedAt;

    /**
     * JPA lifecycle callback method. Sets the 'createdAt' timestamp automatically before
     * a new entity is persisted to the database.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }

    /**
     * JPA lifecycle callback method. Sets the 'updatedAt' timestamp automatically before
     * an existing entity is updated in the database.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
