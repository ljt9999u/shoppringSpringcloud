package org.example.Service;

import org.example.Product.ProductComment;
import org.example.Product.ProductCommentVO;
import org.example.common.PageResult;

import java.util.Map;

/**
 * 商品评价服务
 */
public interface ProductCommentService {

    /**
     * 分页查询商品的展示中评价
     */
    PageResult<ProductCommentVO> listPage(Long productId, int pageNum, int pageSize);

    /**
     * 分页查询某商家旗下所有商品的评价（含隐藏，商家评价管理用）
     */
    PageResult<ProductCommentVO> listPageByMerchant(Long merchantId, int pageNum, int pageSize);

    /**
     * 商品评价汇总：总数 / 平均分 / 好评数 / 好评率 / 带图数 / 星级分布
     */
    Map<String, Object> summary(Long productId);

    /**
     * 发表评价（必须已购买该商品，且同一订单同一商品只能评价一次）
     *
     * @param comment 评价内容（productId/orderId/star/content/commentImg）
     * @param userId  当前登录用户（网关注入，不信任前端传值）
     */
    boolean add(ProductComment comment, Long userId);

    /**
     * 商家回复评价
     */
    boolean reply(Long id, String merchantReply);
}
