package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.security.Hasher;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Role;

import java.util.List;

@Stateless
public class UserBean {

    @PersistenceContext
    private EntityManager entityManager;

    public User create(String name, String email, String password, Role role) {
        String hashed = Hasher.hash(password);
        User user = new User(name, email, hashed, role);
        entityManager.persist(user);
        return user;
    }

    public User find(long id) {
        User user = entityManager.find(User.class, id);
        if (user != null) {
            user.getTags().size();
        }
        return user;
    }

    public User find(String username) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT u FROM User u WHERE u.email = :email", User.class
        );
        query.setParameter("email", username);
        List<User> users = query.getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    public boolean canLogin(String username, String password) {
        var user = find(username);
        return user != null && Hasher.verify(password, user.getPassword());
    }

    public List<User> findAll() {
        List<User> users = entityManager.createNamedQuery("getAllUsers", User.class).getResultList();
        users.forEach(user -> user.getTags().size());
        return users;
    }

    public void update(long id, String name, String email, String password, Role role) {
        User user = find(id);
        if (user == null) return;

        user.setName(name);
        user.setEmail(email);
        user.setPassword(Hasher.hash(password));
        user.setRole(role);
    }

    public void updateUser(long id, String name, String email, Role role, boolean active) {
        User user = find(id);
        if (user == null) return;

        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        user.setActive(active);
    }

    public void delete(long id) {
        User user = find(id);
        if (user == null) return;

        entityManager.remove(user);
    }

    public void updatePassword(long id, String password) {
        User user = find(id);
        if (user == null) return;

        user.setPassword(Hasher.hash(password));
    }

    public void updateEmail(long id, String email) {
        User user = find(id);
        if (user == null) return;

        user.setEmail(email);
    }

    public void setActive(long id, boolean active) {
        User user = find(id);
        if (user == null) return;

        user.setActive(active);
    }

    public void setRole(long id, Role role) {
        User user = find(id);
        if (user == null) return;

        user.setRole(role);
    }

    public void subscribeTag(String username, long tagId) {
        User user = find(username);
        if (user == null) return;
        var tag = entityManager.find(pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Tag.class, tagId);
        if (tag == null) return;
        user.getTags().add(tag);
    }

    public void unsubscribeTag(String username, long tagId) {
        User user = find(username);
        if (user == null) return;
        var tag = entityManager.find(pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Tag.class, tagId);
        if (tag == null) return;
        user.getTags().remove(tag);
    }

    public List<Long> getTagIds(String username) {
        TypedQuery<Long> q = entityManager.createQuery(
                "SELECT t.id FROM User u JOIN u.tags t WHERE u.email = :email",
                Long.class
        );
        q.setParameter("email", username);
        return q.getResultList();
    }

    public List<String> getTagNames(String username) {
        TypedQuery<String> q = entityManager.createQuery(
                "SELECT t.name FROM User u JOIN u.tags t WHERE u.email = :email",
                String.class
        );
        q.setParameter("email", username);
        return q.getResultList();
    }
}
