/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sessions;

import entities.Reviews;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 *
 * @author Admin
 */
@Stateless
public class ReviewsFacade extends AbstractFacade<Reviews> implements ReviewsFacadeLocal {

    @PersistenceContext(unitName = "StarShoe-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ReviewsFacade() {
        super(Reviews.class);
    }

    @Override
    public void create(Reviews r) {
        em.persist(r);
    }

    @Override
    public List<Reviews> findByProduct(int productID) {
        return em.createQuery(
                "SELECT r FROM Reviews r WHERE r.productID.productID = :p ORDER BY r.createdAt DESC",
                Reviews.class
        )
                .setParameter("p", productID)
                .getResultList();
    }

    @Override
    public List<Reviews> findByProductAndCustomer(int pid, int cid) {
        return em.createQuery(
                "SELECT r FROM Reviews r WHERE r.productID.productID = :p AND r.customerID.customerID = :c",
                Reviews.class
        )
                .setParameter("p", pid)
                .setParameter("c", cid)
                .getResultList();
    }

    @Override
    public Reviews findOneByProductAndCustomer(int pid, int cid) {
        List<Reviews> list = findByProductAndCustomer(pid, cid);
        return list.isEmpty() ? null : list.get(0);
    }

}
