package com.heima.accounting.repository;

import com.heima.accounting.entity.Record;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {

    @Query("SELECT r FROM Record r WHERE " +
           "(:type IS NULL OR r.type = :type) AND " +
           "(:startDate IS NULL OR r.recordDate >= :startDate) AND " +
           "(:endDate IS NULL OR r.recordDate <= :endDate) AND " +
           "(:categoryId IS NULL OR r.categoryId = :categoryId) AND " +
           "(:parentCategoryId IS NULL OR r.parentCategoryId = :parentCategoryId) AND " +
           "(:minAmount IS NULL OR r.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR r.amount <= :maxAmount) AND " +
           "(:keyword IS NULL OR r.remark LIKE %:keyword%) " +
           "ORDER BY r.recordDate DESC, r.createTime DESC")
    Page<Record> findWithFilters(@Param("type") String type,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 @Param("categoryId") Long categoryId,
                                 @Param("parentCategoryId") Long parentCategoryId,
                                 @Param("minAmount") BigDecimal minAmount,
                                 @Param("maxAmount") BigDecimal maxAmount,
                                 @Param("keyword") String keyword,
                                 Pageable pageable);

    /** 按类型统计月度各一级分类金额 */
    @Query("SELECT r.parentCategoryId, r.parentCategoryName, SUM(r.amount) " +
           "FROM Record r WHERE r.type = :type AND r.recordDate >= :startDate AND r.recordDate <= :endDate " +
           "GROUP BY r.parentCategoryId, r.parentCategoryName ORDER BY SUM(r.amount) DESC")
    List<Object[]> sumByParentCategoryAndType(@Param("type") String type,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /** 按类型统计月度总金额 */
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM Record r " +
           "WHERE r.type = :type AND r.recordDate >= :startDate AND r.recordDate <= :endDate")
    BigDecimal sumTotalByMonthAndType(@Param("type") String type,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /** 统计每日支出或收入 */
    @Query("SELECT r.recordDate, SUM(r.amount) FROM Record r " +
           "WHERE r.type = :type AND r.recordDate >= :startDate AND r.recordDate <= :endDate " +
           "GROUP BY r.recordDate ORDER BY r.recordDate")
    List<Object[]> sumByDayAndType(@Param("type") String type,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);
}
