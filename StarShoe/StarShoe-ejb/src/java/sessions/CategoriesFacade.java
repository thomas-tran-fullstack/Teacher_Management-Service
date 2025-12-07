/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sessions;

import entities.Categories;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 *
 * @author Admin
 */
@Stateless
public class CategoriesFacade extends AbstractFacade<Categories> implements CategoriesFacadeLocal {
    @PersistenceContext(unitName = "StarShoe-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CategoriesFacade() {
        super(Categories.class);
    }

    @Override
    public List<Categories> findByName(String keyword) {
        return em.createQuery("SELECT c FROM Categories c WHERE LOWER(c.categoryName) LIKE :kw", Categories.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    @Override
    public List<Categories> findAll() {
        return em.createQuery("SELECT c FROM Categories c", Categories.class).getResultList();
    }

    @Override
    public void create(Categories c) {
        em.persist(c);
    }

    @Override
    public void edit(Categories c) {
        em.merge(c);
    }

    @Override
    public void remove(Categories c) {
        em.remove(em.merge(c));
    }

}
