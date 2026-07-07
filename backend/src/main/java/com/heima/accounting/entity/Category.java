package com.heima.accounting.entity;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 分类名称 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 父分类ID，一级分类为null */
    @Column(name = "parent_id")
    private Long parentId;

    /** 分类层级：1=一级分类, 2=二级分类 */
    @Column(nullable = false)
    private Integer level;

    /** 收支类型：EXPENSE=支出, INCOME=收入 */
    @Column(nullable = false, length = 10)
    private String type;

    /** 排序号 */
    @Column(name = "sort_order")
    private Integer sortOrder;
}
