package com.heima.accounting.controller;

import com.heima.accounting.dto.MonthlyStatsResponse;
import com.heima.accounting.dto.RecordRequest;
import com.heima.accounting.entity.Record;
import com.heima.accounting.service.RecordService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;

    /**
     * 新增记录
     */
    @PostMapping
    public Record create(@Valid @RequestBody RecordRequest request) {
        return recordService.create(request);
    }

    /**
     * 更新记录
     */
    @PutMapping("/{id}")
    public Record update(@PathVariable Long id, @Valid @RequestBody RecordRequest request) {
        return recordService.update(id, request);
    }

    /**
     * 删除记录
     */
    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        recordService.delete(id);
        return Map.of("message", "删除成功");
    }

    /**
     * 查询单条记录
     */
    @GetMapping("/{id}")
    public Record findById(@PathVariable Long id) {
        return recordService.findById(id);
    }

    /**
     * 多条件分页查询记录列表
     */
    @GetMapping
    public Page<Record> list(@RequestParam(required = false) String type,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                             @RequestParam(required = false) Long categoryId,
                             @RequestParam(required = false) Long parentCategoryId,
                             @RequestParam(required = false) BigDecimal minAmount,
                             @RequestParam(required = false) BigDecimal maxAmount,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size) {
        return recordService.findWithFilters(
                type, startDate, endDate, categoryId, parentCategoryId,
                minAmount, maxAmount, keyword, page, size);
    }

    /**
     * 月度统计报表
     */
    @GetMapping("/stats/monthly")
    public MonthlyStatsResponse monthlyStats(@RequestParam int year, @RequestParam int month) {
        return recordService.getMonthlyStats(year, month);
    }

    /**
     * 每日收支统计
     */
    @GetMapping("/stats/daily")
    public Map<String, Map<String, BigDecimal>> dailyStats(@RequestParam int year, @RequestParam int month) {
        return recordService.getDailyStats(year, month);
    }
}
