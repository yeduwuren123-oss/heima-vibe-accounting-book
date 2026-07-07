package com.heima.accounting.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "record")
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 收支类型：EXPENSE=支出, INCOME=收入 */
    @Column(nullable = false, length = 10)
    private String type;

    /** 金额（人民币，元） */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** 二级分类ID */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /** 二级分类名称 */
    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;

    /** 一级分类ID */
    @Column(name = "parent_category_id", nullable = false)
    private Long parentCategoryId;

    /** 一级分类名称 */
    @Column(name = "parent_category_name", nullable = false, length = 50)
    private String parentCategoryName;

    /** 记账日期 */
    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    /** 备注 */
    @Column(length = 200)
    private String remark;

    /** 创建时间 */
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    /** 更新时间 */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
