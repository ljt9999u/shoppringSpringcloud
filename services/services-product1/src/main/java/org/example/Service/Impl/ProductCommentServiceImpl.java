package org.example.Service.Impl;

import org.example.Mapper.ProductCommentMapper;
import org.example.Product.ProductComment;
import org.example.Product.ProductCommentVO;
import org.example.Service.ProductCommentService;
import org.example.common.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品评价服务实现
 */
@Service
public class ProductCommentServiceImpl implements ProductCommentService {

    @Autowired
    private ProductCommentMapper productCommentMapper;

    /**
     * 规范化分页参数，防止负数或过大
     */
    private int[] normalizePage(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 50) pageSize = 50;
        return new int[]{pageNum, pageSize};
    }

    @Override
    public PageResult<ProductCommentVO> listPage(Long productId, int pageNum, int pageSize) {
        int[] p = normalizePage(pageNum, pageSize);
        int offset = (p[0] - 1) * p[1];
        long total = productCommentMapper.countByProduct(productId);
        List<ProductCommentVO> list = productCommentMapper.findPageByProduct(productId, offset, p[1]);
        return new PageResult<>(total, p[0], p[1], list);
    }

    @Override
    public PageResult<ProductCommentVO> listPageByMerchant(Long merchantId, int pageNum, int pageSize) {
        int[] p = normalizePage(pageNum, pageSize);
        int offset = (p[0] - 1) * p[1];
        long total = productCommentMapper.countByMerchant(merchantId);
        List<ProductCommentVO> list = productCommentMapper.findPageByMerchant(merchantId, offset, p[1]);
        return new PageResult<>(total, p[0], p[1], list);
    }

    @Override
    public Map<String, Object> summary(Long productId) {
        Map<String, Object> row = productCommentMapper.summaryByProduct(productId);

        long total = row == null ? 0L : ((Number) row.getOrDefault("total", 0)).longValue();
        double avgStar = row == null ? 0d : ((Number) row.getOrDefault("avgStar", 0)).doubleValue();
        long goodCount = row == null ? 0L : ((Number) row.getOrDefault("goodCount", 0)).longValue();
        long imgCount = row == null ? 0L : ((Number) row.getOrDefault("imgCount", 0)).longValue();

        // 星级分布补零，固定返回 5~1 星，前端可直接渲染进度条
        Map<Integer, Long> distMap = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            distMap.put(star, 0L);
        }
        List<Map<String, Object>> rawDist = productCommentMapper.starDistribution(productId);
        for (Map<String, Object> item : rawDist) {
            int star = ((Number) item.get("star")).intValue();
            long cnt = ((Number) item.get("cnt")).longValue();
            distMap.put(star, cnt);
        }
        List<Map<String, Object>> distribution = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : distMap.entrySet()) {
            Map<String, Object> starRow = new LinkedHashMap<>();
            starRow.put("star", entry.getKey());
            starRow.put("count", entry.getValue());
            distribution.add(starRow);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalCount", total);
        result.put("avgStar", BigDecimal.valueOf(avgStar).doubleValue());
        result.put("goodCount", goodCount);
        result.put("goodRate", total == 0 ? 0 : Math.round(goodCount * 1000.0 / total) / 10.0);
        result.put("imgCount", imgCount);
        result.put("distribution", distribution);
        return result;
    }

    @Override
    public boolean add(ProductComment comment, Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("未登录，请先登录");
        }
        if (comment.getProductId() == null || comment.getOrderId() == null) {
            throw new IllegalArgumentException("商品或订单信息不完整");
        }
        if (comment.getStar() == null || comment.getStar() < 1 || comment.getStar() > 5) {
            throw new IllegalArgumentException("评分必须为 1~5 星");
        }
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("评价内容不能为空");
        }

        // 必须购买（已支付）该商品
        int bought = productCommentMapper.countPaidOrderDetail(
                comment.getOrderId(), userId, comment.getProductId());
        if (bought <= 0) {
            throw new IllegalArgumentException("只有购买该商品后才能评价");
        }
        // 同一订单同一商品只能评价一次
        int commented = productCommentMapper.countByOrderProduct(
                comment.getOrderId(), comment.getProductId());
        if (commented > 0) {
            throw new IllegalArgumentException("该订单的此商品已评价过，请勿重复评价");
        }

        comment.setUserId(userId);
        comment.setContent(comment.getContent().trim());
        comment.setMerchantReply(null);
        comment.setStatus(1);
        return productCommentMapper.insert(comment) > 0;
    }

    @Override
    public boolean reply(Long id, String merchantReply) {
        if (id == null) {
            throw new IllegalArgumentException("评价ID不能为空");
        }
        if (merchantReply == null || merchantReply.trim().isEmpty()) {
            throw new IllegalArgumentException("回复内容不能为空");
        }
        return productCommentMapper.reply(id, merchantReply.trim()) > 0;
    }
}
