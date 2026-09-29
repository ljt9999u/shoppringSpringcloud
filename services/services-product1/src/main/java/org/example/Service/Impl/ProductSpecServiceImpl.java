package org.example.Service.Impl;

import org.example.Mapper.ProductSpecMapper;
import org.example.Product.ProductSpec;
import org.example.Service.ProductSpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品规格服务实现
 */
@Service
public class ProductSpecServiceImpl implements ProductSpecService {

    @Autowired
    private ProductSpecMapper productSpecMapper;

    @Override
    public List<ProductSpec> listByProductId(Long productId) {
        return productSpecMapper.findByProductId(productId);
    }

    @Override
    public ProductSpec getById(Long id) {
        return productSpecMapper.findById(id);
    }

    @Override
    public int add(ProductSpec spec) {
        if (spec.getStock() == null) {
            spec.setStock(0);
        }
        return productSpecMapper.insert(spec);
    }

    @Override
    public int update(ProductSpec spec) {
        return productSpecMapper.update(spec);
    }

    @Override
    public boolean delete(Long id) {
        return productSpecMapper.deleteById(id) > 0;
    }

    @Override
    public boolean reduceStock(Long id, int quantity) {
        return productSpecMapper.reduceStock(id, quantity) > 0;
    }
}
