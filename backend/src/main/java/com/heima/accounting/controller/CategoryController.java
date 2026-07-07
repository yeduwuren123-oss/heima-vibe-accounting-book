package com.heima.accounting.controller;

import com.heima.accounting.dto.CategoryTreeNode;
import com.heima.accounting.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 获取分类树，可按收支类型筛选
     * @param type EXPENSE=支出, INCOME=收入，不传则返回全部
     */
    @GetMapping
    public List<CategoryTreeNode> getAll(@RequestParam(required = false) String type) {
        return categoryService.getAllAsTree(type);
    }
}
