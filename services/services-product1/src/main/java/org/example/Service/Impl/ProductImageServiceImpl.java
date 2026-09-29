package org.example.Service.Impl;

import org.example.Mapper.ProductImageMapper;
import org.example.Product.ProductImage;
import org.example.Service.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品图片服务实现
 */
@Service
public class ProductImageServiceImpl implements ProductImageService {

    @Autowired
    private ProductImageMapper productImageMapper;

    @Override
    public List<ProductImage> listByProductId(Long productId) {
        return productImageMapper.findByProductId(productId);
    }

    @Override
    public int add(ProductImage image) {
        if (image.getSort() == null) {
            image.setSort(0);
        }
        return productImageMapper.insert(image);
    }

    @Override
    public int addBatch(List<ProductImage> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }
        for (ProductImage image : list) {
            if (image.getSort() == null) {
                image.setSort(0);
            }
        }
        return productImageMapper.insertBatch(list);
    }

    @Override
    public int update(ProductImage image) {
        return productImageMapper.update(image);
    }

    @Override
    public boolean delete(Long id) {
        return productImageMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByProductId(Long productId) {
        return productImageMapper.deleteByProductId(productId) >= 0;
    }
}
