/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sessions;

import entities.Products;
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
public class ProductsFacade extends AbstractFacade<Products> implements ProductsFacadeLocal {

    @PersistenceContext(unitName = "StarShoe-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProductsFacade() {
        super(Products.class);
    }

    @Override
    public List<Products> findByName(String keyword) {
        return em.createQuery("SELECT p FROM Products p WHERE LOWER(p.productName) LIKE :kw", Products.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    @Override
    public List<Products> findAll() {
        return em.createQuery("SELECT p FROM Products p", Products.class).getResultList();
    }

    public Products find(Integer id) {
        return em.find(Products.class, id);
    }

    @Override
    public List<Products> getHotProducts() {
        return em.createQuery("SELECT p FROM Products p ORDER BY p.productID ASC", Products.class)
                .setMaxResults(5)
                .getResultList();
    }

    @Override
    public void create(Products p) {
        em.persist(p);
    }

    @Override
    public void edit(Products p) {
        em.merge(p);
    }

    @Override
    public void remove(Products p) {
        em.remove(em.merge(p));
    }

    @Override
    public List<Products> findByBrand(int brandID) {
        return em.createQuery("SELECT p FROM Products p WHERE p.brandID.brandID = :bid", Products.class)
                .setParameter("bid", brandID)
                .getResultList();
    }

    @Override
    public List<Products> findByCategory(int categoryID) {
        return em.createQuery("SELECT p FROM Products p WHERE p.categoryID.categoryID = :cid", Products.class)
                .setParameter("cid", categoryID)
                .getResultList();
    }

    @Override
    public List<Products> searchByKeyword(String keyword) {
        return em.createQuery("SELECT p FROM Products p WHERE LOWER(p.productName) LIKE :kw", Products.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    @Override
    public List<Products> findRandomProducts(int limit, int excludeProductId) {
        return em.createQuery("SELECT p FROM Products p WHERE p.productID <> :id ORDER BY FUNCTION('NEWID')", Products.class)
                .setParameter("id", excludeProductId)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<Products> getProductsByBrand(String brandName) {
        TypedQuery<Products> query = em.createQuery(
                "SELECT p FROM Products p WHERE p.brandID.brandName = :brandName ORDER BY p.productID ASC", Products.class);
        query.setParameter("brandName", brandName);
        return query.getResultList();
    }

    @Override
    public List<Products> getProductsByCategory(String categoryName) {
        TypedQuery<Products> query = em.createQuery(
                "SELECT p FROM Products p WHERE p.categoryID.categoryName = :categoryName ORDER BY p.productID ASC", Products.class);
        query.setParameter("categoryName", categoryName);
        return query.getResultList();
    }

}
