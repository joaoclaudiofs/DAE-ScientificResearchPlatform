package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.History;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User;

import java.util.List;

@Stateless
public class HistoryBean {
    @PersistenceContext
    private EntityManager entityManager;

    public void create(User user, String action, String endpoint, String body) {
        History history = new History(user, action, endpoint, body);
        entityManager.persist(history);
    }

    public List<History> getHistoryByUser(long userId) {
        return entityManager.createNamedQuery("getHistoryByUser", History.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<History> getAllHistory() {
        return entityManager.createNamedQuery("getAllHistory", History.class)
                .getResultList();
    }
}
