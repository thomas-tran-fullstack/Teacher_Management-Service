/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sessions;

import entities.Brands;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 *
 * @author Admin
 */
@Stateless
public class BrandsFacade extends AbstractFacade<Brands> implements BrandsFacadeLocal {

    @PersistenceContext(unitName = "StarShoe-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public BrandsFacade() {
        super(Brands.class);
    }
    
    @Override
    public List<Brands> findByName(String keyword) {
        return em.createQuery("SELECT c FROM Brands c WHERE LOWER(c.brandName) LIKE :kw", Brands.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }
    
    @Override
    public List<Brands> findAll() {
        return em.createQuery("SELECT b FROM Brands b", Brands.class).getResultList();
    }

    @Override
    public void create(Brands b) {
        em.persist(b);
    }

    @Override
    public void edit(Brands b) {
        em.merge(b);
    }

    @Override
    public void remove(Brands b) {
        em.remove(em.merge(b));
    }
    
}
