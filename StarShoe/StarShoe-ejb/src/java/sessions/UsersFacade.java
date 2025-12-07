package sessions;

import entities.Users;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Stateless
public class UsersFacade extends AbstractFacade<Users> implements UsersFacadeLocal {

    @PersistenceContext(unitName = "StarShoe-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UsersFacade() {
        super(Users.class);
    }

    @Override
    public Users findByUsernameAndPassword(String username, String password) {
        try {
            return em.createQuery(
                    "SELECT u FROM Users u WHERE u.username = :username AND u.passwordHash = :password",
                    Users.class)
                    .setParameter("username", username)
                    .setParameter("password", password)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Users findByUsername(String username) {
        try {
            return em.createQuery(
                    "SELECT u FROM Users u WHERE u.username = :username",
                    Users.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Users> searchByKeyword(String keyword) {
        return em.createQuery("SELECT u FROM Users u WHERE LOWER(u.username) LIKE :kw", Users.class)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    @Override
    public List<Users> findAll() {
        return em.createQuery("SELECT u FROM Users u", Users.class).getResultList();
    }
}
