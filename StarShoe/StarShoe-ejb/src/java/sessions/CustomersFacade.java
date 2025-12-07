/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sessions;

import entities.Customers;
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
public class CustomersFacade extends AbstractFacade<Customers> implements CustomersFacadeLocal {

    @PersistenceContext(unitName = "StarShoe-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CustomersFacade() {
        super(Customers.class);
    }

    @Override
    public List<Customers> findByName(String keyword) {
        return em.createQuery("SELECT c FROM Customers c WHERE LOWER(c.customerName) LIKE :kw", Customers.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    @Override
    public List<Customers> findAll() {
        return em.createQuery("SELECT c FROM Customers c", Customers.class).getResultList();
    }

    public Customers find(Integer id) {
        return em.find(Customers.class, id);
    }

    @Override
    public List<Customers> searchByKeyword(String keyword) {
        return em.createQuery("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE :kw OR LOWER(c.email) LIKE :kw", Customers.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }
    
    @Override
    public Customers findByUserId(int userId) {
        try {
            TypedQuery<Customers> query = em.createQuery(
                "SELECT c FROM Customers c WHERE c.userID.userID = :uid", Customers.class);
            query.setParameter("uid", userId);
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

}
