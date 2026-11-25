package com.auca.lostandfound.repository;

import com.auca.lostandfound.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Full-text keyword search across name, description, and location.
     * Case-insensitive via LOWER().
     */
    @Query("SELECT i FROM Item i WHERE " +
           "LOWER(i.name)        LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.location)    LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Item> searchByKeyword(@Param("keyword") String keyword);

    /** Filter by report type only (e.g. LOST or FOUND). */
    List<Item> findByReportType(String reportType);

    /** Filter by category only. */
    List<Item> findByCategory(String category);

    /** Filter by both report type and category. */
    List<Item> findByReportTypeAndCategory(String reportType, String category);
}
