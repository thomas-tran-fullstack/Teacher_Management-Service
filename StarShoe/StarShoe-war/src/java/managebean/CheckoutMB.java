package managebean;

import entities.CartItem;
import entities.Customers;
import entities.OrderDetails;
import entities.Orders;
import entities.Products;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import sessions.OrderDetailsFacadeLocal;
import sessions.OrdersFacadeLocal;
import sessions.ProductsFacadeLocal;

@Named("checkoutMB")
@SessionScoped
public class CheckoutMB implements Serializable {

    @EJB
    private OrdersFacadeLocal ordersFacade;
    @EJB
    private OrderDetailsFacadeLocal orderDetailsFacade;
    @EJB
    private ProductsFacadeLocal productsFacade;

    private String fullName;
    private String phone;
    private String address;

    public String placeOrder() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        // Validate form
        if (fullName == null || fullName.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty() ||
            address == null || address.trim().isEmpty()) {
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "All fields are required.", null));
            return null;
        }

        try {
            // Get current customer from session
            Customers customer = (Customers) ctx.getExternalContext().getSessionMap().get("currentCustomer");
            if (customer == null) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Please log in before placing an order.", null));
                return "/login.xhtml?faces-redirect=true";
            }

            // Get cart from session
            CartMB cartMB = (CartMB) ctx.getExternalContext().getSessionMap().get("cartMB");
            if (cartMB == null || cartMB.getCartItems().isEmpty()) {
                ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Your cart is empty.", null));
                return null;
            }

            // Create Order
            Orders order = new Orders();
            order.setOrderDate(new Date());
            order.setStatus("Pending");
            order.setCustomerID(customer);
            order.setTotalAmount(BigDecimal.valueOf(cartMB.getTotalAmount()));

            // Persist order
            ordersFacade.create(order);

            // Create OrderDetails from cart items and update product quantity
            for (CartMB.CartItem cartItem : cartMB.getCartItems()) {
                Products product = cartItem.getProduct();

                // Check if product exists and has sufficient quantity
                if (product == null || product.getQuantity() < cartItem.getQuantity()) {
                    ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Product '" + (product != null ? product.getProductName() : "Unknown")
                            + "' is out of stock.", null));
                    return null;
                }

                // Create OrderDetail
                OrderDetails detail = new OrderDetails();
                detail.setOrderID(order);
                detail.setProductID(product);
                detail.setQuantity(cartItem.getQuantity());
                detail.setUnitPrice(product.getPrice());

                // Reduce product quantity
                product.setQuantity(product.getQuantity() - cartItem.getQuantity());
                productsFacade.edit(product);

                // Persist detail
                orderDetailsFacade.create(detail);
            }

            // Clear cart
            cartMB.getCartItems().clear();
            cartMB.updateTotal();

            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Order placed successfully! Order ID: " + order.getOrderID(), null));

            return "/client/order-confirmation.xhtml?faces-redirect=true&orderId=" + order.getOrderID();

        } catch (Exception e) {
            e.printStackTrace();
            ctx.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error placing order: " + e.getMessage(), null));
            return null;
        }
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
