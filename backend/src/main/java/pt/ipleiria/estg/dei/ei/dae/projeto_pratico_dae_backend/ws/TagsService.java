package pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ws;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.PublicationVisibilityDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.dtos.ResponseDTO;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.Tag;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.security.Authenticated;
import pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.TagBean;

import java.net.URI;
import java.util.List;

@Stateless
@PermitAll
@Path("/tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Authenticated
public class TagsService {

    @EJB
    private TagBean tagBean;

    @EJB
    private pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.HistoryBean historyBean;

    @EJB
    private pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.ejbs.UserBean userBean;

    @Context
    private SecurityContext securityContext;

    @PersistenceContext
    private EntityManager entityManager;

    @GET
    public List<Tag> getAllTags() {
        TypedQuery<Tag> q = entityManager.createQuery("SELECT t FROM Tag t", Tag.class);
        return q.getResultList();
    }

    @GET
    @Path("/visible")
    public List<Tag> getVisibleTags() {
        TypedQuery<Tag> q = entityManager.createQuery("SELECT t FROM Tag t WHERE t.visible = true", Tag.class);
        return q.getResultList();
    }

    @POST
    public Response createTag(Tag tag) {
        if (securityContext == null ||
                !(securityContext.isUserInRole("RESPONSAVEL") || securityContext.isUserInRole("ADMINISTRADOR"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ResponseDTO(false, "Access forbidden for this resource"))
                    .build();
        }
        if (tag == null || tag.getName() == null || tag.getName().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, "Tag name is required"))
                    .build();
        }

        Tag created = tagBean.defineTag(tag.getName());

        var principal = securityContext.getUserPrincipal();
        if (principal != null) {
            pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User currentUser = userBean.find(principal.getName());
            if (currentUser != null) {
                historyBean.create(currentUser, "POST", "/tags",
                    String.format("{\"name\":\"%s\",\"visible\":%b}", tag.getName(), tag.isVisible()));
            }
        }

        return Response.status(Response.Status.CREATED)
                .entity(new ResponseDTO(true, "The tag " + created.getName() + " has been created successfully"))
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteTag(@PathParam("id") long id) {
        if (securityContext == null ||
                !(securityContext.isUserInRole("RESPONSAVEL") || securityContext.isUserInRole("ADMINISTRADOR"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ResponseDTO(false, "Access forbidden for this resource"))
                    .build();
        }
        try {
            Tag tag = entityManager.find(Tag.class, id);
            if (tag == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseDTO(false, "Tag not found"))
                        .build();
            }

            String tagName = tag.getName();
            tagBean.removeTag(id);

            var principal = securityContext.getUserPrincipal();
            if (principal != null) {
                pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User currentUser = userBean.find(principal.getName());
                if (currentUser != null) {
                    historyBean.create(currentUser, "DELETE", "/tags/" + id, null);
                }
            }

            return Response.ok()
                    .entity(new ResponseDTO(true, "The tag " + tagName + " has been deleted successfully"))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ResponseDTO(false, "Error deleting tag: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}/visibility")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setPublicationVisibility(
            @PathParam("id") long id,
            PublicationVisibilityDTO dto
    ) {
        if (securityContext == null ||
                !(securityContext.isUserInRole("RESPONSAVEL") || securityContext.isUserInRole("ADMINISTRADOR"))) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ResponseDTO(false, "Access forbidden for this resource"))
                    .build();
        }
        try {
            Tag tag = entityManager.find(Tag.class, id);
            if (tag == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ResponseDTO(false, "Tag not found"))
                        .build();
            }

            String tagName = tag.getName();
            tagBean.setTagVisibility(id, dto.isPublic());

            var principal = securityContext.getUserPrincipal();
            if (principal != null) {
                pt.ipleiria.estg.dei.ei.dae.projeto_pratico_dae_backend.entities.User currentUser = userBean.find(principal.getName());
                if (currentUser != null) {
                    historyBean.create(currentUser, "PUT", "/tags/" + id + "/visibility",
                            String.format("{\"public\":%b}", dto.isPublic()));
                }
            }

            String status = dto.isPublic() ? "visible" : "hidden";
            return Response.ok()
                    .entity(new ResponseDTO(true, "The tag " + tagName + " has been set to " + status))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ResponseDTO(false, e.getMessage()))
                    .build();
        }
    }


}
