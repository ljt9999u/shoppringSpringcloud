package org.example.priperties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "oride")
public class OrideProperties {
    String timeout;
    String autoConfirm;
}
