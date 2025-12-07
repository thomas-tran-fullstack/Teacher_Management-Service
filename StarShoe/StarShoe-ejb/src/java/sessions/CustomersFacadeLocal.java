package sessions;

import entities.Customers;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface CustomersFacadeLocal {

    void create(Customers c);

    void edit(Customers c);

    void remove(Customers c);

    Customers find(Object id);

    List<Customers> findAll();

    List<Customers> findRange(int[] range);

    int count();

    List<Customers> findByName(String keyword);
    
    List<Customers> searchByKeyword(String keyword);
    
    Customers findByUserId(int userId);

}
