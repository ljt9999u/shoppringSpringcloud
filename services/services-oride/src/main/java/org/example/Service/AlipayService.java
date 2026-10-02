package org.example.Service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.Mapper.OrderMapper;
import org.example.Order.OrderPOJO;
import org.example.Order.Payment;
import org.example.priperties.AlipayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝沙箱支付服务
 */
@Slf4j
@Service
public class AlipayService {

    @Autowired
    private AlipayProperties alipayProperties;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 生成支付宝支付页面表单（用户扫码/登录后完成支付）
     *
     * @param orderNo 订单号
     * @return 支付宝收银台 HTML 表单，前端直接输出到页面即可跳转
     */
    public String createPayForm(String orderNo) {
        OrderPOJO order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在，orderNo=" + orderNo);
        }
        if (order.getStatus() != 0) {
            throw new RuntimeException("订单状态不允许支付，当前状态=" + order.getStatus());
        }

        AlipayClient client = buildClient();
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // 支付完成后的同步跳转地址（用户浏览器）
        request.setReturnUrl(alipayProperties.getReturnUrl());
        // 支付结果异步通知地址（支付宝服务器 → 我们的后端）
        request.setNotifyUrl(alipayProperties.getNotifyUrl());

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(orderNo);
        model.setTotalAmount(order.getPayAmount().toPlainString());
        model.setSubject("购物网站订单支付-" + orderNo);
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        request.setBizModel(model);

        try {
            AlipayTradePagePayResponse response = client.pageExecute(request);
            if (response.isSuccess()) {
                log.info("支付宝支付表单生成成功，订单号：{}", orderNo);
                return response.getBody();
            } else {
                log.error("支付宝支付表单生成失败，订单号：{}，错误：{}", orderNo, response.getSubMsg());
                throw new RuntimeException("生成支付页面失败：" + response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            log.error("调用支付宝接口异常，订单号：{}", orderNo, e);
            throw new RuntimeException("调用支付宝支付接口异常：" + e.getMessage());
        }
    }

    /**
     * 处理支付宝异步回调（支付成功通知）
     *
     * @param params 回调参数
     * @return 支付宝要求的响应文本，success / failure
     */
    public String handleNotify(Map<String, String[]> params) {
        // 1. 参数转 Map<String, String>
        Map<String, String> notifyParams = new HashMap<>();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String[] values = entry.getValue();
            if (values != null && values.length > 0) {
                notifyParams.put(entry.getKey(), values[0]);
            }
        }

        log.info("收到支付宝异步回调，参数：{}", notifyParams);

        // 2. 验签
        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV1(
                    notifyParams,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType());
        } catch (AlipayApiException e) {
            log.error("支付宝回调验签异常", e);
            return "failure";
        }
        if (!signVerified) {
            log.error("支付宝回调验签失败，可能为伪造请求，参数：{}", notifyParams);
            return "failure";
        }

        // 3. 校验业务参数
        String orderNo = notifyParams.get("out_trade_no");
        String tradeNo = notifyParams.get("trade_no");
        String tradeStatus = notifyParams.get("trade_status");
        String totalAmount = notifyParams.get("total_amount");

        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            log.info("支付宝回调非支付成功状态，trade_status={}，忽略", tradeStatus);
            return "success"; // 其他状态也返回 success，避免重复通知
        }

        // 4. 幂等处理：先查支付记录，已支付则直接返回
        Payment payment = orderMapper.findPaymentByOrderNo(orderNo);
        if (payment != null && payment.getStatus() == 1) {
            log.info("订单已支付，幂等返回，orderNo={}", orderNo);
            return "success";
        }

        // 5. 校验金额
        OrderPOJO order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            log.error("支付宝回调订单不存在，orderNo={}", orderNo);
            return "failure";
        }
        if (order.getPayAmount().compareTo(new BigDecimal(totalAmount)) != 0) {
            log.error("支付宝回调金额不一致，orderNo={}，订单金额={}，回调金额={}",
                    orderNo, order.getPayAmount(), totalAmount);
            return "failure";
        }

        // 6. 更新订单状态为待发货
        if (order.getStatus() == 0) {
            orderMapper.updatePaySuccess(order.getId());
        }

        // 7. 记录/更新支付记录
        if (payment == null) {
            Payment newPayment = new Payment();
            newPayment.setOrderNo(orderNo);
            newPayment.setPayMethod(2); // 支付宝
            newPayment.setPayAmount(new BigDecimal(totalAmount));
            newPayment.setTradeNo(tradeNo);
            newPayment.setStatus(1);
            orderMapper.insertPayment(newPayment);
        } else {
            payment.setStatus(1);
            payment.setTradeNo(tradeNo);
            payment.setPayTime(LocalDateTime.now());
            orderMapper.updatePaymentStatus(orderNo, 1);
        }

        log.info("支付宝支付成功，orderNo={}，tradeNo={}，amount={}", orderNo, tradeNo, totalAmount);
        return "success";
    }

    private AlipayClient buildClient() {
        return new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getMerchantPrivateKey(),
                alipayProperties.getFormat(),
                alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getSignType());
    }
}
