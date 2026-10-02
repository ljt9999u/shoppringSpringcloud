package org.example.cache;

/**
 * 商品服务缓存 key 统一管理
 */
public final class CacheKeys {

    private CacheKeys() {
    }

    /** 商品详情 key 前缀，拼接商品ID：product:detail:{id} */
    public static final String PRODUCT_DETAIL_PREFIX = "product:detail:"; //这样拼接是给redis查缓存看的不是给数据库看的

    /** 商品分类树 */
    public static final String CATEGORY_TREE = "category:tree";//同里都是个redis查缓存看的

    /** 品牌全量列表 */
    public static final String BRAND_LIST = "brand:list";//同里都是个redis查缓存看的

    public static String productDetail(Long id) {
        return PRODUCT_DETAIL_PREFIX + id;
    }
}
