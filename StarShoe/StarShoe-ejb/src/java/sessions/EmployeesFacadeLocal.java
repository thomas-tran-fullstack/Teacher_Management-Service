/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package sessions;

import entities.Employees;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author Admin
 */
@Local
public interface EmployeesFacadeLocal {

    void create(Employees employees);

    void edit(Employees employees);

    void remove(Employees employees);

    Employees find(Object id);

    List<Employees> findAll();

    List<Employees> findRange(int[] range);
    
    List<Employees> findByName(String keyword);
    
    List<Employees> searchByKeyword(String keyword);

    int count();
    
}
