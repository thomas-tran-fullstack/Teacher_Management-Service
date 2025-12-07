package sessions;

import entities.Orders;
import java.util.List;
import jakarta.ejb.Local;

@Local
public interface OrdersFacadeLocal {
    void create(Orders o);
    void edit(Orders o);
    void remove(Orders o);
    Orders find(Object id);
    List<Orders> findAll();
    List<Orders> findRange(int[] range);
    int count();

    List<Orders> findByCustomerName(String keyword);
    List<Orders> getOrdersByUser(Integer userID);
}
