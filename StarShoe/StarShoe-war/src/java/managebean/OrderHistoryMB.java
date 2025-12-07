package managebean;

import entities.Customers;
import entities.Orders;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;
import sessions.OrdersFacadeLocal;

@Named("orderHistoryMB")
@SessionScoped
public class OrderHistoryMB implements Serializable {

    @EJB
    private OrdersFacadeLocal ordersFacade;

    private List<Orders> orderList;

    public List<Orders> getOrderList() {
        if (orderList == null) {
            loadOrders();
        }
        return orderList;
    }

    private void loadOrders() {
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            Customers customer = (Customers) ctx.getExternalContext().getSessionMap().get("currentCustomer");

            if (customer != null && customer.getCustomerID() != null) {
                orderList = ordersFacade.getOrdersByUser(customer.getCustomerID());
            } else {
                orderList = java.util.Collections.emptyList();
            }
        } catch (Exception e) {
            e.printStackTrace();
            orderList = java.util.Collections.emptyList();
        }
    }

    public void refresh() {
        orderList = null;
        loadOrders();
    }
}
