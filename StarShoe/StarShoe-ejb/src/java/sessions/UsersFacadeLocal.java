package sessions;

import entities.Users;
import java.util.List;
import jakarta.ejb.Local;

@Local
public interface UsersFacadeLocal {

    void create(Users user);

    void edit(Users user);

    void remove(Users user);

    Users find(Object id);

    List<Users> findAll();

    List<Users> findRange(int[] range);

    int count();

    Users findByUsername(String username);

    List<Users> searchByKeyword(String keyword);

    Users findByUsernameAndPassword(String username, String password);
}
