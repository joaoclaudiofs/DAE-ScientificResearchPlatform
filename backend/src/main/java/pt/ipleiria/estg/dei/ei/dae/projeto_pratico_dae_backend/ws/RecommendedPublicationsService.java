package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ws;

import jakarta.ejb.EJB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.PublicationListDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.UserBean;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Publication;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.security.Authenticated;

import java.util.*;
import java.util.stream.Collectors;

@Path("/publications/recommended")
@Produces(MediaType.APPLICATION_JSON)
public class RecommendedPublicationsService {

    @EJB
    private UserBean userBean;

    @PersistenceContext
    private EntityManager em;

    @Context
    private SecurityContext securityContext;

    private static final int MAX_RESULTS = 3;

    @GET
    @Authenticated
    public List<PublicationListDTO> getRecommended() {
        var principal = securityContext.getUserPrincipal();
        if (principal == null) {
            return List.of();
        }

        String username = principal.getName();
        // buscar as tags subscritas pelo utilizador
        Set<Long> subscritasTagIds = new HashSet<>(userBean.getTagIds(username));

        // se não tiver subscrições, mostrar as publicações mais recentes
        // se tiver subscrições, mostrar as publicações que tenham essas tags
        List<Publication> publications = subscritasTagIds.isEmpty()
                ? getRecentPublications()
                : getPublicationsByTags(subscritasTagIds);

        // se não houver publicações visíveis, retornar lista vazia
        if (publications.isEmpty()) {
            return List.of();
        }

        // obter estatísticas de rating para as publicações obtidas
        Map<Long, RatingStats> ratingStats = getRatingStats(publications.stream()
                .map(Publication::getId)
                .collect(Collectors.toList()));

        // converter para DTOs e retornar
        return publications.stream()
                .map(p -> toDTO(p, ratingStats))
                .collect(Collectors.toList());
    }

    private List<Publication> getRecentPublications() {
        return em.createQuery(
                        "SELECT DISTINCT p FROM Publication p " +
                                "LEFT JOIN FETCH p.tags " +
                                "WHERE p.visible = true " +
                                "ORDER BY p.data DESC",
                        Publication.class)
                .setMaxResults(MAX_RESULTS)
                .getResultList();
    }

    private List<Publication> getPublicationsByTags(Set<Long> tagIds) {
        return em.createQuery(
                        "SELECT DISTINCT p FROM Publication p " +
                                "JOIN p.tags t " +
                                "LEFT JOIN FETCH p.tags " +
                                "WHERE t.id IN :tagIds AND p.visible = true " +
                                "ORDER BY p.data DESC",
                        Publication.class)
                .setParameter("tagIds", tagIds)
                .setMaxResults(MAX_RESULTS)
                .getResultList();
    }

    private Map<Long, RatingStats> getRatingStats(List<Long> publicationIds) {
        if (publicationIds.isEmpty()) return Map.of();

        TypedQuery<Object[]> query = em.createQuery(
                "SELECT r.publication.id, COUNT(r), AVG(r.value) " +
                        "FROM Rating r " +
                        "WHERE r.publication.id IN :ids " +
                        "GROUP BY r.publication.id",
                Object[].class);
        query.setParameter("ids", publicationIds);

        Map<Long, RatingStats> stats = new HashMap<>();
        for (Object[] row : query.getResultList()) {
            Long pubId = ((Number) row[0]).longValue();
            long count = ((Number) row[1]).longValue();
            double avg = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
            stats.put(pubId, new RatingStats(count, avg));
        }
        return stats;
    }

    // Mapper adaptado para PublicationListDTO completo
    private PublicationListDTO toDTO(Publication p, Map<Long, RatingStats> stats) {
        RatingStats rs = stats.getOrDefault(p.getId(), new RatingStats(0, 0));

        List<String> tagNames = p.getTags() == null
                ? List.of()
                : p.getTags().stream()
                .filter(Tag::isVisible)
                .map(Tag::getName)
                .collect(Collectors.toList());

        String userName = p.getUser() != null ? p.getUser().getUsername() : null;

        return new PublicationListDTO(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getData() != null ? p.getData().toString() : null,
                p.getArea() != null ? p.getArea().getName() : null,
                (int) rs.count,
                tagNames,
                rs.avg,
                (int) rs.count,
                p.isPublic(),
                userName
        );
    }


    private static class RatingStats {
        long count;
        double avg;

        RatingStats(long count, double avg) {
            this.count = count;
            this.avg = avg;
        }
    }
}
