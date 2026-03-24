package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ws;

import jakarta.annotation.security.PermitAll;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.security.Authenticated;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.NotificationDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Comment;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
@PermitAll
@Path("/notifications")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
public class NotificationsService {

    @PersistenceContext
    private EntityManager entityManager;

    @EJB
    private UserBean userBean;

    @Context
    private SecurityContext securityContext;

    @GET
    @PermitAll
    public List<NotificationDTO> getNotifications() {
        var principal = securityContext.getUserPrincipal();
        if (principal == null) return List.of();
        String username = principal.getName();
        User user = userBean.find(username);
        if (user == null) return List.of();

        Set<Long> tagIds = user.getTags().stream().map(t -> t.getId()).collect(Collectors.toSet());
        if (tagIds.isEmpty()) return List.of();

        LocalDateTime since = LocalDateTime.now().minusDays(7);
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        List<NotificationDTO> result = new ArrayList<>();

        //comentarios recentes em publicações associadas às tags do user
        TypedQuery<Comment> q1 =
            entityManager.createQuery(
                "SELECT c FROM Comment c JOIN c.publication p JOIN p.tags t WHERE t.id IN :tagIds AND c.date >= :since AND c.visible = true ORDER BY c.date DESC",
                Comment.class
            );
        q1.setParameter("tagIds", tagIds);
        q1.setParameter("since", since);
        List<Comment> comments = q1.getResultList();
        long id = 1;
        for (var c : comments) {
            String text = c.getUser().getName() + " comentou em " + c.getPublication().getName() + ": " + (c.getText().length() > 120 ? c.getText().substring(0, 120) + "..." : c.getText());
            result.add(new NotificationDTO(id++, text, c.getDate().format(fmt), false));
        }

        //publicações recentes com as tags do user
        TypedQuery<Publication> q2 =
            entityManager.createQuery(
                "SELECT DISTINCT p FROM Publication p JOIN p.tags t WHERE t.id IN :tagIds AND p.data >= :sinceDate ORDER BY p.data DESC",
                Publication.class
            );
        q2.setParameter("tagIds", tagIds);
        q2.setParameter("sinceDate", java.time.LocalDate.now().minusDays(7));
        List<Publication> pubs = q2.getResultList();
        for (var p : pubs) {
            String text = "Nova publicação com tag que acompanhas: " + p.getName();
            String when = p.getData() != null ? p.getData().toString() : LocalDateTime.now().format(fmt);
            result.add(new NotificationDTO(id++, text, when, false));
        }

        return result;
    }
}
