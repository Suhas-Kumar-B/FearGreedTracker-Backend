package com.aurelius.fear_greed_tracker.service;

import com.aurelius.fear_greed_tracker.api.dto.FearGreedApiResponse;
import com.aurelius.fear_greed_tracker.api.dto.FearGreedData;
import com.aurelius.fear_greed_tracker.api.dto.FearGreedHistoricalDataWrapper;
import com.aurelius.fear_greed_tracker.model.FearGreedIndex;
import com.aurelius.fear_greed_tracker.repository.FearGreedIndexRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service class responsible for fetching Fear & Greed Index data from the CNN API,
 * storing it in the database, and providing methods to retrieve this data for the UI.
 * Handles both daily scheduled fetches, a one-time historical data load,
 * and now includes data retention and monthly data retrieval.
 */
@Service
@Slf4j
public class FearGreedService {

    private final RestTemplate restTemplate;
    private final FearGreedIndexRepository fearGreedIndexRepository;

    @Value("${cnn.feargreed.api.url}")
    private String cnnApiBaseUrl;

    public FearGreedService(RestTemplate restTemplate, FearGreedIndexRepository fearGreedIndexRepository) {
        this.restTemplate = restTemplate;
        this.fearGreedIndexRepository = fearGreedIndexRepository;
    }

