/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sessions;

import entities.Orders;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author Admin
 */
@Stateless
public class OrdersFacade extends AbstractFacade<Orders> implements OrdersFacadeLocal {

    @PersistenceContext(unitName = "StarShoe-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public OrdersFacade() {
        super(Orders.class);
    }

    @Override
    public List<Orders> findByCustomerName(String keyword) {
        return em.createQuery("SELECT o FROM Orders o WHERE LOWER(o.customerID.fullName) LIKE :kw", Orders.class)
            .setParameter("kw", "%" + keyword.toLowerCase() + "%")
            .getResultList();
    }

    @Override
    public List<Orders> findAll() {
        return em.createQuery("SELECT o FROM Orders o", Orders.class).getResultList();
    }

    @Override
    public void create(Orders o) {
        em.persist(o);
    }

    @Override
    public void edit(Orders o) {
        em.merge(o);
    }

    @Override
    public void remove(Orders o) {
        em.remove(em.merge(o));
    }

    @Override
    public List<Orders> getOrdersByUser(Integer userID) {
        TypedQuery<Orders> query = em.createQuery(
                "SELECT o FROM Orders o WHERE o.customerID.userID = :userID ORDER BY o.orderDate DESC", Orders.class);
        query.setParameter("userID", userID);
        return query.getResultList();
    }

}
