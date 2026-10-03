package org.example.Service.Impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.example.Feign.ProductFeign;
import org.example.Feign.UserFeign;
import org.example.Mapper.OrderMapper;
import org.example.Oride.OridePOJO;
import org.example.Order.*;
import org.example.Product.Product;
import org.example.Service.Orideservice;
import org.example.User.UserPOJO;
import org.example.common.PageResult;
import org.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class OrideImpl implements Orideservice {

    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    RestTemplate restemplate;
    @Autowired
    DiscoveryClient discoveryClient;
    @Qualifier("org.example.Feign.ProductFeign")
    @Autowired
    ProductFeign productFeign;
    @Qualifier("org.example.Feign.UserFeign")
    @Autowired
    UserFeign userFeign;
    @Autowired
    OrderMapper orderMapper;

    // ========== 原有方法 ==========

    @SentinelResource(value = "createOride", blockHandler = "createOrideFllback")
    @Override
    public OridePOJO createOride(Long productId, Long userId) {
        Product product = productFeign.getProductById(productId);
        OridePOJO oridePOJO = new OridePOJO();
        oridePOJO.setId(productId);
        oridePOJO.setTotalAmount(product.getPrice().multiply(new BigDecimal(product.getStock())));
        oridePOJO.setUserId(userId);
        oridePOJO.setNickname("张三");
        oridePOJO.setAddress("北京市朝阳区");
        oridePOJO.setProductList(Arrays.asList(product));
        return oridePOJO;
    }

    // Sentinel blockHandler 方法 - 兜底回调
    public OridePOJO createOrideFllback(Long productId, Long userId, BlockException e) {
        OridePOJO oridePOJO = new OridePOJO();
        oridePOJO.setId(0L);
        oridePOJO.setTotalAmount(new BigDecimal(0));
        oridePOJO.setUserId(userId);
        oridePOJO.setNickname("未知");
        oridePOJO.setAddress("异常信息" + e.getClass());
        return oridePOJO;
    }

    @Override
    public Product getProductFromRemote(Long productId) {
        // 直接写服务名，不需要DiscoveryClient
        Product product=productFeign.getProductById(productId);
        return product;
//        String url = "http://services-product1/product/" + productId;
//        log.info("远程调用商品服务，url:{}", url);
//        return restemplate.getForObject(url, Product.class);
    }

    @Override
    public int getOrderCountByProduct(Long productId) {
        return orderMapper.countOrdersByProduct(productId);
    }

    @Override
    public int getCommentCount(Long productId) {
        return orderMapper.countCommentByProduct(productId);
    }

    // ========== 订单业务 ==========

    /**
     * 生成唯一订单号：年月日时分秒 + 6位随机数
     */
    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
    }

    /**
     * 创建订单
     * 事务说明：
     * 1. @GlobalTransactional 开启 Seata 全局事务（seata.enabled=true 时生效）：
     *    XID 随 Feign 请求自动传播到商品服务，扣库存分支与订单分支要么一起提交、要么一起回滚；
     * 2. @Transactional 保留本地事务兜底：未部署 seata-server（enabled=false）时，
     *    仍保证订单主表与订单明细的本地原子性。
     */
    @GlobalTransactional(rollbackFor = Exception.class, name = "createOrder")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderPOJO createOrder(OrderPOJO order) {
        // 1. 远程调用用户服务，校验用户是否存在
        Result<UserPOJO> userResult = userFeign.getUserById(order.getUserId());
        if (userResult.getCode() != 200 || userResult.getData() == null) {
            throw new RuntimeException("用户不存在，下单失败");
        }
        log.info("用户校验通过：{}", userResult.getData().getUsername());

        // 2. 远程调用商品服务，校验商品并扣减库存
        List<OrderDetail> details = order.getDetailList();
        if (details != null && !details.isEmpty()) {
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (OrderDetail detail : details) {
                // 远程查询商品信息
                Product product = productFeign.getProductById(detail.getProductId());
                if (product == null || product.getId() == null) {
                    throw new RuntimeException("商品ID=" + detail.getProductId() + " 不存在");
                }
                // 校验库存
                if (product.getStock() < detail.getQuantity()) {
                    throw new RuntimeException("商品【" + product.getName() + "】库存不足");
                }
                // 远程调用商品服务扣减库存
                boolean reduceSuccess = productFeign.reduceStock(detail.getProductId(), detail.getQuantity());
                if (!reduceSuccess) {
                    throw new RuntimeException("商品【" + product.getName() + "】扣减库存失败");
                }
                // 填充商品快照信息
                detail.setProductName(product.getName());
                detail.setProductImage(product.getMainImage());
                detail.setPrice(product.getPrice());
                detail.setSubtotal(product.getPrice().multiply(new BigDecimal(detail.getQuantity())));
                totalAmount = totalAmount.add(detail.getSubtotal());
            }
            order.setTotalAmount(totalAmount);
            if (order.getMerchantId() == null && !details.isEmpty()) {
                // 通过商品获取商家ID（这里假设所有商品都是同一个商家）
                Product firstProduct = productFeign.getProductById(details.get(0).getProductId());
                if (firstProduct != null) {
                    order.setMerchantId(firstProduct.getMerchantId());
                }
            }
        }

        // 3. 生成订单号
        order.setOrderNo(generateOrderNo());
        if (order.getStatus() == null) {
            order.setStatus(0);
        }
        if (order.getFreight() == null) {
            order.setFreight(BigDecimal.ZERO);
        }
        if (order.getPayAmount() == null) {
            order.setPayAmount(order.getTotalAmount().add(order.getFreight()));
        }

        // 4. 保存订单
        orderMapper.insertOrder(order);
        // 5. 保存订单详情
        if (details != null && !details.isEmpty()) {
            for (OrderDetail detail : details) {
                detail.setOrderId(order.getId());
            }
            orderMapper.batchInsertDetail(details);
        }
        log.info("订单创建成功，订单号：{}", order.getOrderNo());
        return order;
    }

    @Override
    public OrderPOJO getOrderById(Long id) {
        OrderPOJO order = orderMapper.findById(id);
        if (order != null) {
            order.setDetailList(orderMapper.findDetailByOrderId(id));
            // 远程调用用户服务，补充用户信息
            enrichOrderUserInfo(order);
        }
        return order;
    }

    @Override
    public OrderPOJO getOrderByOrderNo(String orderNo) {
        OrderPOJO order = orderMapper.findByOrderNo(orderNo);
        if (order != null) {
            order.setDetailList(orderMapper.findDetailByOrderId(order.getId()));
            enrichOrderUserInfo(order);
        }
        return order;
    }

    /**
     * 远程调用用户服务，补充订单中的用户信息
     */
    private void enrichOrderUserInfo(OrderPOJO order) {
        try {
            Result<UserPOJO> userResult = userFeign.getUserById(order.getUserId());
            if (userResult.getCode() == 200 && userResult.getData() != null) {
                order.setUsername(userResult.getData().getUsername());
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败，orderId={}, userId={}", order.getId(), order.getUserId());
        }
    }

    /**
     * 分页参数规范化
     */
    private int[] normalizePage(int pageNum, int pageSize) {
        if (pageNum < 1) pageNum = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100;
        return new int[]{pageNum, pageSize};
    }

    @Override
    public PageResult<OrderPOJO> getUserOrdersPage(Long userId, int pageNum, int pageSize) {
        int[] p = normalizePage(pageNum, pageSize);
        int offset = (p[0] - 1) * p[1];
        long total = orderMapper.countByUserId(userId);
        List<OrderPOJO> list = orderMapper.findPageByUserId(userId, offset, p[1]);
        // 查询每个订单的详情
        for (OrderPOJO order : list) {
            order.setDetailList(orderMapper.findDetailByOrderId(order.getId()));
        }
        return new PageResult<>(total, p[0], p[1], list);
    }

    @Override
    public PageResult<OrderPOJO> getMerchantOrdersPage(Long merchantId, int pageNum, int pageSize) {
        int[] p = normalizePage(pageNum, pageSize);
        int offset = (p[0] - 1) * p[1];
        long total = orderMapper.countByMerchantId(merchantId);
        List<OrderPOJO> list = orderMapper.findPageByMerchantId(merchantId, offset, p[1]);
        for (OrderPOJO order : list) {
            order.setDetailList(orderMapper.findDetailByOrderId(order.getId()));
        }
        return new PageResult<>(total, p[0], p[1], list);
    }

    @Override
    public boolean cancelOrder(Long orderId) {
        OrderPOJO order = orderMapper.findById(orderId);
        if (order == null) {
            return false;
        }
        // 只有待付款状态才能取消
        if (order.getStatus() != 0) {
            return false;
        }
        return orderMapper.updateStatus(orderId, 4) > 0;
    }

    /**
     * 支付成功：更新订单状态 + 写入支付记录，两步写必须同事务，避免订单已改态但支付记录丢失
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean payOrder(Long orderId, int payMethod, String tradeNo) {
        OrderPOJO order = orderMapper.findById(orderId);
        if (order == null || order.getStatus() != 0) {
            return false;
        }
        // 更新订单状态为待发货
        orderMapper.updatePaySuccess(orderId);
        // 创建支付记录
        Payment payment = new Payment();
        payment.setOrderNo(order.getOrderNo());
        payment.setPayMethod(payMethod);
        payment.setPayAmount(order.getPayAmount());
        payment.setTradeNo(tradeNo);
        payment.setStatus(1);
        orderMapper.insertPayment(payment);
        log.info("订单支付成功，订单号：{}", order.getOrderNo());
        return true;
    }

    /**
     * 发货：更新订单状态 + 写入物流记录，两步写必须同事务
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean shipOrder(Long orderId, String logisticsNo, String company) {
        OrderPOJO order = orderMapper.findById(orderId);
        if (order == null || order.getStatus() != 1) {
            return false;
        }
        // 更新订单状态为待收货
        orderMapper.updateShipSuccess(orderId);
        // 创建物流记录
        Logistics logistics = new Logistics();
        logistics.setOrderNo(order.getOrderNo());
        logistics.setLogisticsNo(logisticsNo);
        logistics.setCompany(company);
        logistics.setStatus(1);
        orderMapper.insertLogistics(logistics);
        log.info("订单发货成功，订单号：{}", order.getOrderNo());
        return true;
    }

    /**
     * 确认收货：订单状态置完成 + 物流状态置签收，两步更新必须同事务
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean receiveOrder(Long orderId) {
        OrderPOJO order = orderMapper.findById(orderId);
        if (order == null || order.getStatus() != 2) {
            return false;
        }
        // 更新订单状态为已完成
        orderMapper.updateReceiveSuccess(orderId);
        // 更新物流状态为已签收
        orderMapper.updateLogisticsStatus(order.getOrderNo(), 2);
        log.info("订单确认收货成功，订单号：{}", order.getOrderNo());
        return true;
    }

    @Override
    public List<OrderDetail> getOrderDetails(Long orderId) {
        return orderMapper.findDetailByOrderId(orderId);
    }

    // ========== 支付 ==========

    @Override
    public Payment getPayment(String orderNo) {
        return orderMapper.findPaymentByOrderNo(orderNo);
    }

    // ========== 退款 ==========

    @Override
    public boolean applyRefund(Refund refund) {
        OrderPOJO order = orderMapper.findByOrderNo(refund.getOrderNo());
        if (order == null) {
            return false;
        }
        refund.setUserId(order.getUserId());
        refund.setStatus(0); // 申请中
        return orderMapper.insertRefund(refund) > 0;
    }

    @Override
    public Refund getRefund(String orderNo) {
        return orderMapper.findRefundByOrderNo(orderNo);
    }

    @Override
    public boolean handleRefund(Long refundId, int status) {
        return orderMapper.updateRefundStatus(refundId, status) > 0;
    }

    // ========== 物流 ==========

    @Override
    public Logistics getLogistics(String orderNo) {
        return orderMapper.findLogisticsByOrderNo(orderNo);
    }

    @Override
    public boolean updateLogisticsStatus(String orderNo, int status) {
        return orderMapper.updateLogisticsStatus(orderNo, status) > 0;
    }

    // ========== 评价 ==========

    @Override
    public boolean addComment(Comment comment) {
        return orderMapper.insertComment(comment) > 0;
    }

    @Override
    public PageResult<Comment> getProductCommentsPage(Long productId, int pageNum, int pageSize) {
        int[] p = normalizePage(pageNum, pageSize);
        int offset = (p[0] - 1) * p[1];
        int total = orderMapper.countCommentByProduct(productId);
        List<Comment> list = orderMapper.findCommentByProductId(productId, offset, p[1]);
        return new PageResult<>((long) total, p[0], p[1], list);
    }
}
