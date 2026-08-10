package org.example.Service.Impl;

import lombok.extern.slf4j.Slf4j;
import org.example.Cart.Cart;
import org.example.Feign.ProductFeign;
import org.example.Feign.UserFeign;
import org.example.Mapper.CartMapper;
import org.example.Product.Product;
import org.example.Service.CartService;
import org.example.User.UserPOJO;
import org.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务实现
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartMapper cartMapper;
    @Autowired
    ProductFeign productFeign;
    @Autowired
    UserFeign userFeign;

    @Override
    public Cart addToCart(Cart cart) {
        // 1. 远程校验用户是否存在
        Result<UserPOJO> userResult = userFeign.getUserById(cart.getUserId());
        if (userResult.getCode() != 200 || userResult.getData() == null) {
            throw new RuntimeException("用户不存在，加购失败");
        }
        // 2. 远程校验商品是否存在且上架
        Result<Product> productResult = productFeign.getProductById(cart.getProductId());
        if (productResult.getCode() != 200 || productResult.getData() == null) {
            throw new RuntimeException("商品不存在，加购失败");
        }
        Product product = productResult.getData();
        if (product.getStatus() == null || product.getStatus() != 1) {
            throw new RuntimeException("商品已下架，无法加入购物车");
        }
        // 3. 校验加购数量是否超过库存
        if (cart.getQuantity() == null || cart.getQuantity() < 1) {
            cart.setQuantity(1);
        }
        if (cart.getQuantity() > product.getStock()) {
            throw new RuntimeException("加购数量超过库存，当前库存：" + product.getStock());
        }

        // 4. 判断该用户是否已加入过相同商品+规格，是则合并数量，否则新增
        Cart exist = cartMapper.findByUserProduct(cart.getUserId(), cart.getProductId(), cart.getSpecId());
        if (exist != null) {
            int newQuantity = exist.getQuantity() + cart.getQuantity();
            if (newQuantity > product.getStock()) {
                throw new RuntimeException("购物车已有该商品，合并后数量超过库存");
            }
            cartMapper.addQuantity(exist.getId(), cart.getQuantity());
            exist.setQuantity(newQuantity);
            return exist;
        }

        // 新增购物车项，默认勾选
        if (cart.getChecked() == null) {
            cart.setChecked(1);
        }
        cartMapper.insertCart(cart);
        log.info("加入购物车成功，cartId={}，userId={}，productId={}", cart.getId(), cart.getUserId(), cart.getProductId());
        return cart;
    }

    @Override
    public boolean updateQuantity(Long id, int quantity) {
        Cart cart = cartMapper.findById(id);
        if (cart == null) {
            return false;
        }
        if (quantity < 1) {
            return false;
        }
        // 校验数量不超过库存
        Result<Product> productResult = productFeign.getProductById(cart.getProductId());
        if (productResult.getCode() == 200 && productResult.getData() != null) {
            if (quantity > productResult.getData().getStock()) {
                throw new RuntimeException("数量超过库存，当前库存：" + productResult.getData().getStock());
            }
        }
        return cartMapper.updateQuantity(id, quantity) > 0;
    }

    @Override
    public boolean updateChecked(Long id, int checked) {
        return cartMapper.updateChecked(id, checked) > 0;
    }

    @Override
    public boolean selectAll(Long userId, int checked) {
        return cartMapper.updateAllChecked(userId, checked) > 0;
    }

    /**
     * 远程补充购物车项的商品信息，并标记失效项（商品不存在或已下架）
     */
    private void enrichCartProductInfo(Cart cart) {
        try {
            Result<Product> productResult = productFeign.getProductById(cart.getProductId());
            if (productResult.getCode() == 200 && productResult.getData() != null) {
                Product product = productResult.getData();
                cart.setProductName(product.getName());
                cart.setProductImage(product.getMainImage());
                cart.setPrice(product.getPrice());
                cart.setStock(product.getStock());
                cart.setProductStatus(product.getStatus());
                cart.setValid(product.getStatus() != null && product.getStatus() == 1);
                cart.setSubtotal(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
            } else {
                cart.setValid(false);
            }
        } catch (Exception e) {
            log.warn("获取商品信息失败，cartId={}, productId={}", cart.getId(), cart.getProductId());
            cart.setValid(false);
        }
    }

    @Override
    public List<Cart> listByUserId(Long userId) {
        List<Cart> list = cartMapper.findListByUserId(userId);
        for (Cart cart : list) {
            enrichCartProductInfo(cart);
        }
        return list;
    }

    @Override
    public List<Cart> listCheckedByUserId(Long userId) {
        List<Cart> list = cartMapper.findCheckedByUserId(userId);
        List<Cart> validList = new ArrayList<>();
        for (Cart cart : list) {
            enrichCartProductInfo(cart);
            // 结算时过滤掉失效项（商品下架或不存在）
            if (cart.getValid() != null && cart.getValid()) {
                validList.add(cart);
            }
        }
        return validList;
    }

    @Override
    public int countByUserId(Long userId) {
        return cartMapper.countByUserId(userId);
    }

    @Override
    public boolean deleteById(Long id) {
        return cartMapper.deleteById(id) > 0;
    }

    @Override
    public boolean batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        return cartMapper.batchDeleteByIds(ids) > 0;
    }

    @Override
    public boolean clearByUserId(Long userId) {
        return cartMapper.clearByUserId(userId) > 0;
    }

    @Override
    public boolean deleteCheckedByUserId(Long userId) {
        return cartMapper.deleteCheckedByUserId(userId) > 0;
    }
}
