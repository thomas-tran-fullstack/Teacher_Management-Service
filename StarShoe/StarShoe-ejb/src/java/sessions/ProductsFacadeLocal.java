package sessions;

import entities.Products;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface ProductsFacadeLocal {

    void create(Products p);

    void edit(Products p);

    void remove(Products p);

    Products find(Object id);

    List<Products> findAll();

    List<Products> findRange(int[] range);

    int count();

    List<Products> findByName(String keyword);

    List<Products> findByBrand(int brandID);

    List<Products> findByCategory(int categoryID);
    
    List<Products> searchByKeyword(String keyword);
    
    List<Products> getHotProducts();

    List<Products> findRandomProducts(int limit, int excludeProductId);
    
    List<Products> getProductsByBrand(String brandName);

    List<Products> getProductsByCategory(String categoryName);
}
