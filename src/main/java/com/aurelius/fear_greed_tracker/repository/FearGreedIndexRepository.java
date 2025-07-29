package com.aurelius.fear_greed_tracker.repository;

import com.aurelius.fear_greed_tracker.model.FearGreedIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // Import for @Modifying
import org.springframework.data.jpa.repository.Query;    // Import for @Query
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // Import for @Transactional

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for FearGreedIndex entities.
 * Provides standard CRUD (Create, Read, Update, Delete) operations out-of-the-box
 * due to extending JpaRepository.
 * Also defines custom query methods based on Spring Data JPA's method naming conventions.
 */
@Repository
public interface FearGreedIndexRepository extends JpaRepository<FearGreedIndex, Long> {
    Optional<FearGreedIndex> findByRecordDate(LocalDate recordDate);
    List<FearGreedIndex> findByRecordDateGreaterThanEqualOrderByRecordDateAsc(LocalDate date);

    /**
     * Custom query method to find FearGreedIndex entries for a specific month and year.
     * @param year The year to search for.
     * @param month The month (1-12) to search for.
     * @return A list of FearGreedIndex entities for the specified month and year, ordered by record date.
     */
    @Query("SELECT f FROM FearGreedIndex f WHERE YEAR(f.recordDate) = :year AND MONTH(f.recordDate) = :month ORDER BY f.recordDate ASC")
    List<FearGreedIndex> findByYearAndMonthOrderByRecordDateAsc(int year, int month);

    /**
     * Custom query method to delete FearGreedIndex entries older than a specified date.
     * @param date The cutoff date; records with recordDate older than this will be deleted.
     * @return The number of records deleted.
     */
    @Modifying // Indicates that this query modifies the database
    @Transactional // Ensures the operation runs within a transaction
    @Query("DELETE FROM FearGreedIndex f WHERE f.recordDate < :date")
    int deleteByRecordDateBefore(LocalDate date);
}
