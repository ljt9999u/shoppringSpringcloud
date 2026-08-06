package org.example.Service.Impl;

import org.example.Mapper.ProductMapper;
import org.example.Product.Product;
import org.example.Service.ProductService;
import org.example.common.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public Product getProduct(Long productId) {
        return productMapper.findById(productId);
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
        return productMapper.update(product);
    }

    @Override
    public int deleteProduct(Long id) {
        return productMapper.deleteById(id);
    }

    @Override
    public boolean reduceStock(Long id, int quantity) {
        return productMapper.reduceStock(id, quantity) > 0;
    }
}
