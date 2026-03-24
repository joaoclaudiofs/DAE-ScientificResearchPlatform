package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Role;

import java.util.List;

@Stateless
public class RoleBean {
    @PersistenceContext
    private EntityManager entityManager;

    public void create(String name){
        Role role = new Role(name);
        entityManager.persist(role);
    }

    public List<Role> findAll() {
        return entityManager.createNamedQuery("getAllRoles", Role.class).getResultList();
    }

    public Role find(int id) {
        return entityManager.find(Role.class, id);
    }

    public Role find(String name) {
        var roles = entityManager.createQuery("SELECT r FROM Role r WHERE r.name = :name", Role.class)
            .setParameter("name", name)
            .getResultList();

        if (roles.isEmpty()) return null;
        return roles.get(0);
    }
}
