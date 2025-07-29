package com.aurelius.fear_greed_tracker.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // For mapping JSON field names
import lombok.Data;          // Lombok: Generates boilerplate methods
import lombok.NoArgsConstructor; // Lombok: Generates a no-argument constructor
import lombok.AllArgsConstructor; // Lombok: Generates a constructor with all arguments

/**
 * Data Transfer Object (DTO) for individual Fear & Greed data points.
 * This DTO is versatile, used for:
 * - Current day's score/rating (from top-level 'data' or 'fear_greed' objects).
 * - Historical data points (from the array within 'fear_and_greed_historical').
 */
@Data // Automatically generates getters, setters, etc.
@NoArgsConstructor // Required by Jackson for deserialization
@AllArgsConstructor // Convenient constructor
public class FearGreedData {
    // Fields common for current day's data (e.g., in 'data' or 'fear_greed' objects)
    private Integer score;
    private String rating;
    private Long timestamp;

    // Fields for previous values (often nested under current day's data)
    @JsonProperty("previous_close")
    private Integer previousClose;

    @JsonProperty("previous_week")
    private Integer previousWeek;

    @JsonProperty("previous_month")
    private Integer previousMonth;

    @JsonProperty("previous_year")
    private Integer previousYear;

    // Fields specifically used for historical data points (from array within 'fear_and_greed_historical')
    private Long x; // Historical timestamp (JSON 'x' is a Long)
    private Double y; // Historical score (JSON 'y' is a Double for precision)
    private String z; // Historical sentiment (JSON 'rating' is used directly, 'z' remains for flexibility)
}
