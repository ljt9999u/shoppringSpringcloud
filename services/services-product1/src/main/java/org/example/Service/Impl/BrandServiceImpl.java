package org.example.Service.Impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.example.Mapper.BrandMapper;
import org.example.Product.Brand;
import org.example.Service.BrandService;
import org.example.cache.CacheKeys;
import org.example.cache.RedisCacheService;
import org.example.common.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 品牌服务实现
 */
@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private RedisCacheService cacheService;

    /** 品牌列表缓存基准 TTL：60 分钟（品牌极少变化） */
    private static final Duration BRAND_TTL = Duration.ofMinutes(60);
    /** TTL 随机抖动上界：0~10 分钟，防雪崩 */
    private static final long BRAND_TTL_JITTER_SECONDS = 600;

    private int[] normalizePage(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;
        return new int[]{pageNum, pageSize};
    }

    /**
     * 全量品牌列表：下拉选择高频访问，缓存 + 随机 TTL（防雪崩）；变更时删除缓存。
     */
    @Override
    public List<Brand> listAll() {
        return cacheService.queryWithProtect(
                CacheKeys.BRAND_LIST,
                new TypeReference<List<Brand>>() {
                },
                BRAND_TTL,
                BRAND_TTL_JITTER_SECONDS,
                () -> brandMapper.findAll());
    }

    @Override
    public PageResult<Brand> page(int pageNum, int pageSize) {
        int[] p = normalizePage(pageNum, pageSize);
        int offset = (p[0] - 1) * p[1];
        long total = brandMapper.count();
        List<Brand> list = brandMapper.findPage(offset, p[1]);
        return new PageResult<>(total, p[0], p[1], list);
    }

    @Override
    public Brand getById(Long id) {
        return brandMapper.findById(id);
    }

    @Override
    public int add(Brand brand) {
        int rows = brandMapper.insert(brand);
        if (rows > 0) {
            cacheService.evict(CacheKeys.BRAND_LIST);
        }
        return rows;
    }

    @Override
    public int update(Brand brand) {
        int rows = brandMapper.update(brand);
        if (rows > 0) {
            cacheService.evict(CacheKeys.BRAND_LIST);
        }
        return rows;
    }

    @Override
    public boolean delete(Long id) {
        boolean ok = brandMapper.deleteById(id) > 0;
        if (ok) {
            cacheService.evict(CacheKeys.BRAND_LIST);
        }
        return ok;
    }
}
