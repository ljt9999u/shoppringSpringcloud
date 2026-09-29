package org.example.Service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.example.Mapper.ProductMapper;
import org.example.Product.Product;
import org.example.Service.ProductService;
import org.example.cache.CacheKeys;
import org.example.cache.RedisCacheService;
import org.example.common.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisCacheService cacheService;

    /** 商品详情缓存基准 TTL：30 分钟 */
    private static final Duration PRODUCT_TTL = Duration.ofMinutes(30);
    /** 商品详情 TTL 随机抖动上界：0~5 分钟，防雪崩 */
    private static final long PRODUCT_TTL_JITTER_SECONDS = 300;

    @Override
    public Product getProduct(Long productId) {
        // 热点读：空值缓存防穿透 + 互斥锁防击穿 + 随机TTL防雪崩
        return cacheService.queryWithProtect(
                CacheKeys.productDetail(productId),
                new TypeReference<Product>() {
                },
                PRODUCT_TTL,
                PRODUCT_TTL_JITTER_SECONDS,
                () -> productMapper.findById(productId));
    }

    @Override
    public List<Product> listAllOnShelf() {
        return productMapper.findAllOnShelf();
    }

    @Override
    public List<Product> searchByName(String keyword) {
        return productMapper.searchByName(keyword);
    }

    @Override
    public List<Product> listByCategory(Long categoryId) {
        return productMapper.findByCategory(categoryId);
    }

    // ========== 分页查询实现 ==========

    /**
     * 规范化分页参数，防止负数或过大
     */
    private int[] normalizePage(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;
        return new int[]{pageNum, pageSize};
    }

    @Override
    public PageResult<Product> listPageOnShelf(int pageNum, int pageSize) {
        int[] p = normalizePage(pageNum, pageSize);
        int offset = (p[0] - 1) * p[1];
        long total = productMapper.countOnShelf();
        List<Product> list = productMapper.findPageOnShelf(offset, p[1]);
        return new PageResult<>(total, p[0], p[1], list);
    }

    @Override
    public PageResult<Product> searchPageByName(String keyword, int pageNum, int pageSize) {
        int[] p = normalizePage(pageNum, pageSize);
        int offset = (p[0] - 1) * p[1];
        long total = productMapper.countSearchByName(keyword);
        List<Product> list = productMapper.searchPageByName(keyword, offset, p[1]);
        return new PageResult<>(total, p[0], p[1], list);
    }

    @Override
    public PageResult<Product> listPageByCategory(Long categoryId, int pageNum, int pageSize) {
        int[] p = normalizePage(pageNum, pageSize);
        int offset = (p[0] - 1) * p[1];
        long total = productMapper.countByCategory(categoryId);
        List<Product> list = productMapper.findPageByCategory(categoryId, offset, p[1]);
        return new PageResult<>(total, p[0], p[1], list);
    }

    // ========== 增删改 ==========

    @Override
    public int addProduct(Product product) {
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        return productMapper.insert(product);
    }

    @Override
    public int updateProduct(Product product) {
        int rows = productMapper.update(product);
        // DB 更新成功后删除详情缓存，下次查询自动重建，避免脏数据
        if (rows > 0 && product.getId() != null) {
            cacheService.evict(CacheKeys.productDetail(product.getId()));
        }
        return rows;
    }

    @Override
    public int deleteProduct(Long id) {
        int rows = productMapper.deleteById(id);
        if (rows > 0) {
            cacheService.evict(CacheKeys.productDetail(id));
        }
        return rows;
    }

    @Override
    public boolean reduceStock(Long id, int quantity) {
        boolean success = productMapper.reduceStock(id, quantity) > 0;
        // 库存变化后删除详情缓存，防止下单后读到旧库存
        if (success) {
            cacheService.evict(CacheKeys.productDetail(id));
        }
        return success;
    }
}