    private HttpHeaders createRequestHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Accept-Language", "en-US,en;q=0.9");
        headers.set("Referer", "https://edition.cnn.com/markets/fear-and-greed");
        return headers;
    }

    @Scheduled(cron = "0 0 1 * * *") // Runs daily at 1:00 AM
    public void fetchAndSaveDailyFearGreedIndex() {
        LocalDate today = LocalDate.now();
        log.info("Attempting to fetch Fear & Greed Index for date: {}", today);

        String apiUrl = cnnApiBaseUrl + "/graphdata/" + today.toString();
        log.debug("API URL: {}", apiUrl);

        try {
            Optional<FearGreedIndex> existingEntry = fearGreedIndexRepository.findByRecordDate(today);

            if (existingEntry.isPresent()) {
                log.info("Fear & Greed Index for {} already exists. Skipping save operation.", today);
                return;
            }

            HttpEntity<String> entity = new HttpEntity<>(createRequestHeaders());
            ResponseEntity<FearGreedApiResponse> responseEntity = restTemplate.exchange(
                    apiUrl, HttpMethod.GET, entity, FearGreedApiResponse.class
            );
            FearGreedApiResponse response = responseEntity.getBody();

            FearGreedData primaryData = null;
            if (response != null) {
                if (response.getData() != null) {
                    primaryData = response.getData();
                } else if (response.getFearGreed() != null) {
                    primaryData = response.getFearGreed();
                } else if (response.getMarketMomentumSp500() != null) {
                    primaryData = response.getMarketMomentumSp500();
                }
            }

            if (primaryData != null && primaryData.getScore() != null && primaryData.getRating() != null && primaryData.getTimestamp() != null) {
                FearGreedIndex fgi = new FearGreedIndex();
                fgi.setRecordDate(today);
                fgi.setFgiValue(primaryData.getScore());
                fgi.setSentiment(primaryData.getRating());
                OffsetDateTime apiTimestamp = OffsetDateTime.ofInstant(
                        Instant.ofEpochMilli(primaryData.getTimestamp()), ZoneOffset.UTC);
                fgi.setTimestamp(apiTimestamp);

                fearGreedIndexRepository.save(fgi);
                log.info("Successfully saved Fear & Greed Index for {}: Value={}, Sentiment={}",
                        today, fgi.getFgiValue(), fgi.getSentiment());
            } else {
                log.warn("Failed to retrieve Fear & Greed Index data for {}. Primary data (score/rating/timestamp) was not found or was incomplete in the API response for daily save.", today);
            }

        } catch (Exception e) {
            log.error("Error fetching or saving daily Fear & Greed Index for {}: {}", today, e.getMessage(), e);
        }
    }

    public int fetchAndSaveHistoricalFearGreedIndex() {
        log.info("Attempting to fetch ALL historical Fear & Greed Index data.");
        String apiUrl = cnnApiBaseUrl + "/graphdata";
        log.debug("Historical API URL: {}", apiUrl);

        int savedCount = 0;
        try {
            HttpEntity<String> entity = new HttpEntity<>(createRequestHeaders());
            ResponseEntity<FearGreedApiResponse> responseEntity = restTemplate.exchange(
                    apiUrl, HttpMethod.GET, entity, FearGreedApiResponse.class
            );
            FearGreedApiResponse response = responseEntity.getBody();

            if (response != null && response.getFearAndGreedHistorical() != null &&
                    response.getFearAndGreedHistorical().getData() != null &&
                    !response.getFearAndGreedHistorical().getData().isEmpty()) {

                List<FearGreedData> historicalPoints = response.getFearAndGreedHistorical().getData();
                log.info("Found {} historical data points to process.", historicalPoints.size());

                for (FearGreedData historicalPoint : historicalPoints) {
                    Long timestampX = historicalPoint.getX();
                    Double scoreY = historicalPoint.getY();
                    String sentimentRating = historicalPoint.getRating();

                    if (timestampX == null || scoreY == null || sentimentRating == null) {
                        log.warn("Skipping incomplete historical data point: {}", historicalPoint);
                        continue;
                    }

                    LocalDate recordDate = Instant.ofEpochMilli(timestampX).atZone(ZoneOffset.UTC).toLocalDate();
                    OffsetDateTime fullTimestamp = OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestampX), ZoneOffset.UTC);

                    if (!fearGreedIndexRepository.findByRecordDate(recordDate).isPresent()) {
                        FearGreedIndex fgi = new FearGreedIndex();
                        fgi.setRecordDate(recordDate);
                        fgi.setFgiValue(scoreY.intValue());
                        fgi.setSentiment(sentimentRating);
                        fgi.setTimestamp(fullTimestamp);

                        fearGreedIndexRepository.save(fgi);
                        savedCount++;
                        log.debug("Saved historical FGI for {}: Value={}, Sentiment={}", recordDate, scoreY.intValue(), sentimentRating);
                    } else {
                        log.debug("Historical FGI for {} already exists. Skipping save.", recordDate);
                    }
                }
                log.info("Successfully processed historical data. Saved {} new entries.", savedCount);
            } else {
                log.warn("Failed to retrieve historical Fear & Greed Index data. API response had no valid 'fear_and_greed_historical.data' or it was empty.");
            }
        } catch (Exception e) {
            log.error("Error fetching or saving historical Fear & Greed Index: {}", e.getMessage(), e);
        }
        return savedCount;
    }

    public List<FearGreedIndex> getLastNDaysFearGreedIndex(int days) {
        if (days <= 0) {
            return List.of();
        }
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        log.info("Retrieving Fear & Greed Index data from {} for the last {} days.", startDate, days);
        return fearGreedIndexRepository.findByRecordDateGreaterThanEqualOrderByRecordDateAsc(startDate);
    }

    // Original getTodaysFearGreedIndex() is now private and called by getOrCreateTodaysFearGreedIndex()
    private Optional<FearGreedIndex> getTodaysFearGreedIndex() {
        return fearGreedIndexRepository.findByRecordDate(LocalDate.now());
    }

    /**
     * Attempts to retrieve today's Fear & Greed Index from the database.
     * If not found, it triggers a fetch and save operation for today's data,
     * then attempts to retrieve it again.
     * This ensures that the frontend always gets the latest data, even if
     * the scheduled job hasn't run or the server was down.
     * @return An Optional containing today's FearGreedIndex entity if found/fetched, otherwise an empty Optional.
     */
    public Optional<FearGreedIndex> getOrCreateTodaysFearGreedIndex() {
        LocalDate today = LocalDate.now();
        log.info("Attempting to get or create Fear & Greed Index for today: {}", today);

        Optional<FearGreedIndex> todayIndex = getTodaysFearGreedIndex(); // Try to get from DB first

        if (todayIndex.isEmpty()) {
            log.info("Today's Fear & Greed Index not found in DB. Triggering on-demand fetch and save.");
            fetchAndSaveDailyFearGreedIndex(); // Call the existing fetch logic
            // After attempting to fetch, try to retrieve it again
            todayIndex = getTodaysFearGreedIndex();
            if (todayIndex.isEmpty()) {
                log.warn("Failed to retrieve today's Fear & Greed Index even after on-demand fetch.");
            } else {
                log.info("Successfully retrieved today's Fear & Greed Index after on-demand fetch.");
            }
        } else {
            log.info("Today's Fear & Greed Index found in DB.");
        }
        return todayIndex;
    }

    /**
     * Retrieves Fear & Greed Index data for a specific month and year.
     * @param year The year to retrieve data for.
     * @param month The month (1-12) to retrieve data for.
     * @return A list of FearGreedIndex entities for the specified month and year.
     */
    public List<FearGreedIndex> getFearGreedIndexByMonthAndYear(int year, int month) {
        log.info("Retrieving Fear & Greed Index data for year: {}, month: {}", year, month);
        return fearGreedIndexRepository.findByYearAndMonthOrderByRecordDateAsc(year, month);
    }

    /**
     * Scheduled task to delete Fear & Greed Index data older than 5 years.
     * Runs once a month, on the 1st day of the month at 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 1 * *") // Runs at 2:00 AM on the 1st day of every month
    @Transactional // Ensures the deletion operation is atomic
    public void deleteOldFearGreedIndexData() {
        // Keep data for the last 5 years
        LocalDate cutoffDate = LocalDate.now().minusYears(5);
        log.info("Attempting to delete Fear & Greed Index data older than: {}", cutoffDate);
        try {
            int deletedCount = fearGreedIndexRepository.deleteByRecordDateBefore(cutoffDate);
            log.info("Successfully deleted {} old Fear & Greed Index entries.", deletedCount);
        } catch (Exception e) {
            log.error("Error deleting old Fear & Greed Index data: {}", e.getMessage(), e);
        }
    }
}
