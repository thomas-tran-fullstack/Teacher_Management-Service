package managebean;

import entities.Products;
import entities.Customers;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Named("cartMB")
@SessionScoped
public class CartMB implements Serializable {

    private List<CartItem> cartItems = new ArrayList<>();
    private double totalAmount;

    public static class CartItem implements Serializable {

        private int quantity;
        private int size;
        private Products product;

        public CartItem(Products product, int quantity, int size) {
            this.product = product;
            this.quantity = quantity;
            this.size = size;
        }
        
        private List<CartItem> cartItems;

        public Products getProduct() {
            return product;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public double getTotal() {
            return product.getPrice().multiply(BigDecimal.valueOf(quantity)).doubleValue();
        }
    }

    @PostConstruct
    public void init() {
        cartItems = new ArrayList<>();
        updateTotal();
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void updateTotal() {
        totalAmount = 0;
        for (CartItem item : cartItems) {
            totalAmount += item.getTotal();
        }
    }

    public void addToCart(Products p, int quantity, int size) {
        for (CartItem ci : cartItems) {
            if (ci.getProduct().getProductID().equals(p.getProductID()) && ci.getSize() == size) {
                ci.setQuantity(ci.getQuantity() + quantity); // increase quantity
                updateTotal();
                return;
            }
        }
        cartItems.add(new CartItem(p, quantity, size));
        updateTotal();
    }

    public int getTotalItems() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    public void removeItem(CartItem item) {
        cartItems.remove(item);
        updateTotal();
    }

    public void updateQuantity(CartMB.CartItem item, int quantity) {
        if (quantity < 1) {
            removeItem(item);
            return;
        }
        item.setQuantity(quantity);
        updateTotal();
    }

    public String buyNow() {
        // redirect to checkout page
        return "/client/checkout.xhtml?faces-redirect=true";
    }

    public boolean hasOutOfStockItems() {
        for (CartItem item : cartItems) {
            if (item.getProduct().getQuantity() <= 0) {
                return true;
            }
        }
        return false;
    }
}
