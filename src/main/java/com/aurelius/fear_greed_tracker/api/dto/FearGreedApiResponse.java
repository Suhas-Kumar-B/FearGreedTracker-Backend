package com.aurelius.fear_greed_tracker.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // For mapping JSON field names to Java field names
import lombok.Data;          // Lombok: Generates getters, setters, equals, hashCode, toString
import lombok.NoArgsConstructor; // Lombok: Generates a no-argument constructor
import lombok.AllArgsConstructor; // Lombok: Generates a constructor with all arguments

/**
 * Data Transfer Object (DTO) representing the top-level structure of the JSON response
 * received from the CNN Fear & Greed Index unofficial API (both daily and full historical endpoints).
 * It includes mappings for various top-level objects in the API response.
 */
@Data // Automatically generates getters, setters, etc.
@NoArgsConstructor // Required by Jackson for deserialization
@AllArgsConstructor // Convenient constructor
public class FearGreedApiResponse {
    // This field maps to the 'data' object in the JSON (often for current day's summary)
    private FearGreedData data;

    // This field maps to the 'fear_greed' object in the JSON (can also contain current/summary data)
    @JsonProperty("fear_greed")
    private FearGreedData fearGreed;

    // This field maps to the 'market_momentum_sp500' object in the JSON
    @JsonProperty("market_momentum_sp500")
    private FearGreedData marketMomentumSp500;

    // This field maps to the 'fear_and_greed_historical' object in the JSON.
    // It's crucial for the full historical data endpoint, as it contains the array of historical points.
    @JsonProperty("fear_and_greed_historical")
    private FearGreedHistoricalDataWrapper fearAndGreedHistorical; // Uses the new wrapper DTO
}
