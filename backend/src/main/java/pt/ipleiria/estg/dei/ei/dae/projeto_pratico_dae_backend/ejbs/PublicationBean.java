package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.EmailDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Document;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Area;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Rating;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Stateless
public class PublicationBean {

    @PersistenceContext
    private EntityManager entityManager;

    @EJB
    private EmailBean emailBean;

    public long create(String name, LocalDateTime data, String description, 
                       boolean isPublic, Long fileId, Integer areaId, List<Long> tagIds, User user) {
        Document file = null;
        if (fileId != null) {
            file = entityManager.find(Document.class, fileId);
        }

        Area area = null;
        if (areaId != null) {
            area = entityManager.find(Area.class, areaId);
        }

        Publication publication = new Publication(name, data, description, isPublic, file, area, user);
        
        //add tags if provided
        if (tagIds != null && !tagIds.isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (Long tagId : tagIds) {
                Tag tag = entityManager.find(Tag.class, tagId);
                if (tag != null) {
                    tags.add(tag);
                }
            }
            publication.setTags(tags);
        }
        
        entityManager.persist(publication);

        //notificar os users subscritos na tag
        TypedQuery<pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User> q = entityManager.createQuery(
                "SELECT u FROM User u JOIN u.tags t WHERE t.id = :tagId",
                pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User.class
        );
        for (Long tagId : tagIds) {
            q.setParameter("tagId", tagId);
            List<pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User> users = q.getResultList();
            if (users != null && !users.isEmpty()) {
                EmailDTO dto = new EmailDTO();
                List<String> recipients = new ArrayList<>();
                for (var u : users) {
                    if (u.getEmail() != null && !u.getEmail().isBlank()) recipients.add(u.getEmail());
                }
                if (!recipients.isEmpty()) {
                    dto.setRecipients(recipients);
                    dto.setSubject("Novidade na tag: " + tagId);
                    String link = "/api/publicacoes/" + publication.getId();
                    dto.setBody("Uma publicação foi associada à tag <b>" + "</b>: <br/>" +
                            "<b>" + publication.getName() + "</b><br/>" +
                            publication.getDescription() + "<br/>" +
                            "Ver: " + link);
                    dto.setHtml(true);
                    //enviar o email
                    if (emailBean != null) emailBean.send(dto);
                }
            }
        }

        return publication.getId();
    }

    public Publication find(long id) {
        return entityManager.find(Publication.class, id);
    }

    public void delete(long id) {
        Publication publication = find(id);
        if (publication != null) {
            entityManager.remove(publication);
        }
    }

    public void update(long id, String name, String description) {
        Publication publication = find(id);
        if (publication != null) {
            publication.setName(name);
            publication.setDescription(description);
        }
    }



    public void addRating(long publicationId, User user, int value) {
        Publication publication = find(publicationId);
        if (publication == null) {
            throw new IllegalArgumentException("Publication not found");
        }

        Rating rating = new Rating(publication, user, value, LocalDateTime.now());
        entityManager.persist(rating);
    }

    public Rating findRating(long ratingId) {
        return entityManager.find(Rating.class, ratingId);
    }

    public void removeRating(long publicationId, long ratingId) {
        Rating rating = findRating(ratingId);
        if (rating == null) {
            throw new IllegalArgumentException("Rating not found");
        }

        if (rating.getPublication().getId() != publicationId) {
            throw new IllegalArgumentException("Rating does not belong to this publication");
        }

        entityManager.remove(rating);
    }

    public void addTag(long publicationId, long tagId) {
        Publication publication = find(publicationId);
        if (publication == null) {
            throw new IllegalArgumentException("Publication not found");
        }

        Tag tag = entityManager.find(Tag.class, tagId);
        if (tag == null) {
            throw new IllegalArgumentException("Tag not found");
        }

        publication.getTags().add(tag);
    }

    public void removeTag(long publicationId, long tagId) {
        Publication publication = find(publicationId);
        if (publication == null) {
            throw new IllegalArgumentException("Publication not found");
        }

        Tag tag = entityManager.find(Tag.class, tagId);
        if (tag == null) {
            throw new IllegalArgumentException("Tag not found");
        }

        publication.getTags().remove(tag);
    }

    public void addComment(long publicationId, User user, String text) {
        Publication publication = find(publicationId);
        if (publication == null) {
            throw new IllegalArgumentException("Publication not found");
        }

        Comment comment = new Comment(publication, user, text, LocalDateTime.now(), true);
        entityManager.persist(comment);

        //notificar os users subscritos nas tags da publicacao
        TypedQuery<pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Tag> tq = entityManager.createQuery(
                "SELECT t FROM Publication p JOIN p.tags t WHERE p.id = :pubId",
                pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Tag.class
        );
        tq.setParameter("pubId", publication.getId());
        List<pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Tag> tags = tq.getResultList();
        if (tags != null && !tags.isEmpty()) {
            List<Long> tagIds = tags.stream().map(t -> t.getId()).collect(Collectors.toList());
            TypedQuery<String> q = entityManager.createQuery(
                    "SELECT DISTINCT u.email FROM User u JOIN u.tags t WHERE t.id IN :tagIds AND u.email IS NOT NULL",
                    String.class
            );
            q.setParameter("tagIds", tagIds);
            List<String> emails = q.getResultList();
            if (emails != null && !emails.isEmpty()) {
                EmailDTO dto = new EmailDTO();
                dto.setRecipients(emails.stream().distinct().collect(Collectors.toList()));
                dto.setSubject("Novo comentário em: " + publication.getName());
                String link = "/api/publicacoes/" + publication.getId();
                dto.setBody("<b>" + user.getName() + "</b> comentou em <b>" + publication.getName() + "</b>:<br/>" +
                        text + "<br/>Ver: " + link);
                dto.setHtml(true);
                emailBean.send(dto);
            }
        }
    }

    public List<Publication> findByUser(User user) {
        return entityManager.createQuery(
            "SELECT p FROM Publication p WHERE p.user = :user", 
            Publication.class)
            .setParameter("user", user)
            .getResultList();
    }

    public void setPublicationVisibility(long id, boolean isPublic) {
        Publication p = entityManager.find(Publication.class, id);
        if (p == null) {
            throw new IllegalArgumentException("Publicação não encontrada");
        }

        p.setPublic(isPublic);
        entityManager.merge(p);
    }

    public List<Publication> getHiddenPublications() {
        TypedQuery<Publication> query = entityManager.createQuery(
                "SELECT p FROM Publication p WHERE p.isPublic = false",
                Publication.class
        );
        return query.getResultList();
    }

    public void setCommentVisibility(long pubId, long commentId, boolean visible) {

        Publication p = entityManager.find(Publication.class, pubId);
        if (p == null) {
            throw new IllegalArgumentException("Publicação não encontrada");
        }

        Comment comment = p.getComments()
                .stream()
                .filter(c -> c.getId() == commentId)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Comentário não encontrado"));

        comment.setVisible(visible);
        entityManager.merge(comment);
    }
}
