package com.heima.accounting.service;

import com.heima.accounting.dto.CategoryTreeNode;
import com.heima.accounting.entity.Category;
import com.heima.accounting.repository.CategoryRepository;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final String EXPENSE = "EXPENSE";
    private static final String INCOME = "INCOME";

    private final CategoryRepository categoryRepository;

    /**
     * 获取指定类型的分类树
     */
    public List<CategoryTreeNode> getAllAsTree(String type) {
        List<Category> level1List;
        if (type != null) {
            level1List = categoryRepository.findByLevelAndTypeOrderBySortOrder(1, type);
        } else {
            level1List = categoryRepository.findByLevelOrderBySortOrder(1);
        }

        List<CategoryTreeNode> tree = new ArrayList<>();
        for (Category level1 : level1List) {
            List<Category> children = categoryRepository.findByParentIdOrderBySortOrder(level1.getId());
            List<CategoryTreeNode> childNodes = children.stream()
                    .map(c -> CategoryTreeNode.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .sortOrder(c.getSortOrder())
                            .preset(c.getPreset())
                            .build())
                    .collect(Collectors.toList());

            tree.add(CategoryTreeNode.builder()
                    .id(level1.getId())
                    .name(level1.getName())
                    .sortOrder(level1.getSortOrder())
                    .preset(level1.getPreset())
                    .children(childNodes)
                    .build());
        }
        return tree;
    }

    @PostConstruct
    public void initializeCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }
        List<Category> categories = buildCategories();
        categoryRepository.saveAll(categories);
    }

    private List<Category> buildCategories() {
        List<Category> list = new ArrayList<>();
        long[] nextId = {1L};

        // ==================== 支出分类 ====================
        long canyinId = nextId[0]++;
        addCategory(list, 1, "餐饮", null, canyinId, 1, EXPENSE);
        addCategory(list, 2, "早餐", canyinId, nextId[0]++, 1, EXPENSE);
        addCategory(list, 2, "午餐", canyinId, nextId[0]++, 2, EXPENSE);
        addCategory(list, 2, "晚餐", canyinId, nextId[0]++, 3, EXPENSE);
        addCategory(list, 2, "零食水果", canyinId, nextId[0]++, 4, EXPENSE);
        addCategory(list, 2, "外卖", canyinId, nextId[0]++, 5, EXPENSE);
        addCategory(list, 2, "奶茶咖啡", canyinId, nextId[0]++, 6, EXPENSE);
        addCategory(list, 2, "聚餐请客", canyinId, nextId[0]++, 7, EXPENSE);

        long jiaotongId = nextId[0]++;
        addCategory(list, 1, "交通", null, jiaotongId, 2, EXPENSE);
        addCategory(list, 2, "公交地铁", jiaotongId, nextId[0]++, 1, EXPENSE);
        addCategory(list, 2, "打车", jiaotongId, nextId[0]++, 2, EXPENSE);
        addCategory(list, 2, "加油充电", jiaotongId, nextId[0]++, 3, EXPENSE);
        addCategory(list, 2, "停车费", jiaotongId, nextId[0]++, 4, EXPENSE);
        addCategory(list, 2, "火车高铁", jiaotongId, nextId[0]++, 5, EXPENSE);
        addCategory(list, 2, "飞机票", jiaotongId, nextId[0]++, 6, EXPENSE);
        addCategory(list, 2, "共享单车", jiaotongId, nextId[0]++, 7, EXPENSE);

        long gouwuId = nextId[0]++;
        addCategory(list, 1, "购物", null, gouwuId, 3, EXPENSE);
        addCategory(list, 2, "服饰鞋包", gouwuId, nextId[0]++, 1, EXPENSE);
        addCategory(list, 2, "数码产品", gouwuId, nextId[0]++, 2, EXPENSE);
        addCategory(list, 2, "家居用品", gouwuId, nextId[0]++, 3, EXPENSE);
        addCategory(list, 2, "化妆品护肤", gouwuId, nextId[0]++, 4, EXPENSE);
        addCategory(list, 2, "日用百货", gouwuId, nextId[0]++, 5, EXPENSE);
        addCategory(list, 2, "母婴用品", gouwuId, nextId[0]++, 6, EXPENSE);

        long zhufangId = nextId[0]++;
        addCategory(list, 1, "住房", null, zhufangId, 4, EXPENSE);
        addCategory(list, 2, "房租", zhufangId, nextId[0]++, 1, EXPENSE);
        addCategory(list, 2, "房贷", zhufangId, nextId[0]++, 2, EXPENSE);
        addCategory(list, 2, "水电燃气", zhufangId, nextId[0]++, 3, EXPENSE);
        addCategory(list, 2, "物业费", zhufangId, nextId[0]++, 4, EXPENSE);
        addCategory(list, 2, "维修保养", zhufangId, nextId[0]++, 5, EXPENSE);
        addCategory(list, 2, "宽带话费", zhufangId, nextId[0]++, 6, EXPENSE);

        long yuleId = nextId[0]++;
        addCategory(list, 1, "娱乐", null, yuleId, 5, EXPENSE);
        addCategory(list, 2, "游戏充值", yuleId, nextId[0]++, 1, EXPENSE);
        addCategory(list, 2, "电影演出", yuleId, nextId[0]++, 2, EXPENSE);
        addCategory(list, 2, "旅游出行", yuleId, nextId[0]++, 3, EXPENSE);
        addCategory(list, 2, "运动健身", yuleId, nextId[0]++, 4, EXPENSE);
        addCategory(list, 2, "KTV酒吧", yuleId, nextId[0]++, 5, EXPENSE);
        addCategory(list, 2, "视频会员", yuleId, nextId[0]++, 6, EXPENSE);

        long yiliaoId = nextId[0]++;
        addCategory(list, 1, "医疗", null, yiliaoId, 6, EXPENSE);
        addCategory(list, 2, "看病挂号", yiliaoId, nextId[0]++, 1, EXPENSE);
        addCategory(list, 2, "药品费", yiliaoId, nextId[0]++, 2, EXPENSE);
        addCategory(list, 2, "体检", yiliaoId, nextId[0]++, 3, EXPENSE);
        addCategory(list, 2, "牙科眼科", yiliaoId, nextId[0]++, 4, EXPENSE);
        addCategory(list, 2, "保健品", yiliaoId, nextId[0]++, 5, EXPENSE);

        long jiaoyuId = nextId[0]++;
        addCategory(list, 1, "教育", null, jiaoyuId, 7, EXPENSE);
        addCategory(list, 2, "书籍资料", jiaoyuId, nextId[0]++, 1, EXPENSE);
        addCategory(list, 2, "培训课程", jiaoyuId, nextId[0]++, 2, EXPENSE);
        addCategory(list, 2, "考试报名", jiaoyuId, nextId[0]++, 3, EXPENSE);
        addCategory(list, 2, "文具用品", jiaoyuId, nextId[0]++, 4, EXPENSE);

        long renqingId = nextId[0]++;
        addCategory(list, 1, "人情", null, renqingId, 8, EXPENSE);
        addCategory(list, 2, "红包礼金", renqingId, nextId[0]++, 1, EXPENSE);
        addCategory(list, 2, "请客送礼", renqingId, nextId[0]++, 2, EXPENSE);
        addCategory(list, 2, "慈善捐款", renqingId, nextId[0]++, 3, EXPENSE);
        addCategory(list, 2, "份子钱", renqingId, nextId[0]++, 4, EXPENSE);

        long jinrongId = nextId[0]++;
        addCategory(list, 1, "金融", null, jinrongId, 9, EXPENSE);
        addCategory(list, 2, "银行手续费", jinrongId, nextId[0]++, 1, EXPENSE);
        addCategory(list, 2, "贷款利息", jinrongId, nextId[0]++, 2, EXPENSE);
        addCategory(list, 2, "保险费", jinrongId, nextId[0]++, 3, EXPENSE);
        addCategory(list, 2, "投资亏损", jinrongId, nextId[0]++, 4, EXPENSE);

        long qitaExpenseId = nextId[0]++;
        addCategory(list, 1, "其他", null, qitaExpenseId, 10, EXPENSE);
        addCategory(list, 2, "快递费", qitaExpenseId, nextId[0]++, 1, EXPENSE);
        addCategory(list, 2, "宠物花销", qitaExpenseId, nextId[0]++, 2, EXPENSE);
        addCategory(list, 2, "美发美甲", qitaExpenseId, nextId[0]++, 3, EXPENSE);
        addCategory(list, 2, "其他杂项", qitaExpenseId, nextId[0]++, 4, EXPENSE);

        // ==================== 收入分类 ====================
        long gongziId = nextId[0]++;
        addCategory(list, 1, "工资收入", null, gongziId, 1, INCOME);
        addCategory(list, 2, "基本工资", gongziId, nextId[0]++, 1, INCOME);
        addCategory(list, 2, "加班费", gongziId, nextId[0]++, 2, INCOME);
        addCategory(list, 2, "奖金提成", gongziId, nextId[0]++, 3, INCOME);
        addCategory(list, 2, "补贴津贴", gongziId, nextId[0]++, 4, INCOME);
        addCategory(list, 2, "年终奖", gongziId, nextId[0]++, 5, INCOME);

        long jianzhiId = nextId[0]++;
        addCategory(list, 1, "兼职收入", null, jianzhiId, 2, INCOME);
        addCategory(list, 2, "自由职业", jianzhiId, nextId[0]++, 1, INCOME);
        addCategory(list, 2, "咨询顾问", jianzhiId, nextId[0]++, 2, INCOME);
        addCategory(list, 2, "外包项目", jianzhiId, nextId[0]++, 3, INCOME);
        addCategory(list, 2, "副业收入", jianzhiId, nextId[0]++, 4, INCOME);
        addCategory(list, 2, "零工散活", jianzhiId, nextId[0]++, 5, INCOME);

        long touziId = nextId[0]++;
        addCategory(list, 1, "投资收益", null, touziId, 3, INCOME);
        addCategory(list, 2, "股票收益", touziId, nextId[0]++, 1, INCOME);
        addCategory(list, 2, "基金收益", touziId, nextId[0]++, 2, INCOME);
        addCategory(list, 2, "理财收益", touziId, nextId[0]++, 3, INCOME);
        addCategory(list, 2, "房租收入", touziId, nextId[0]++, 4, INCOME);
        addCategory(list, 2, "分红派息", touziId, nextId[0]++, 5, INCOME);
        addCategory(list, 2, "利息收入", touziId, nextId[0]++, 6, INCOME);

        long hongbaoId = nextId[0]++;
        addCategory(list, 1, "红包礼金", null, hongbaoId, 4, INCOME);
        addCategory(list, 2, "微信红包", hongbaoId, nextId[0]++, 1, INCOME);
        addCategory(list, 2, "节日礼金", hongbaoId, nextId[0]++, 2, INCOME);
        addCategory(list, 2, "生日红包", hongbaoId, nextId[0]++, 3, INCOME);
        addCategory(list, 2, "份子钱收入", hongbaoId, nextId[0]++, 4, INCOME);

        long baoxiaoId = nextId[0]++;
        addCategory(list, 1, "报销退款", null, baoxiaoId, 5, INCOME);
        addCategory(list, 2, "差旅报销", baoxiaoId, nextId[0]++, 1, INCOME);
        addCategory(list, 2, "办公报销", baoxiaoId, nextId[0]++, 2, INCOME);
        addCategory(list, 2, "购物退款", baoxiaoId, nextId[0]++, 3, INCOME);
        addCategory(list, 2, "押金退还", baoxiaoId, nextId[0]++, 4, INCOME);

        long jiedaiId = nextId[0]++;
        addCategory(list, 1, "借贷回收", null, jiedaiId, 6, INCOME);
        addCategory(list, 2, "借出款回收", jiedaiId, nextId[0]++, 1, INCOME);
        addCategory(list, 2, "欠款收回", jiedaiId, nextId[0]++, 2, INCOME);

        long qitaIncomeId = nextId[0]++;
        addCategory(list, 1, "其他收入", null, qitaIncomeId, 7, INCOME);
        addCategory(list, 2, "二手卖出", qitaIncomeId, nextId[0]++, 1, INCOME);
        addCategory(list, 2, "闲置转让", qitaIncomeId, nextId[0]++, 2, INCOME);
        addCategory(list, 2, "其他来源", qitaIncomeId, nextId[0]++, 3, INCOME);

        return list;
    }

    private void addCategory(List<Category> list, int level, String name, Long parentId, long id, int sort, String type) {
        list.add(Category.builder()
                .id(id)
                .name(name)
                .parentId(parentId)
                .level(level)
                .sortOrder(sort)
                .type(type)
                .preset(true)
                .build());
    }

    /** 创建自定义分类（只能是二级分类，挂在一级分类下） */
    public Category create(String name, Long parentId) {
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("父分类不存在"));
        if (parent.getLevel() != 1) {
            throw new IllegalArgumentException("只能在一级分类下创建二级分类");
        }

        Category category = Category.builder()
                .name(name)
                .parentId(parentId)
                .level(2)
                .type(parent.getType())
                .sortOrder(999)
                .preset(false)
                .build();
        return categoryRepository.save(category);
    }

    /** 修改分类名称（只能修改非预置分类） */
    public Category updateName(Long id, String name) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));
        if (Boolean.TRUE.equals(category.getPreset())) {
            throw new IllegalArgumentException("预置分类不可修改");
        }
        category.setName(name);
        return categoryRepository.save(category);
    }

    /** 删除分类（只能删除非预置分类，且分类下不能有子分类） */
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));
        if (Boolean.TRUE.equals(category.getPreset())) {
            throw new IllegalArgumentException("预置分类不可删除");
        }
        List<Category> children = categoryRepository.findByParentIdOrderBySortOrder(id);
        if (!children.isEmpty()) {
            throw new IllegalArgumentException("请先删除该分类下的子分类");
        }
        categoryRepository.deleteById(id);
    }
}
