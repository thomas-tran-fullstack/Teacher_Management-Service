/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sessions;

import entities.Reviews;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author Admin
 */
@Local
public interface ReviewsFacadeLocal {

    void create(Reviews reviews);

    void edit(Reviews reviews);

    void remove(Reviews reviews);

    Reviews find(Object id);

    List<Reviews> findAll();

    List<Reviews> findRange(int[] range);

    int count();

    List<Reviews> findByProduct(int productId);

    List<Reviews> findByProductAndCustomer(int productID, int customerID);

    Reviews findOneByProductAndCustomer(int productID, int customerID);

}
