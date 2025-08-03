package com.aurelius.fear_greed_tracker.controller;

import com.aurelius.fear_greed_tracker.model.FearGreedIndex;
import com.aurelius.fear_greed_tracker.service.FearGreedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller responsible for exposing Fear & Greed Index data via HTTP endpoints.
 * These endpoints will be consumed by the frontend to display current and historical data.
 * Includes temporary endpoints for manual fetching during development.
 */
@RestController
@RequestMapping("/api/fear-greed")
public class FearGreedApiController {

    private final FearGreedService fearGreedService;

    public FearGreedApiController(FearGreedService fearGreedService) {
        this.fearGreedService = fearGreedService;
    }

    @GetMapping("/today")
    public ResponseEntity<FearGreedIndex> getTodaysIndex() {
        Optional<FearGreedIndex> todayIndex = fearGreedService.getOrCreateTodaysFearGreedIndex();
        return todayIndex.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    public List<FearGreedIndex> getHistoricalIndex(@RequestParam(defaultValue = "7") int days) {
        return fearGreedService.getLastNDaysFearGreedIndex(days);
    }

    /**
     * HTTP GET endpoint to retrieve Fear & Greed Index data for a specific month and year.
     * Accessible at: GET http://localhost:8080/api/fear-greed/history-by-month?year=2023&month=10
     * @param year The year to retrieve data for.
     * @param month The month (1-12) to retrieve data for.
     * @return A List of FearGreedIndex entities for the specified month and year.
     */
    @GetMapping("/history-by-month")
    public List<FearGreedIndex> getHistoricalIndexByMonth(
            @RequestParam int year,
            @RequestParam int month) {
        return fearGreedService.getFearGreedIndexByMonthAndYear(year, month);
    }

    @GetMapping("/fetch-now")
    public ResponseEntity<String> fetchNow() {
        try {
            fearGreedService.fetchAndSaveDailyFearGreedIndex();
            return ResponseEntity.ok("Daily Fear & Greed Index fetch triggered successfully. Check logs for details.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to trigger daily fetch: " + e.getMessage());
        }
    }

    @GetMapping("/fetch-history-now")
    public ResponseEntity<String> fetchHistoryNow() {
        try {
            int savedCount = fearGreedService.fetchAndSaveHistoricalFearGreedIndex();
            return ResponseEntity.ok("Historical Fear & Greed Index fetch triggered successfully. Saved " + savedCount + " new entries. Check logs for details.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to trigger historical fetch: " + e.getMessage());
        }
    }

    /**
     * TEMPORARY ENDPOINT FOR MANUAL TESTING (DEVELOPMENT ONLY):
     * Triggers the deletion of old data immediately.
     * This endpoint should be REMOVED in a production environment as scheduling handles this.
     * Accessible at: GET http://localhost:8080/api/fear-greed/cleanup-old-data
     */
    @GetMapping("/cleanup-old-data")
    public ResponseEntity<String> cleanupOldData() {
        try {
            fearGreedService.deleteOldFearGreedIndexData();
            return ResponseEntity.ok("Old Fear & Greed Index data cleanup triggered successfully. Check logs for details.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to trigger data cleanup: " + e.getMessage());
        }
    }
}
