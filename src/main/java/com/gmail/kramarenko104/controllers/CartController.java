package com.gmail.kramarenko104.controllers;

import com.gmail.kramarenko104.dto.CartDto;
import com.gmail.kramarenko104.model.Cart;
import com.gmail.kramarenko104.model.User;
import com.gmail.kramarenko104.services.CartService;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Controller
@SessionAttributes(value = {"user", "cart", "warning"})
@RequestMapping("/cart")
public class CartController {

    private static Logger logger = LoggerFactory.getLogger(CartController.class);
    private static final String DB_WARNING = "Check your connection to DB!";
    private CartService cartService;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView doGet(@ModelAttribute("user") User user) {
        ModelAndView modelAndView = new ModelAndView("cart");
        if (em != null) {
            Cart cart = cartService.getCartByUserId(user.getUserId());
            logger.debug("[eshop] CartController.doGet:  user cart: " + cart);
            modelAndView.addObject("user", user);
            modelAndView.addObject("cart", cart);
        } else {
            modelAndView.addObject("warning", DB_WARNING);
        }
        return modelAndView;
    }


    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody
    String doPost(@RequestParam("action") String action,
                  @RequestParam("productId") int productId,
                  @RequestParam("quantity") int quantity,
                  @ModelAttribute("user") User user) {
        String jsonString = null;
        ModelAndView modelAndView = new ModelAndView("cart");

        if (em != null) {
            if (user != null && user.getLogin() != null) {
                long userId = user.getUserId();
                modelAndView.addObject("user", user);
                // CHANGE CART
                // getProduct info from Ajax POST req (from updateCart.js)
                boolean needRefresh = false;
                if (action != null && action.length() > 0) {
                    switch (action) {
                        case "add":
                            logger.debug("[eshop] CartController.doPost: GOT PARAMETER 'add'....");
                            cartService.addProduct(user.getUserId(), productId, quantity);
                            logger.debug("[eshop] CartController.doPost: for user '" + user.getName() + "' was added " + quantity + " of productId: " + productId);
                            break;
                        case "remove":
                            logger.debug("[eshop] CartController.doPost: GOT PARAMETER 'remove' ");
                            cartService.removeProduct(user.getUserId(), productId, quantity);
                            logger.debug("[eshop] CartController.doPost: for user: " + user.getUserId() + " was removed " + quantity + " of productId " + productId);
                            break;
                    }
                    needRefresh = true;
                }
                //  REFRESH CART's characteristics if need to refresh
                if (needRefresh) {
                    Cart userCart = cartService.getCartByUserId(userId);
                    logger.debug("[eshop] CartController.doPost:  updated cart " + userCart);
                    modelAndView.addObject("cart", userCart);

                    // send JSON with updated Cart back to cart.jsp
                    if (userCart != null) {
                        // we don't need to pass full Cart object with included User object, but only cart's properties
                        // so, use Cart DTO object for marshalling
                        CartDto jsonCart = new CartDto(userId);
                        jsonCart.setProducts(userCart.getProducts());
                        jsonCart.setItemsCount(userCart.getItemsCount());
                        jsonCart.setTotalSum(userCart.getTotalSum());
                        jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(jsonCart);
                    }
                }
            } else {
                // pass empty cart for null user to see modal window from updateCart.js about login before shopping
                new GsonBuilder().setPrettyPrinting().create().toJson(new CartDto(0));
            }
        } else { // session to DB is closed
            modelAndView.addObject("warning", DB_WARNING);
        }
//        logger.debug("[eshop] CartController.doPost:  return json: " + jsonString);
//        logger.debug("[eshop] CartController.doPost:   exit with user: " + modelAndView.getModel().get("user"));
        logger.debug("[eshop] CartController.doPost:   exit with cart: " + modelAndView.getModel().get("cart"));
        return jsonString;
    }
}
