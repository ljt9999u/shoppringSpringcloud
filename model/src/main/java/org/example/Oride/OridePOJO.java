package org.example.Oride;

import lombok.Data;
import org.example.Product.Product;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OridePOJO {
    private  Long id;
    private BigDecimal totalAmount;
    private Long userId;
    private String nickname;
    private String address;
    private List<Product> productList;
}
