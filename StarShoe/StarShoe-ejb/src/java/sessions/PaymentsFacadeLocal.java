/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sessions;

import entities.Payments;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author Admin
 */
@Local
public interface PaymentsFacadeLocal {

    void create(Payments payments);

    void edit(Payments payments);

    void remove(Payments payments);

    Payments find(Object id);

    List<Payments> findAll();

    List<Payments> findRange(int[] range);

    int count();
    
}
