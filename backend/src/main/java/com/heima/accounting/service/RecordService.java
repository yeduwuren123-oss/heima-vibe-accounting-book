package com.heima.accounting.service;

import com.heima.accounting.dto.MonthlyStatsResponse;
import com.heima.accounting.dto.RecordRequest;
import com.heima.accounting.entity.Category;
import com.heima.accounting.entity.Record;
import com.heima.accounting.repository.CategoryRepository;
import com.heima.accounting.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Record create(RecordRequest request) {
        Category subCategory = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));

        if (subCategory.getLevel() != 2) {
            throw new IllegalArgumentException("请选择二级分类");
        }

        if (!request.getType().equals(subCategory.getType())) {
            throw new IllegalArgumentException("分类类型与收支类型不匹配");
        }

        Category parentCategory = categoryRepository.findById(subCategory.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("一级分类不存在"));

        Record record = Record.builder()
                .type(request.getType())
                .amount(request.getAmount())
                .categoryId(subCategory.getId())
                .categoryName(subCategory.getName())
                .parentCategoryId(parentCategory.getId())
                .parentCategoryName(parentCategory.getName())
                .recordDate(request.getRecordDate())
                .remark(request.getRemark())
                .build();

        return recordRepository.save(record);
    }

    @Transactional
    public Record update(Long id, RecordRequest request) {
        Record record = recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("记录不存在"));

        Category subCategory = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));

        if (subCategory.getLevel() != 2) {
            throw new IllegalArgumentException("请选择二级分类");
        }

        if (!request.getType().equals(subCategory.getType())) {
            throw new IllegalArgumentException("分类类型与收支类型不匹配");
        }

        Category parentCategory = categoryRepository.findById(subCategory.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("一级分类不存在"));

        record.setType(request.getType());
        record.setAmount(request.getAmount());
        record.setCategoryId(subCategory.getId());
        record.setCategoryName(subCategory.getName());
        record.setParentCategoryId(parentCategory.getId());
        record.setParentCategoryName(parentCategory.getName());
        record.setRecordDate(request.getRecordDate());
        record.setRemark(request.getRemark());

        return recordRepository.save(record);
    }

    @Transactional
    public void delete(Long id) {
        if (!recordRepository.existsById(id)) {
            throw new IllegalArgumentException("记录不存在");
        }
        recordRepository.deleteById(id);
    }

    public Record findById(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("记录不存在"));
    }

    public Page<Record> findWithFilters(String type, LocalDate startDate, LocalDate endDate,
                                         Long categoryId, Long parentCategoryId,
                                         BigDecimal minAmount, BigDecimal maxAmount,
                                         String keyword, int page, int size) {
        return recordRepository.findWithFilters(
                type, startDate, endDate, categoryId, parentCategoryId,
                minAmount, maxAmount, keyword,
                PageRequest.of(page, size));
    }

    /** 获取月度收支统计（收入+支出） */
    public MonthlyStatsResponse getMonthlyStats(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        BigDecimal expenseTotal = recordRepository.sumTotalByMonthAndType("EXPENSE", startDate, endDate);
        BigDecimal incomeTotal = recordRepository.sumTotalByMonthAndType("INCOME", startDate, endDate);
        if (expenseTotal == null) expenseTotal = BigDecimal.ZERO;
        if (incomeTotal == null) incomeTotal = BigDecimal.ZERO;

        List<MonthlyStatsResponse.CategoryStat> expenseStats =
                buildCategoryStats(recordRepository.sumByParentCategoryAndType("EXPENSE", startDate, endDate), expenseTotal);
        List<MonthlyStatsResponse.CategoryStat> incomeStats =
                buildCategoryStats(recordRepository.sumByParentCategoryAndType("INCOME", startDate, endDate), incomeTotal);

        return MonthlyStatsResponse.builder()
                .incomeTotal(incomeTotal)
                .expenseTotal(expenseTotal)
                .balance(incomeTotal.subtract(expenseTotal))
                .expenseStats(expenseStats)
                .incomeStats(incomeStats)
                .build();
    }

    private List<MonthlyStatsResponse.CategoryStat> buildCategoryStats(List<Object[]> rows, BigDecimal total) {
        List<MonthlyStatsResponse.CategoryStat> stats = new ArrayList<>();
        for (Object[] row : rows) {
            Long catId = (Long) row[0];
            String catName = (String) row[1];
            BigDecimal amount = (BigDecimal) row[2];
            double pct = total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.multiply(new BigDecimal("100")).divide(total, 1, RoundingMode.HALF_UP).doubleValue()
                    : 0;

            stats.add(MonthlyStatsResponse.CategoryStat.builder()
                    .categoryId(catId)
                    .categoryName(catName)
                    .amount(amount)
                    .percentage(pct)
                    .build());
        }
        return stats;
    }

    /** 获取每日收支统计 */
    public Map<String, Map<String, BigDecimal>> getDailyStats(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        List<Object[]> expenseRows = recordRepository.sumByDayAndType("EXPENSE", startDate, endDate);
        List<Object[]> incomeRows = recordRepository.sumByDayAndType("INCOME", startDate, endDate);

        Map<String, Map<String, BigDecimal>> result = new LinkedHashMap<>();

        // 初始化当月所有日期
        LocalDate d = startDate;
        while (!d.isAfter(endDate)) {
            Map<String, BigDecimal> dayMap = new LinkedHashMap<>();
            dayMap.put("expense", BigDecimal.ZERO);
            dayMap.put("income", BigDecimal.ZERO);
            result.put(d.toString(), dayMap);
            d = d.plusDays(1);
        }

        for (Object[] row : expenseRows) {
            LocalDate date = (LocalDate) row[0];
            result.get(date.toString()).put("expense", (BigDecimal) row[1]);
        }
        for (Object[] row : incomeRows) {
            LocalDate date = (LocalDate) row[0];
            result.get(date.toString()).put("income", (BigDecimal) row[1]);
        }

        return result;
    }
}
