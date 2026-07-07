package com.heima.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatsResponse {

    /** 当月总收入 */
    private BigDecimal incomeTotal;

    /** 当月总支出 */
    private BigDecimal expenseTotal;

    /** 当月结余 */
    private BigDecimal balance;

    /** 支出分类明细 */
    private List<CategoryStat> expenseStats;

    /** 收入分类明细 */
    private List<CategoryStat> incomeStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStat {
        private Long categoryId;
        private String categoryName;
        private BigDecimal amount;
        private Double percentage;
    }
}
