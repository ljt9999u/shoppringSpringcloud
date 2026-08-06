package org.example.Service;

import org.example.Oride.OridePOJO;
import org.example.Product.Product;

public interface Orideservice {
    OridePOJO createOride(Long productId, Long userId);

    Product getProductFromRemote(Long productId);

    /**
     * 查询商品订单数
     */
    int getOrderCountByProduct(Long productId);

    /**
     * 查询商品评价数
     */
    int getCommentCount(Long productId);
}
