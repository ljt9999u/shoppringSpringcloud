package org.example.priperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝沙箱配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayProperties {
    /** 沙箱应用 appId */
    private String appId;
    /** 应用私钥（签名用） */
    private String merchantPrivateKey;
    /** 支付宝公钥（验签回调用） */
    private String alipayPublicKey;
    /** 沙箱网关地址 */
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    /** 支付结果异步回调地址（公网可访问） */
    private String notifyUrl;
    /** 支付完成后前端页面跳转地址 */
    private String returnUrl;
    private String signType = "RSA2";
    private String charset = "utf-8";
    private String format = "json";
}
