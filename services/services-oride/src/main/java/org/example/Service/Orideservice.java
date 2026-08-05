package org.example.Service;

import org.example.Oride.OridePOJO;
import org.example.Product.Product;
import org.springframework.web.bind.annotation.RequestParam;

public interface Orideservice {
    OridePOJO createOride(Long productId, Long userId);

     Product getProductFromRemote(Long productId);

}
