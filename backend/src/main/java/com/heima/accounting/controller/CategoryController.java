package com.heima.accounting.controller;

import com.heima.accounting.dto.CategoryRequest;
import com.heima.accounting.dto.CategoryTreeNode;
import com.heima.accounting.entity.Category;
import com.heima.accounting.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryTreeNode> getAll(@RequestParam(required = false) String type) {
        return categoryService.getAllAsTree(type);
    }

    /** 创建自定义分类 */
    @PostMapping
    public Category create(@Valid @RequestBody CategoryRequest request) {
        if (request.getParentId() == null) {
            throw new IllegalArgumentException("请选择父分类");
        }
        return categoryService.create(request.getName(), request.getParentId());
    }

    /** 修改分类名称 */
    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return categoryService.updateName(id, request.getName());
    }

    /** 删除自定义分类 */
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        categoryService.delete(id);
        return "ok";
    }
}
