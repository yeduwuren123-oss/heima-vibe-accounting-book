package com.heima.accounting.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    /** 创建时必传（父分类ID），修改时可留空 */
    private Long parentId;
}
