package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.EmailDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Tag;

import java.util.ArrayList;
import java.util.List;

@Stateless
public class TagBean {

    @PersistenceContext
    private EntityManager entityManager;

    public Tag defineTag(String name) {
        Tag tag = new Tag(name);
        entityManager.persist(tag);
        return tag;
    }

    public void removeTag(long tagId) {
        Tag tag = entityManager.find(Tag.class, tagId);
        if (tag == null) {
            throw new IllegalArgumentException("Tag with id " + tagId + " not found");
        }

        TypedQuery<Publication> query = entityManager.createQuery(
                "SELECT p FROM Publication p JOIN p.tags t WHERE t.id = :tagId",
                Publication.class
        );
        query.setParameter("tagId", tagId);
        List<Publication> publications = query.getResultList();

        for (Publication pub : publications) {
            pub.getTags().remove(tag);
        }

        entityManager.remove(tag);
    }

    public void setTagVisibility(long tagId, boolean visible) {
        Tag tag = entityManager.find(Tag.class, tagId);
        if (tag == null) {
            throw new IllegalArgumentException("Tag with id " + tagId + " not found");
        }
       tag.setVisible(visible);
    }
}
