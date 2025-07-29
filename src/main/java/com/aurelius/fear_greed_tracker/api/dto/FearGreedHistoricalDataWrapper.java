package com.aurelius.fear_greed_tracker.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // For mapping JSON field names
import lombok.Data;          // Lombok: Generates boilerplate methods
import lombok.NoArgsConstructor; // Lombok: Generates a no-argument constructor
import lombok.AllArgsConstructor; // Lombok: Generates a constructor with all arguments

import java.util.List; // For the list of historical data points

/**
 * Data Transfer Object (DTO) to wrap the historical data array.
 * This DTO represents the object found under the "fear_and_greed_historical" key in the API response.
 * It contains a 'data' field which is the actual list of historical Fear & Greed data points.
 */
@Data // Automatically generates getters, setters, etc.
@NoArgsConstructor // Required by Jackson for deserialization
@AllArgsConstructor // Convenient constructor
public class FearGreedHistoricalDataWrapper {
    // These fields might exist at the wrapper level for summary, but the 'data' list is key
    private Long timestamp;
    private Double score;
    private String rating;

    @JsonProperty("data") // This JSON key contains the actual list of historical points
    private List<FearGreedData> data; // List of historical data points (each contains x, y, rating)
}
