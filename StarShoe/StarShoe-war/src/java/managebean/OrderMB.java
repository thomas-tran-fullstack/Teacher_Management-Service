package managebean;

import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;
import sessions.OrdersFacadeLocal;
import entities.Orders;

@Named("orderMB")
@SessionScoped
public class OrderMB implements Serializable {

    @EJB
    private OrdersFacadeLocal ordersFacade;

    private List<Orders> orderList;

    public List<Orders> getOrderList() {
        if (orderList == null) {
            orderList = ordersFacade.findAll();
        }
        return orderList;
    }

    public void approveOrder(Orders order) {
        try {
            order.setStatus("Approved");
            ordersFacade.edit(order);
            orderList = null; // Refresh list
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Order approved successfully.", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error approving order: " + e.getMessage(), null));
        }
    }

    public void rejectOrder(Orders order) {
        try {
            order.setStatus("Rejected");
            ordersFacade.edit(order);
            orderList = null; // Refresh list
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Order rejected.", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error rejecting order: " + e.getMessage(), null));
        }
    }

    public void refresh() {
        orderList = ordersFacade.findAll();
    }
}
