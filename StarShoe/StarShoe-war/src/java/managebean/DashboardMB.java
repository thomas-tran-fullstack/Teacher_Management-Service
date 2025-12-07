package managebean;

import entities.Orders;
import entities.Products;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import sessions.OrdersFacadeLocal;
import sessions.ProductsFacadeLocal;

@Named("dashboardMB")
@SessionScoped
public class DashboardMB implements Serializable {

    @EJB
    private OrdersFacadeLocal ordersFacade;
    @EJB
    private ProductsFacadeLocal productsFacade;

    private List<Orders> recentOrders;
    private List<Products> lowStockProductList;

    public BigDecimal getTotalRevenue() {
        try {
            List<Orders> allOrders = ordersFacade.findAll();
            return allOrders.stream()
                    .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    public Integer getTotalOrders() {
        try {
            return ordersFacade.findAll().size();
        } catch (Exception e) {
            return 0;
        }
    }

    public Integer getProductsInStock() {
        try {
            List<Products> products = productsFacade.findAll();
            return (int) products.stream()
                    .filter(p -> p.getQuantity() != null && p.getQuantity() > 0)
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    public Integer getLowStockProducts() {
        try {
            List<Products> products = productsFacade.findAll();
            return (int) products.stream()
                    .filter(p -> p.getQuantity() != null && p.getQuantity() > 0 && p.getQuantity() < 5)
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    public List<Orders> getRecentOrders() {
        if (recentOrders == null) {
            try {
                List<Orders> allOrders = ordersFacade.findAll();
                // Sort by date descending and limit to 5 most recent
                recentOrders = allOrders.stream()
                        .sorted((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()))
                        .limit(5)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                e.printStackTrace();
                recentOrders = java.util.Collections.emptyList();
            }
        }
        return recentOrders;
    }

    public List<Products> getLowStockProductList() {
        if (lowStockProductList == null) {
            try {
                List<Products> products = productsFacade.findAll();
                // Filter products with quantity < 5 and sort by quantity ascending
                lowStockProductList = products.stream()
                        .filter(p -> p.getQuantity() != null && p.getQuantity() < 5 && p.getQuantity() > 0)
                        .sorted((a, b) -> a.getQuantity().compareTo(b.getQuantity()))
                        .limit(10)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                e.printStackTrace();
                lowStockProductList = java.util.Collections.emptyList();
            }
        }
        return lowStockProductList;
    }

    public void refresh() {
        recentOrders = null;
        lowStockProductList = null;
    }
}
