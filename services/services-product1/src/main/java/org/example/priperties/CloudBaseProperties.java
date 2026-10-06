package org.example.priperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 腾讯云 CloudBase（云开发）配置
 * 用于前端直传图片到云存储，后端只需提供环境 ID
 */
@Data
@Component
@ConfigurationProperties(prefix = "cloudbase")
public class CloudBaseProperties {

    /**
     * CloudBase 环境 ID
     * 获取位置：CloudBase 控制台 → 环境列表 → 环境 ID
     * 格式示例：demo2-4gx58pwtb0429fb1
     */
    private String envId;
}
