package sessions;

import entities.Categories;
import java.util.List;
import jakarta.ejb.Local;

@Local
public interface CategoriesFacadeLocal {
    void create(Categories category);
    void edit(Categories category);
    void remove(Categories category);
    Categories find(Object id);
    List<Categories> findAll();
    List<Categories> findRange(int[] range);
    public List<Categories> findByName(String keyword);
    int count();
}
