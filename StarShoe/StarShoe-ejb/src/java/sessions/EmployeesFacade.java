/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sessions;

import entities.Employees;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 *
 * @author Admin
 */
@Stateless
public class EmployeesFacade extends AbstractFacade<Employees> implements EmployeesFacadeLocal {

    @PersistenceContext(unitName = "StarShoe-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public EmployeesFacade() {
        super(Employees.class);
    }

    public List<Employees> findByName(String keyword) {
        return em.createQuery("SELECT c FROM Employees c WHERE LOWER(c.fullName) LIKE :kw", Employees.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    @Override
    public List<Employees> findAll() {
        return em.createQuery("SELECT e FROM Employees e", Employees.class).getResultList();
    }

    @Override
    public void create(Employees e) {
        em.persist(e);
    }

    @Override
    public void edit(Employees e) {
        em.merge(e);
    }

    @Override
    public void remove(Employees e) {
        em.remove(em.merge(e));
    }

    @Override
    public List<Employees> searchByKeyword(String keyword) {
        return em.createQuery(
                "SELECT e FROM Employees e WHERE "
                + "LOWER(e.fullName) LIKE :kw OR "
                + "LOWER(e.position) LIKE :kw OR "
                + "e.phone LIKE :kw", Employees.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

}
