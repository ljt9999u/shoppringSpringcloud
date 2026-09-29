package org.example.cache;

/**
 * 商品服务缓存 key 统一管理
 */
public final class CacheKeys {

    private CacheKeys() {
    }

    /** 商品详情 key 前缀，拼接商品ID：product:detail:{id} */
    public static final String PRODUCT_DETAIL_PREFIX = "product:detail:";

    /** 商品分类树 */
    public static final String CATEGORY_TREE = "category:tree";

    /** 品牌全量列表 */
    public static final String BRAND_LIST = "brand:list";

    public static String productDetail(Long id) {
        return PRODUCT_DETAIL_PREFIX + id;
    }
}
