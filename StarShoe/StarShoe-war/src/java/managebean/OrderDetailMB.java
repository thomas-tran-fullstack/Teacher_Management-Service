package managebean;

import entities.OrderDetails;
import entities.Orders;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;
import sessions.OrderDetailsFacadeLocal;
import sessions.OrdersFacadeLocal;

@Named("orderDetailMB")
@SessionScoped
public class OrderDetailMB implements Serializable {

    @EJB
    private OrdersFacadeLocal ordersFacade;
    @EJB
    private OrderDetailsFacadeLocal orderDetailsFacade;

    private Orders order;
    private List<OrderDetails> detailList;

    public void loadOrder() {
        try {
            String id = FacesContext.getCurrentInstance().getExternalContext()
                    .getRequestParameterMap().get("orderId");

            if (id != null) {
                int oid = Integer.parseInt(id);
                order = ordersFacade.find(oid);
                detailList = orderDetailsFacade.findByOrder(oid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Orders getOrder() {
        return order;
    }

    public List<OrderDetails> getDetailList() {
        if (detailList == null && order != null) {
            detailList = orderDetailsFacade.findByOrder(order.getOrderID());
        }
        return detailList;
    }

    public void refresh() {
        if (order != null) {
            detailList = orderDetailsFacade.findByOrder(order.getOrderID());
        }
    }
}
