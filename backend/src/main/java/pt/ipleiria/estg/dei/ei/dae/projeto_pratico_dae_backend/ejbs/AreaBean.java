package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Area;

import java.util.List;

@Stateless
public class AreaBean {

    @PersistenceContext
    private EntityManager em;

    public List<Area> findAll() {
        return em.createQuery("SELECT a FROM Area a", Area.class)
                .getResultList();
    }

    public Area find(int id) {
        return em.find(Area.class, id);
    }

    public Area  create(String name) {
        Area area = new Area();
        area.setName(name);
        em.persist(area);
        return area;
    }

    public void delete(int id) {
        Area area = find(id);
        if (area == null) {
            throw new IllegalArgumentException("Area not found");
        }
        em.remove(area);
    }
}
