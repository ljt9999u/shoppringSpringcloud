package org.example.Service;

import org.example.Product.Brand;
import org.example.common.PageResult;

import java.util.List;

/**
 * 品牌服务
 */
public interface BrandService {

    /**
     * 查询全部品牌
     */
    List<Brand> listAll();

    /**
     * 分页查询品牌
     */
    PageResult<Brand> page(int pageNum, int pageSize);

    /**
     * 根据ID查询
     */
    Brand getById(Long id);

    /**
     * 新增品牌
     */
    int add(Brand brand);

    /**
     * 更新品牌
     */
    int update(Brand brand);

    /**
     * 删除品牌
     */
    boolean delete(Long id);
}
