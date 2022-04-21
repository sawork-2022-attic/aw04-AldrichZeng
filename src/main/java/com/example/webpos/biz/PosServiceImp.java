package com.example.webpos.biz;

import com.example.webpos.db.PosDB;
import com.example.webpos.model.Cart;
import com.example.webpos.model.Item;
import com.example.webpos.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpSession;

@Component
@Slf4j
public class PosServiceImp implements PosService, Serializable {

    private PosDB posDB;

    @Autowired
    private HttpSession session;

    @Autowired
    public void setPosDB(PosDB posDB) {
        this.posDB = posDB;
    }


    @Override
    public Product randomProduct() {
        return products().get(ThreadLocalRandom.current().nextInt(0, products().size()));
    }

    @Override
    public void checkout(Cart cart) {

    }

    @Override
    public Cart add(Cart cart, Product product, int amount) {
        return add(cart, product.getId(), amount);
    }

    @Override
//    @Cacheable(value="products",key="#productId")//缓存了个寂寞，导致无法第二次添加到购物车
    public Cart add(Cart cart, String productId, int amount) {

        Product product = posDB.getProduct(productId);//底层用到遍历，所以耗时，故用缓存
        if (product == null) return cart;

        cart.addItem(new Item(product, amount));
        log.info("add successfully in "+session.getId());
        return cart;
    }

    @Override
    @Cacheable(value = "products")
    public List<Product> products() {
        log.info("Load all products in "+session.getId());
        return posDB.getProducts();
    }

    @Override
    public Cart plus(Cart cart, String productId, int amount){ //不改变item数组，仅改变元素的内容。
        List<Item> items = cart.getItems();
        for (Item item : items) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQuantity(item.getQuantity() + 1);
                log.info("Plus successfully in "+session.getId());
                return cart;
            }
        }
        log.info("Plus failed in "+session.getId());
        return cart;
    }
    @Override
    public Cart minus(Cart cart, String productId, int amount) {

        List<Item> items = cart.getItems();
        int finalQuantity=-1;
        boolean tag=false;
        for (Item item : items) {
            if (item.getProduct().getId().equals(productId)) {

                item.setQuantity(item.getQuantity() - 1);
                finalQuantity = item.getQuantity();
                log.info("Minus successfully in "+session.getId());
                tag=true;
                break;
            }
        }

        if(!tag || finalQuantity==-1){
            log.info("Minus failed in "+session.getId());
            return cart;
        }
        if(finalQuantity==0){
            this.deleteFromCart(cart,productId);
        }
        return cart;
    }

    @Override
    public Cart deleteFromCart(Cart cart, String productId) {
        List<Item> newItems = new ArrayList<>();
        List<Item> items = cart.getItems();
        for (Item item : items) {
            if (item.getProduct().getId().equals(productId)) {
                continue;
            } else {
                newItems.add(item);
            }
        }
        cart.setItems(newItems);
        log.info("delete " + productId + "from Cart in "+session.getId());
        return cart;
    }

    @Override
    public Cart cancel(Cart cart){
        cart.setItems(new ArrayList<>());
        return cart;
    }
}
