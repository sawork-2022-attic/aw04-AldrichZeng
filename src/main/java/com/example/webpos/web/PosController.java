package com.example.webpos.web;

import com.example.webpos.biz.PosService;
import com.example.webpos.model.Cart;
import com.example.webpos.model.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class PosController {

    private PosService posService;

    @Autowired
    private HttpSession session;

//    private Cart cart;//每次都从session获取了。若不注释掉，则使用全局的cart，在各个Session中会使用同一个cart

    public Cart getCart(){
        Cart cart = (Cart)session.getAttribute("cart");
        if(cart == null){
            cart = new Cart();
            setCart(cart);
        }
        log.info("getCart in "+session.getId());
        return cart;
    }

//    @Autowired
    public void setCart(Cart cart) {
        session.setAttribute("cart",cart);
        log.info("setCart: "+cart.toString()+" in "+session.getId());//往session中填充
//        this.cart = cart;
    }

    @Autowired
    public void setPosService(PosService posService) {
        this.posService = posService;
    }

    @GetMapping("/")
    public String pos(Model model) {
        Cart cart = getCart();
        model.addAttribute("products", posService.products());
        model.addAttribute("cart", cart);
        return "index";
    }

    @GetMapping("/add")
    public String addByGet(@RequestParam(name = "pid") String pid, Model model) {
        Cart cart = getCart();
        cart = posService.add(cart, pid, 1);
        setCart(cart);
        model.addAttribute("products", posService.products());
        model.addAttribute("cart", cart);
        return "index";
    }

    @GetMapping("/minus")
    public String minusById(@RequestParam(name="pid") String pid, Model model){
        //采用pid，是因为每个Item的product.id不同。
        Cart cart = getCart();
        cart = posService.minus(cart,pid,1);
        setCart(cart);
        model.addAttribute("products", posService.products());
        model.addAttribute("cart", cart);

        return "index";
    }

    @GetMapping("/plus")
    public String plusById(@RequestParam(name="pid")String pid, Model model){
        Cart cart = getCart();
        cart = posService.plus(cart,pid,1);
        setCart(cart);
        model.addAttribute("products", posService.products());
        model.addAttribute("cart", cart);
        return "index";
    }
    @GetMapping("delete")
    public String deleteById(@RequestParam(name="pid")String pid, Model model){
        Cart cart = getCart();
        cart = posService.deleteFromCart(cart,pid);
        setCart(cart);
        model.addAttribute("products", posService.products());
        model.addAttribute("cart", cart);
        return "index";
    }

    @GetMapping("cancel")
    public String cancel(Model model){
        Cart cart = getCart();
        cart = posService.cancel(cart);
        setCart(cart);
        model.addAttribute("products", posService.products());
        model.addAttribute("cart", cart);
        return "index";
    }

}
