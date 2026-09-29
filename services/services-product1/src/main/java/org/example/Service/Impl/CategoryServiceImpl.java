package org.example.Service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.example.Mapper.CategoryMapper;
import org.example.Product.Category;
import org.example.Service.CategoryService;
import org.example.cache.CacheKeys;
import org.example.cache.RedisCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类服务实现
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisCacheService cacheService;

    /** 分类树缓存基准 TTL：60 分钟（分类极少变化） */
    private static final Duration CATEGORY_TTL = Duration.ofMinutes(60);
    /** TTL 随机抖动上界：0~10 分钟，防雪崩 */
    private static final long CATEGORY_TTL_JITTER_SECONDS = 600;

    @Override
    public List<Category> listEnabled() {
        return categoryMapper.findAllEnabled();
    }

    @Override
    public List<Category> listAll() {
        return categoryMapper.findAll();
    }

    /**
     * 组装分类树：读多写少，整树缓存 + 随机 TTL（防雪崩）；变更时删除缓存。
     */
    @Override
    public List<Category> tree() {
        return cacheService.queryWithProtect(
                CacheKeys.CATEGORY_TREE,
                new TypeReference<List<Category>>() {
                },
                CATEGORY_TTL,
                CATEGORY_TTL_JITTER_SECONDS,
                this::buildTree);
    }

    /**
     * 从平铺列表组装分类树
     */
    private List<Category> buildTree() {
        List<Category> all = categoryMapper.findAllEnabled();
        List<Category> roots = new ArrayList<>();
        for (Category root : all) {
            if (root.getParentId() == null || root.getParentId() == 0L) {
                root.setChildren(findChildren(root.getId(), all));
                roots.add(root);
            }
        }
        return roots;
    }

    /**
     * 在平铺列表中递归查找子分类
     */
    private List<Category> findChildren(Long parentId, List<Category> all) {
        List<Category> children = new ArrayList<>();
        for (Category c : all) {
            if (parentId.equals(c.getParentId())) {
                c.setChildren(findChildren(c.getId(), all));
                children.add(c);
            }
        }
        return children;
    }

    @Override
    public List<Category> listChildren(Long parentId) {
        return categoryMapper.findByParentId(parentId);
    }

    @Override
    public Category getById(Long id) {
        return categoryMapper.findById(id);
    }

    @Override
    public int add(Category category) {
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSort() == null) {
            category.setSort(0);
        }
        int rows = categoryMapper.insert(category);
        if (rows > 0) {
            cacheService.evict(CacheKeys.CATEGORY_TREE);
        }
        return rows;
    }

    @Override
    public int update(Category category) {
        int rows = categoryMapper.update(category);
        if (rows > 0) {
            cacheService.evict(CacheKeys.CATEGORY_TREE);
        }
        return rows;
    }

    @Override
    public boolean updateStatus(Long id, int status) {
        boolean ok = categoryMapper.updateStatus(id, status) > 0;
        if (ok) {
            cacheService.evict(CacheKeys.CATEGORY_TREE);
        }
        return ok;
    }

    @Override
    public boolean delete(Long id) {
        // 存在子分类时不允许删除
        if (categoryMapper.countByParentId(id) > 0) {
            return false;
        }
        boolean ok = categoryMapper.deleteById(id) > 0;
        if (ok) {
            cacheService.evict(CacheKeys.CATEGORY_TREE);
        }
        return ok;
    }
}
