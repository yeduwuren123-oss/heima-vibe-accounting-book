package com.heima.accounting.repository;

import com.heima.accounting.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** 查询所有一级分类，按排序号排列 */
    List<Category> findByLevelOrderBySortOrder(Integer level);

    /** 查询某个一级分类下的所有二级分类 */
    List<Category> findByParentIdOrderBySortOrder(Long parentId);

    /** 按类型查询一级分类 */
    List<Category> findByLevelAndTypeOrderBySortOrder(Integer level, String type);
}
