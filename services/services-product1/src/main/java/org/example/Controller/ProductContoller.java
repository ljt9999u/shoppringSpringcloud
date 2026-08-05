package org.example.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.Service.Impl.PriductImpl;
import org.example.Product.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RequestMapping("/api/product")
@RestController
public class ProductContoller {
    @Autowired
    private PriductImpl priductImpl;
   @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable("id") Long ProductId,
                              HttpServletRequest request) {
     String token =  request.getHeader("X-Token");
       System.out.println("hello===token=【"+token+"】");
        Product Product = priductImpl.getProduct(ProductId);
//        try {
//            TimeUnit.SECONDS.sleep(2);
//        }catch (InterruptedException e){
//            throw new RuntimeException(e);
//        }
        return  Product;
    }
}
